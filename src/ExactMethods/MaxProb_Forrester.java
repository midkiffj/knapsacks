package ExactMethods;

import Problems.MaxProbability;
import ilog.concert.*;
import ilog.cplex.*;

/**
 * Run the Max Probability MIP of Forrester
 * @author midkiffj
 *
 */
public class MaxProb_Forrester {

	static IloCplex cplex;
	static IloNumVar p;
	static IloNumVar[] x;
	static IloNumVar[] z;
	static IloNumVar[] s;

	static MaxProbability mp;
	static double pUpper;
	static double[] U;
	static double[] L;

	/*
	 * Setup Max Prob problem and run MIP
	 */
	public static void main(String[] args) {
		mp = new MaxProbability("problems/mp/P5_K65_0");
		try {
			cplex = new IloCplex();
			// Choose one model to use
			//  addModelZj();
			addModelSj();
		} catch (IloException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/*
	 * Setup second formulation of Forrester using s_j
	 */
	private static void addModelSj() throws IloException {
		// Calculate bounds
		int n = mp.getN();
		calcPUpper();
		calcLU();
		
		// Add Objective Function
		p = cplex.numVar(0, pUpper, IloNumVarType.Float, "p");
		cplex.addMaximize(p);


		// Initialize and name variables
		String[] xname = new String[n];
		String[] sname = new String[n];
		for (int i = 0; i < n; i++) {
			xname[i] = "x_"+i;
			sname[i] = "s_"+i;
		}
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
		s = cplex.numVarArray(n, 0, Double.POSITIVE_INFINITY, IloNumVarType.Float, sname);


		// Knapsack constraint
		IloNumExpr aLTb = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			aLTb = cplex.sum(aLTb, cplex.prod(mp.getA(i),x[i]));
		}
		cplex.addLe(aLTb, mp.getB());

		// t constraint
		IloNumExpr uxGTt = cplex.numExpr(); 
		for (int i = 0; i < n; i++) {
			uxGTt = cplex.sum(uxGTt, cplex.prod(mp.getU(i),x[i]));
		}
		cplex.addGe(uxGTt, mp.getT());

		// Ux-sj >= -t^2
		IloNumExpr zGTt = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			zGTt = cplex.sum(zGTt,cplex.prod(U[i],x[i]),cplex.prod(-1,s[i]));
		}
		cplex.addGe(zGTt, -1*(mp.getT()*mp.getT()));

		// sj constraint
		for (int i = 0; i < n; i++) {
			IloNumExpr sj = cplex.numExpr();
			int u = mp.getU(i);
			sj = cplex.sum(sj, s[i], cplex.prod(-1*U[i],x[i]), cplex.prod(L[i],x[i]));
			double RHS = L[i] - (u*u) + (2*mp.getT()*u);
			IloNumExpr uuxp = cplex.numExpr();
			for(int j = 0; j < i; j++) {
				uuxp = cplex.sum(uuxp,cplex.prod(u*mp.getU(j), x[j]));
			}
			for(int j = i+1; j < n; j++) {
				uuxp = cplex.sum(uuxp,cplex.prod(u*mp.getU(j), x[j]));
			}
			uuxp = cplex.sum(uuxp,cplex.prod(-1*mp.getS(i), p));
			sj = cplex.sum(sj, uuxp);
			cplex.addGe(sj, RHS);
		}

		// Export LP and Solve
		cplex.exportModel("maxProbForrester.lp");
		cplex.solve();
		
		// Pretty Print solution
		System.out.println("Optimal: " + cplex.getObjValue());
		double[] xvals = new double[n];
		xvals = cplex.getValues(x);
		for (int i = 0; i < n; i++) {
			System.out.println("x_"+i+": " + xvals[i]);
		}
	}

	/*
	 * Setup first formulation using z_j
	 */
	private static void addModelZj() throws IloException {
		// Calculate bounds
		int n = mp.getN();
		calcPUpper();
		calcLU();
		
		// Add Objective Function
		p = cplex.numVar(0, pUpper, IloNumVarType.Float, "p");
		cplex.addMaximize(p);

		// Initialize other vars
		String[] xname = new String[n];
		String[] zname = new String[n];
		for (int i = 0; i < n; i++) {
			xname[i] = "x_"+i;
			zname[i] = "z_"+i;
		}
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
		z = cplex.numVarArray(n, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, zname);

		// Knapsack constraint
		IloNumExpr aLTb = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			aLTb = cplex.sum(aLTb, cplex.prod(mp.getA(i),x[i]));
		}
		cplex.addLe(aLTb, mp.getB());

		// t constraint
		IloNumExpr uxGTt = cplex.numExpr(); 
		for (int i = 0; i < n; i++) {
			uxGTt = cplex.sum(uxGTt, cplex.prod(mp.getU(i),x[i]));
		}
		cplex.addGe(uxGTt, mp.getT());

		// z >= -t^2
		IloNumExpr zGTt = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			zGTt = cplex.sum(zGTt,z[i]);
		}
		cplex.addGe(zGTt, -1*(mp.getT()*mp.getT()));

		// z <= Ux
		for (int i = 0; i < n; i++) {
			IloNumExpr zLTU = cplex.numExpr();
			zLTU = cplex.sum(zLTU,z[i],cplex.prod(-1*U[i],x[i]));
			cplex.addLe(zLTU, 0);
		}

		// z <= h - L(1-x)
		for (int i = 0; i < n; i++) {
			IloNumExpr zLTh = cplex.numExpr();
			int u = mp.getU(i);
			double RHS = (-1*L[i]) + (u*u) - (2*mp.getT()*u);
			for(int j = 0; j < i; j++) {
				zLTh = cplex.sum(zLTh,cplex.prod(-1*u*mp.getU(j), x[j]));
			}
			for(int j = i+1; j < n; j++) {
				zLTh = cplex.sum(zLTh,cplex.prod(-1*u*mp.getU(j), x[j]));
			}
			zLTh = cplex.sum(zLTh,z[i],cplex.prod(-1*L[i], x[i]),cplex.prod(mp.getS(i),p));
			cplex.addLe(zLTh, RHS);
		}

		// Export LP and solve
		cplex.exportModel("maxProbForrester.lp");
		cplex.solve();
		
		// Pretty print solution
		System.out.println("Optimal: " + cplex.getObjValue());
		double[] xvals = new double[n];
		xvals = cplex.getValues(x);
		for (int i = 0; i < n; i++) {
			System.out.println("x_"+i+": " + xvals[i]);
		}
	}

	/*
	 * Calculate constants and bounds for formulation
	 */
	private static void calcPUpper() throws IloException {
		int n = mp.getN();
		
		// Solve for X_umax
		int[] a = new int[n];
		int b = mp.getB();
		int[] c = new int[n];
		for (int i = 0; i < n; i++) {
			a[i] = mp.getA(i);
			c[i] = mp.getU(i);
		}
		Knapsack ks = new Knapsack(a,b,c);
		long umax = ks.getBestObj();
		boolean[] xVals = ks.getXVals();

		// Solve for X_vmin
		IloCplex minVar = new IloCplex();
		String[] xname = new String[n];
		for (int k = 0; k < n; k++) {
			xname[k] = "x_"+k;
		}
		x = minVar.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);

		// Use variance as objective
		IloNumExpr vars = minVar.numExpr();
		for (int i = 0; i < n; i++) {
			vars = minVar.sum(vars,minVar.prod(mp.getS(i),x[i]));
		}
		minVar.addMinimize(vars);

		// t constraint
		IloNumExpr uGTt = minVar.numExpr();
		for(int i = 0; i < n; i++) {
			uGTt = minVar.sum(uGTt,cplex.prod(mp.getU(i),x[i]));
		}
		minVar.addGe(uGTt, mp.getT());

		// Export Model and Solve
		minVar.exportModel("maxProbMinVar.lp");
		minVar.solve();
		System.out.println("MinVar Obj: " + minVar.getObjValue());

		// Calculate constants
		int den1 = 0;
		for (int i = 0; i < n; i++) {
			if (xVals[i]) {
				den1 += mp.getS(i);
			}
		}
		double deltaConst = Math.abs(mp.getT() - umax);
		double rho1Const = (deltaConst*deltaConst) / den1;
		double B2Const = ((deltaConst*deltaConst)/minVar.getObjValue()) - rho1Const;
		pUpper = B2Const;
	}

	/*
	 * Find lower and upper bounds on z_j
	 */
	private static void calcLU() throws IloException {
		int n = mp.getN();
		double t = mp.getT();

		U = new double[n];
		L = new double[n];

		for (int i = 0; i < n; i ++) {
			IloCplex bounds = new IloCplex();
			IloNumExpr hj = bounds.numExpr();
			p = bounds.numVar(0, pUpper, IloNumVarType.Float, "p");


			// Initialize variables
			String[] xname = new String[n];
			for (int k = 0; k < n; k++) {
				xname[k] = "x_"+k;
			}
			x = bounds.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);

			int u = mp.getU(i);
			int s = mp.getS(i);

			// Calculate hj from Forrester linearization
			double h = (u*u) - (2*t*u);
			hj = bounds.sum(hj, h);
			for(int j = 0; j < i; j++) {
				hj = bounds.sum(hj, bounds.prod(u*mp.getU(j),x[j]));
			}
			for(int j = i+1; j < n; j++) {
				hj = bounds.sum(hj, bounds.prod(u*mp.getU(j),x[j]));
			}
			hj = bounds.sum(hj, bounds.prod(-1*s,p));

			// U_i = max{hj}
			bounds.addMaximize(hj);
			bounds.solve();
			U[i] = bounds.getObjValue();

			bounds.clearModel();

			// L_i = min{hj}
			bounds.addMinimize(hj);
			bounds.solve();
			L[i] = bounds.getObjValue();

			System.out.println("U: " + U[i] + " L: " + L[i]);
		}
	}

}
