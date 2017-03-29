package ExactMethods;

import java.util.ArrayList;

import Problems.MaxProbability;
import Solutions.MaxProbabilitySol;
import ilog.concert.*;
import ilog.cplex.*;

/**
 * Run the Max Probability MIP formulation of Billionnet
 * @author midkiffj
 *
 */
public class MaxProb_Bill {

	static IloCplex cplex;
	static IloNumVar p;
	static IloNumVar[] x;
	static IloNumVar[] y;
	static IloNumVar[] z;
	static IloNumVar W;
	static IloNumVar W2;

	static MaxProbability mp;
	static double pUpper;
	static double delta;
	static double B2;
	static double B1;
	static double p1;

	static double bestObj;
	static double gap;
	static boolean timeout;
	static String file;

	/**
	 * Setup Max Prob problem and MIP
	 * 
	 * @param args - can take file name
	 */
	public static void main(String[] args) {
		// Can take file as argument
		file = "1000_P5_K95_0";
		if (args.length == 1) {
			file = args[0];
		}
		mp = new MaxProbability("problems/mp/"+file);
		try {
			calcPUpper();
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Calculate needed constants and upper bound on rho(p)
	 * 
	 * @throws IloException
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
		Knapsack_Frac ks = new Knapsack_Frac(a,b,c,true);
		long umax = ks.getBestObj();
		boolean[] xVals = ks.getXVals();

		// Setup and run X_vmin
		IloCplex minVar = new IloCplex();
		String[] xname = new String[n];
		for (int k = 0; k < n; k++) {
			xname[k] = "x_"+k;
		}
		x = minVar.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);

		// Use variances as objective
		IloNumExpr vars = minVar.numExpr();
		for (int i = 0; i < n; i++) {
			vars = minVar.sum(vars,minVar.prod(mp.getS(i),x[i]));
		}
		minVar.addMinimize(vars);

		// Add sum(ux) >= t constraint
		IloNumExpr uGTt = minVar.numExpr();
		for(int i = 0; i < n; i++) {
			uGTt = minVar.sum(uGTt,minVar.prod(mp.getU(i),x[i]));
		}
		minVar.addGe(uGTt, mp.getT());

		// Solve
		minVar.exportModel("maxProbMinVar.lp");
		minVar.solve();
		System.err.println("MinVar Obj: " + minVar.getObjValue());

		// Calculate constants and bounds
		int den1 = 0;
		for (int i = 0; i < n; i++) {
			if (xVals[i]) {
				den1 += mp.getS(i);
			}
		}
		delta = Math.abs(mp.getT() - umax);
		p1 = (delta*delta) / den1;
		B1 = delta * Math.sqrt(minVar.getObjValue()/den1);
		B2 = ((delta*delta)/minVar.getObjValue());
		pUpper = B2;
	}

	/**
	 * Add the MPP model of Billionnet(2004) to cplex
	 * @throws IloException
	 */
	private static void addModel() throws IloException {
		int n = mp.getN();

		// Initialize and name variables
		String[] xname = new String[n];
		String[] yname = new String[n];
		String[] zname = new String[n];
		for (int k = 0; k < n; k++) {
			xname[k] = "x_"+k;
			yname[k] = "y_"+k;
			zname[k] = "z_"+k;
		}
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
		y = cplex.numVarArray(n, 0, Double.MAX_VALUE, IloNumVarType.Float, yname);
		z = cplex.numVarArray(n, 0, Double.MAX_VALUE, IloNumVarType.Float, zname);

		p = cplex.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "p");
		W = cplex.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "W");
		W2 = cplex.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "W2");

		// Constraint C1
		IloNumExpr wEQ = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			wEQ = cplex.sum(wEQ,cplex.prod(mp.getU(i),x[i]));
		}
		wEQ  = cplex.sum(W,cplex.prod(-1, wEQ));
		cplex.addEq(wEQ, -1*mp.getT());

		// Constraint C2
		IloNumExpr w2EQ1 = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			w2EQ1 = cplex.sum(w2EQ1,cplex.prod(B1*mp.getU(i),x[i]));
			w2EQ1 = cplex.sum(w2EQ1,cplex.prod(mp.getU(i),y[i]));
		}
		w2EQ1 = cplex.sum(cplex.prod(-1*mp.getT(),W),w2EQ1);
		cplex.addEq(W2,w2EQ1);

		// Constraint C3
		IloNumExpr w2EQ2 = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			w2EQ2 = cplex.sum(w2EQ2,cplex.prod(p1*mp.getS(i),x[i]));
			w2EQ2 = cplex.sum(w2EQ2,cplex.prod(mp.getS(i),z[i]));
		}
		cplex.addEq(W2,w2EQ2);

		// Constraint C4
		for (int i = 0; i < n; i++) {
			cplex.addLe(cplex.sum(y[i],cplex.prod(-1,W)), -1*B1);
		}

		// Constraint C5
		for (int i = 0; i < n; i++) {
			cplex.addLe(y[i],cplex.prod(delta-B1,x[i]));
		}

		// Constraint C6
		for (int i = 0; i < n; i++) {
			cplex.addLe(z[i],cplex.prod(B2,x[i]));
		}

		// Constraint C7
		for (int i = 0; i < n; i++) {
			cplex.addLe(cplex.sum(p,cplex.prod(-1,z[i]),cplex.prod(B2,x[i])),B2);
		}

		// Knapsack constraint
		IloNumExpr aLTb = cplex.numExpr();
		for (int i = 0; i < n; i++) {
			aLTb = cplex.sum(aLTb, cplex.prod(mp.getA(i),x[i]));
		}
		cplex.addLe(aLTb, mp.getB());

		// Objective
		cplex.addMaximize(cplex.sum(p,p1));

		// Export LP
		cplex.exportModel("maxProbBill.lp");

		// Solve and print solution
		cplex.setParam(IloCplex.DoubleParam.TiLim, 1800);
		cplex.solve();

		if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
			System.err.println(file + " Timeout");
			timeout = true;
		}
		
		try {
			bestObj = cplex.getObjValue();
			System.out.println(bestObj);
			gap = cplex.getMIPRelativeGap();

			printVars();
			
			// Create solution lists from MIP solution
			double[] xvals = new double[n];
			ArrayList<Integer> solX = new ArrayList<Integer>();
			ArrayList<Integer> solR = new ArrayList<Integer>();
			xvals = cplex.getValues(x);
			for (int i = 0; i < n; i++) {
				if (xvals[i] > 1e-05) {
					solX.add(i);
				} else {
					solR.add(i);
				}
			}
			// Check MIP against problem solution and problem objectives
			MaxProbabilitySol mps = new MaxProbabilitySol(solX,solR);
			double mpObj = mp.getObj(solX);
			if (mps.getObj() != bestObj || mps.getObj() != mpObj) {
				System.err.println("Different mps obj: " + mps.getObj());
				System.err.println("MPObj: " + mpObj);
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			bestObj = -1;
			gap = -1;
		}
	}

	/**
	 * Pretty Print solution variables to x, y, z, W, W2, and p
	 * 
	 * @throws IloException
	 */
	static void printVars() throws IloException {
		int n = mp.getN();

		// Print x's
		double[] xvals = new double[n];
		xvals = cplex.getValues(x);
		for (int i = 0; i < n; i++) {
			System.out.println("x_"+i+": " + xvals[i]);
		}

		// Print y's
		double[] yvals = new double[n];
		yvals = cplex.getValues(y);
		for (int i = 0; i < n; i++) {
			System.out.println("y_"+i+": " + yvals[i]);
		}

		// Print z's
		double[] zvals = new double[n];
		zvals = cplex.getValues(z);
		for (int i = 0; i < n; i++) {
			System.out.println("z_"+i+": " + zvals[i]);
		}

		// Print W's
		double w = cplex.getValue(W);
		System.out.println("W: " + w);
		double w2 = cplex.getValue(W2);
		System.out.println("W2: " + w2);

		// Print p
		double P = cplex.getValue(p);
		System.out.println(P);
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
}
