package ExactMethods;

import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;
import Problems.Fractional;
import Solutions.FractionalSol;

public class Fractional_Borrero {

	private static String file;
	private static Fractional f;
	private static IloCplex cplex;

	// Cplex vars
	static IloNumVar[] x;
	static IloNumVar[] y;
	static IloNumVar[][] z;

	// Bounds and Constants
	static double[] yU;
	static double[] yL;
	static int[] A;
	static int[] B;

	public static void main(String[] args) {
		file = "1000_5_false_0";
		f = new Fractional("problems/fractional/"+file);

		try {
			cplex = new IloCplex();
			addModelR1();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addModelR1() throws IloException {
		int n = f.getN();
		int m = f.getM();
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
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
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

		ArrayList<Integer> solX = new ArrayList<Integer>();
		ArrayList<Integer> solR = new ArrayList<Integer>();

		// Pretty Print solution
		double[] xvals = new double[n];
		xvals = cplex.getValues(x);
		for (int i = 0; i < n; i++) {
			System.out.println("x_"+i+": " + xvals[i]);
			if (xvals[i] > 0) {
				solX.add(i);
			} else {
				solR.add(i);
			}
		}
		System.out.println("MIP Optimal: " + cplex.getObjValue());
		FractionalSol fs = new FractionalSol(solX,solR);
		System.out.println("Fractional Obj: " + fs.getObj());
		System.out.println("Valid: " + fs.getValid());
	}

	private static void calcR1YBounds() throws IloException {
		int n = f.getN();
		int m = f.getM();
		yU = new double[m];
		yL = new double[m];

		for (int i = 0; i < m; i++) {
			IloCplex bounds = new IloCplex();
			x = bounds.numVarArray(n, 0, 1, IloNumVarType.Bool);
			IloNumVar u = bounds.numVar(1,Integer.MAX_VALUE,IloNumVarType.Float);
			bounds.addMinimize(u);

			IloNumExpr bs = bounds.numExpr();
			for (int j = 0; j < n; j++) {
				bs = bounds.sum(bs,bounds.prod(f.getD(i,j),x[j]));
			}
			bs = bounds.sum(bs,f.getDenConst(i));
			bounds.addEq(u, bs);

			bounds.solve();
			System.out.println(bounds.getObjValue());
			yU[i] = (double)1/bounds.getObjValue();

			bounds.clearModel();
			IloNumVar l = bounds.numVar(Integer.MIN_VALUE,-1,IloNumVarType.Float);
			bounds.addMaximize(l);

			bounds.addEq(l, bs);
			bounds.solve();

			if (bounds.getCplexStatus() == IloCplex.CplexStatus.Infeasible) {
				l.setUB(Integer.MAX_VALUE);
				bounds.solve();
			}
			System.out.println(bounds.getObjValue());
			yL[i] = (double)1/bounds.getObjValue();
		}
	}
}
