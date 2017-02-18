package ExactMethods;
// Cplex imports
import ilog.concert.*;
import ilog.cplex.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import Problems.Unconstrained;

/**
 * Givry MIP formulation for 3-SAT
 * 
 * @author midkiffj
 */
public class UnconstrainedGivry {

	private static String file = "f/f1000.cnf";

	static int n;
	static Unconstrained u;
	static Random rnd;
	static IloCplex cplex;

	static IloNumVar[] x;
	static IloNumVar[] y;

	static boolean exportLPs = true;

	/*
	 * Setup MIP and run
	 * @param args
	 */
	public static void main(String[] args) {
		u = new Unconstrained(5,true);
		try {
			cplex = new IloCplex();
			buildSAT();
		} catch (IloException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/*
	 * Add the givry formulation to cplex and solve
	 */
	public static void buildSAT() throws FileNotFoundException, IloException {
		Scanner scr = new Scanner(new FileInputStream(file));

		// Ignore DIMAC cnf header
		String line = scr.nextLine();
		while (!(line.substring(0, 1).equals("p"))) {
			line = scr.nextLine();
		} 

		String[] split = line.split(" ");
		int n = Integer.parseInt(split[2]);
		int constraints = Integer.parseInt(split[3]);
		int i,j,k;

		// Initialize and Name Variables
		String[] xname = new String[n];
		String[] yname = new String[constraints];
		for (i = 0; i < n; i++) {
			xname[i] = "x_"+i;
		}
		for (i = 0; i < constraints; i++) {
			yname[i] = "y_"+i;
		}
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
		y = cplex.numVarArray(constraints,0,1, IloNumVarType.Bool, yname);

		// Read in and add Constraints
		for (i = 0; i < constraints; i++) {
			line = scr.nextLine();
			split = line.split(" ");

			IloNumExpr clause = cplex.numExpr();
			for (j = 0; j < 3; j++) {
				k = Integer.parseInt(split[j]);
				if (k != 0) {
					if (k < 0) {
						k = (k*-1)-1;
						clause = cplex.sum(clause, 1);
						clause = cplex.sum(clause, cplex.prod(-1,x[k]));
					} else {
						k = k - 1;
						clause = cplex.sum(clause, x[k]);
					}
				} else {
					j = 3;
				}
			}
			clause = cplex.sum(clause, y[i]);
			cplex.addGe(clause, 1);
		}
		scr.close();

		// Add objective
		IloNumExpr obj = cplex.numExpr();
		for (i = 0; i < constraints; i++) {
			obj = cplex.sum(obj, y[i]);
		}
		cplex.addMinimize(obj);

		// Export LP file.
		if (exportLPs) {
			cplex.exportModel("uncGivry.lp");
		}

		// Solve Model
		cplex.solve();
		double IPOptimal = cplex.getObjValue();

		// Print Integral solution
		System.out.println("Model Status: " + cplex.getCplexStatus());
		System.out.println("IPOptimal: " + IPOptimal);
		prettyPrintInOrder();

		System.exit(0);
	}

	/*
	 * Seed the MIP with the given solution
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

	/*
	 * Print the solution x values
	 */
	private static void prettyPrintInOrder() {
		// Pretty Print solution once complete.
		int i;
		// Get x_ij values
		double[] xvals = new double[n];
		try {
			xvals = cplex.getValues(x);
		} catch (IloException e) {
			System.err.println("Error retrieving x values");
		}
		for (i = 0; i < n; i++) {
			System.out.println("x_"+i+": " + xvals[i]);
		}
	}
}
