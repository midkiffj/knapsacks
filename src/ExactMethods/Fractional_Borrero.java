package ExactMethods;

import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;
import Problems.Fractional;
import Solutions.FractionalSol;

/**
 * Run the Fractional linearization of Borrero et al.
 * - Solve the relaxed LP of formulation R1
 * - Use solution as upper bound to formulation R4
 * 
 * @author midkiffj
 */
public class Fractional_Borrero {

	private static String file;
	private static Fractional f;
	private static IloCplex cplex;
	private static double bestObj;
	private static double gap;
	private static boolean timeout;

	// Cplex vars
	static IloNumVar[] x;
	static IloNumVar[] y;
	static IloNumVar[][] z;
	static IloNumVar[][] w;

	// Bounds and Constants
	static double[] yU;
	static double[] yL;
	static int[] A;
	static int[] B;
	static int[] pb;


	/**
	 * Run the given file with the two models
	 * 
	 * @param args - can accept file name
	 */
	public static void main(String[] args) {
		file = "LN-LD/100_5_false_4";
		// Can get file name as argument
		if (args.length > 0) {
			file = args[0];
		}
		f = new Fractional("problems/fractional/"+file);		

		// Run cplex models
		try {
			cplex = new IloCplex();
			addModelR1();
			cplex.clearModel();
			addModelR4();
		} catch (IloException e) {
			System.err.println("IloException: " + e.getMessage());
		}
	}

	/**
	 * Add model R1 and run with cplex
	 */
	private static void addModelR1() throws IloException {
		int n = f.getN();
		int m = f.getM();
		// update bounds/constants for given problem
		calcR1YBounds();

		// Initialize and name variables
		String[] xname = new String[n];
		String[] yname = new String[m];
		String[][] zname = new String[m][n];
		for (int i = 0; i < n; i++) {
			xname[i] = "x_"+i;
		}
		for (int i = 0; i < m; i++) {
			yname[i] = "y_"+i;
			for (int j = 0; j < n; j++) {
				zname[i][j] = "z_"+i+","+j;
			}
		}
		// x float for LP relaxation
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Float, xname);
		y = new IloNumVar[m];
		z = new IloNumVar[m][];
		for (int i = 0; i < m; i++) {
			y[i] = cplex.numVar(yL[i], yU[i], IloNumVarType.Float,yname[i]);
			z[i] = cplex.numVarArray(n, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, zname[i]);
		}

		// Add objective
		IloNumExpr obj = cplex.numExpr();
		for (int i = 0; i < m; i++) {
			obj = cplex.sum(obj, cplex.prod(f.getNumConst(i),y[i]));
			for (int j = 0; j < n; j++) {
				obj = cplex.sum(obj, cplex.prod(f.getC(i,j),z[i][j]));
			}
		}
		cplex.addMaximize(obj);

		// by + sum(bz) = 1
		for (int i = 0; i < m; i++) {
			IloNumExpr byz = cplex.prod(f.getDenConst(i), y[i]);
			for (int j = 0; j < n; j++) {
				byz = cplex.sum(byz, cplex.prod(f.getD(i,j), z[i][j]));
			}
			cplex.addEq(byz, 1);
		}

		// zij constraints
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				// zij <= yiU*xj
				cplex.addLe(z[i][j], cplex.prod(yU[i], x[j]));
				// zij <= yi + yiL*xj - yiL
				cplex.addLe(cplex.sum(z[i][j],yL[i]), cplex.sum(y[i], cplex.prod(yL[i],x[j])));
				// zij >= yL*xj
				cplex.addGe(z[i][j], cplex.prod(yL[i], x[j]));
				// zij >= yi + yiU*xj - yiU
				cplex.addGe(cplex.sum(z[i][j],yU[i]), cplex.sum(y[i], cplex.prod(yU[i],x[j])));
			}
		}

		// Knapsack constraint
		IloNumExpr knapsack = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			knapsack = cplex.sum(knapsack, cplex.prod(f.getA(i), x[i]));
		}
		cplex.addLe(knapsack, f.getB(), "knapsack");

		cplex.solve();

		bestObj = cplex.getObjValue();
		System.out.println("bestobj: " + cplex.getBestObjValue());
		System.out.println("obj: " + cplex.getObjValue());
		System.out.println("LP Optimal: " + bestObj);
	}

	/**
	 * Update bounds (yU and yL)
	 * 
	 * @throws IloException
	 */
	private static void calcR1YBounds() throws IloException {
		int m = f.getM();
		yU = new double[m];
		yL = new double[m];
		// For each fraction, compute upper and lower bounds on y
		for (int i = 0; i < m; i++) {
			yU[i] = 1;
			yL[i] = 0;
		}
	}

	public static double getBestObj() {
		return bestObj;
	}

	public static boolean getTimeout() {
		return timeout;
	}

	public static double getGap() {
		return gap;
	}

	/**
	 * Add model R4 and solve
	 * 
	 * @throws IloException
	 */
	private static void addModelR4() throws IloException {
		int n = f.getN();
		int m = f.getM();
		// Update bounds/constants for given problem
		calcR4YBounds();

		// Initialize and name variables
		String[] xname = new String[n];
		String[] yname = new String[m];
		String[][] wname = new String[m][n];
		String[][] zname = new String[m][n];
		for (int i = 0; i < n; i++) {
			xname[i] = "x_"+i;
		}
		for (int i = 0; i < m; i++) {
			yname[i] = "y_"+i;
			for (int j = 0; j < n; j++) {
				wname[i][j] = "w_"+i+","+j;
				zname[i][j] = "z_"+i+","+j;
			}
		}
		// x boolean for MIP
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
		y = new IloNumVar[m];
		z = new IloNumVar[m][];
		w = new IloNumVar[m][];
		for (int i = 0; i < m; i++) {
			y[i] = cplex.numVar(yL[i], yU[i], IloNumVarType.Float, yname[i]);
			w[i] = cplex.numVarArray(pb[i], 0, 1, IloNumVarType.Float, wname[i]);
			z[i] = cplex.numVarArray(n, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, zname[i]);
		}

		// Add objective
		IloNumExpr obj = cplex.numExpr();
		for (int i = 0; i < m; i++) {
			obj = cplex.sum(obj,y[i]);
		}
		cplex.addMaximize(obj);

		// (bi0-B)y + 2^k-1*zik = sum(a)
		for (int i = 0; i < m; i++) {
			IloNumExpr byzk = cplex.numExpr();
			byzk = cplex.sum(byzk, cplex.prod((f.getDenConst(i)-B[i]),y[i]));
			for (int j = 0; j < pb[i]; j++) {
				byzk = cplex.sum(byzk, cplex.prod(Math.pow(2, j),z[i][j]));
			}
			IloNumExpr suma = cplex.numExpr();
			suma = cplex.sum(suma,f.getNumConst(i));
			for (int j = 0; j < n; j++) {
				suma = cplex.sum(suma,cplex.prod(f.getC(i,j),x[j]));
			}
			cplex.addEq(byzk, suma);
		}

		// bx+B = 2^k-1*w
		for (int i = 0; i < m; i++) {
			IloNumExpr bxb = cplex.numExpr();
			bxb = cplex.sum(bxb,B[i]);
			for (int j = 0; j < n; j++) {
				bxb = cplex.sum(bxb,cplex.prod(f.getD(i,j),x[j]));
			}
			IloNumExpr wk = cplex.numExpr();
			for (int j = 0; j < pb[i]; j++) {
				wk = cplex.sum(wk, cplex.prod(Math.pow(2, j),w[i][j]));
			}
			cplex.addEq(bxb,wk);
		}

		// z_ik constraints
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < pb[i]; j++) {
				//zik <= yiU*wik
				cplex.addLe(z[i][j], cplex.prod(yU[i],w[i][j]));
				// zik <= yi + yiL*wik - yiL
				cplex.addLe(cplex.sum(z[i][j],yL[i]),cplex.sum(y[i],cplex.prod(yL[i],w[i][j])));
				// zik >= yiL*wik
				cplex.addGe(z[i][j],cplex.prod(yL[i],w[i][j]));
				// zik >= yi + yiU*wik - yiU
				cplex.addGe(cplex.sum(z[i][j],yU[i]),cplex.sum(y[i],cplex.prod(yU[i],w[i][j])));
			}
		}

		// Knapsack constraint
		IloNumExpr knapsack = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			knapsack = cplex.sum(knapsack, cplex.prod(f.getA(i), x[i]));
		}
		cplex.addLe(knapsack, f.getB(), "knapsack");

		// Add relaxed LP solution from R1 model as upper bound
		if (bestObj != 0) {
			IloNumExpr yi = cplex.numExpr();
			for (int i = 0; i < m; i++) {
				yi = cplex.sum(yi,y[i]);
			}
			cplex.addLe(yi,bestObj);
		}

		// Seed MIP with incumbent solution
		FractionalSol inc = new FractionalSol("incumbents/fractional/"+file+"inc.txt");
		ArrayList<Integer> incX = inc.getX();
		seedMIP(incX);

		// Solve solution
		cplex.setParam(IloCplex.DoubleParam.TiLim, 1800);
		cplex.solve();

		System.out.println("Status: " + cplex.getCplexStatus());

		if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
			System.err.println(file + " Timeout");
			timeout = true;
		} else {
			timeout = false;
		}

//		bestObj = cplex.getObjValue();
		gap = cplex.getMIPRelativeGap();

		// Create solution lists from MIP solution
		double[] xvals = new double[n];
		ArrayList<Integer> solX = new ArrayList<Integer>();
		ArrayList<Integer> solR = new ArrayList<Integer>();
		xvals = cplex.getValues(x);
		for (int i = 0; i < n; i++) {
			if (xvals[i] > 1e-05) {
				solX.add(i);
				System.out.println(x[i].getName() + ": " + xvals[i]);
			} else {
				solR.add(i);
			}
		}
		// Check MIP against problem solution and problem objectives
		FractionalSol fs = new FractionalSol(solX,solR);
		double fObj = f.getObj(solX,false);
		bestObj = fObj;
		if (fs.getObj() != bestObj || fs.getObj() != fObj) {
			System.err.println("Best Obj: " + bestObj);
			System.err.println("Different fs obj: " + fs.getObj());
			System.err.println("FObj: " + fObj);
		}
	}

	/**
	 * Seed cplex with the given MIP solution
	 * 
	 * @param initX - solution list to seed to cplex
	 * @throws IloException
	 */
	static private void seedMIP(ArrayList<Integer> initX) throws IloException {
		// New solution to be passed in to MIP.
		IloNumVar[] iniX = cplex.numVarArray(initX.size(),0,1,IloNumVarType.Bool);
		double[] values = new double[initX.size()];
		for (int i = 0; i < initX.size(); i++) {
			int xi = initX.get(i);
			System.out.println("x_"+xi);
			iniX[i] = x[xi];
			values[i] = 1;
		}
		cplex.addMIPStart(iniX,values,"initSol");
	}

	/**
	 * Update bounds (yU,yL)
	 *  and constants (Ai,Bi,Pbi)
	 */
	private static void calcR4YBounds() {
		int n = f.getN();
		int m = f.getM();
		yU = new double[m];
		yL = new double[m];
		for (int i = 0; i < m; i++) {
			yL[i] = 0;
			yU[i] = 0;
			for (int j = 0; j < n; j++) {
				yU[i] += f.getC(i,j);
			}
			//			yU[i] = yU[i] / f.getDenConst(i);
		}
		A = new int[m];
		B = new int[m];
		pb = new int[m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				int cij = f.getC(i,j);
				int dij = f.getD(i,j);
				if (cij < 0) {
					A[i] += Math.abs(cij);
				}
				if (dij < 0) {
					B[i] += Math.abs(dij);
				}
				pb[i] += Math.abs(dij);
			}
			pb[i] = (int) Math.floor(log2nlz(pb[i]))+1;
		}
	}

	/**
	 * Log base 2 of the input number
	 * 
	 * Code taken from: 
	 * http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
	 * 
	 * @param bits - number to take the log of
	 * @return log_2(bits)
	 */
	public static int log2nlz( int bits ) {
		if( bits == 0 )
			return 0; // or throw exception
		return 31 - Integer.numberOfLeadingZeros( bits );
	}
}
