package Sat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import ExactMethods.UnconstrainedStd;
import ExactMethods.Unconstrained_Forrester;
import Heuristics.tabuSearch;
import Problems.Unconstrained;
import Runner.TestLogger;
import Runner.TestLogger.sf;
import Solutions.UnconstrainedSol;

public class Sat2Cubic {

//	private static String file = "C:/Users/midkiffj/Downloads/aim/aim-50-1_6-yes1-1.cnf";
	private static String file = "C:/Users/midkiffj/Downloads/f/f600.cnf";

	private static int c;
	private static int[] ci;
	private static int[][] cij;
	private static int[][][] dijk;

	public static void main(String[] args) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(file));

			String line = scr.nextLine();
			while (!(line.substring(0, 1).equals("p"))) {
				line = scr.nextLine();
			} 

			String[] split = line.split(" ");
			int n = Integer.parseInt(split[2]);
			int constraints = Integer.parseInt(split[3]);

			// Define objective coefficient matrices
			ci = new int[n];
			cij = new int[n-1][];
			dijk = new int[n-1][][];
			for(int i = 0; i < n; i++){
				if (i < n-1) {
					cij[i] = new int[n-i];
					dijk[i] = new int[n-i][];
				}
				for (int j = i+1; j < n; j++){
					dijk[i][j-i] = new int[n-j];
				}
			}

			for (int i = 0; i < constraints; i++) {
				line = scr.nextLine();
				split = line.split(" ");
				ArrayList<Integer> vars = new ArrayList<Integer>();
				for (int j = 0; j < 3; j++) {
					int k = Integer.parseInt(split[j]);
					if (k != 0) {
						vars.add(k);
					} else {
						j = 3;
					}
				}				
				translate(vars);
			}

			Unconstrained unc = new Unconstrained(c, ci, cij, dijk);
//			Unconstrained_Forrester.run(unc);
//			unc.toFile(file+".cubic");
			
			for(Handler h: TestLogger.logger.getHandlers()) {
				TestLogger.logger.removeHandler(h);
			}
			ConsoleHandler ch = new ConsoleHandler();
			TestLogger.logger.setUseParentHandlers(false);
			TestLogger.logger.addHandler(ch);
			sf formatter = new sf();  
	        ch.setFormatter(formatter);
			
			UnconstrainedSol us = new UnconstrainedSol();
			
			tabuSearch h = new tabuSearch(us, -1, -1);
			h.run();

			scr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void prettyPrint() {
		int n = ci.length;
		
		System.out.println(c);
		for (int i = 0; i < n; i++) {
			if (ci[i] != 0) {
				System.out.print(ci[i] + "x"+i+" ");
			}
		}
		System.out.println();

		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				if (cij[i][j-i] != 0) {
					System.out.print(cij[i][j-i] + "x"+i+","+j+" ");
				}
			}
		}
		
		System.out.println();

		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				for (int k = j+1; k < n; k++) {
					if (dijk[i][j-i][k-j] != 0) {
						System.out.print(dijk[i][j-i][k-j] + "x"+i+","+j+","+k+" ");
					}
				}
			}
		}
	}
	
	private static void translate(ArrayList<Integer> vars) {
		Collections.sort(vars);
		if (vars.size() == 3) {
			int k = vars.get(0);
			int j = vars.get(1);
			int i = vars.get(2);

			if (k > 0) {

				c++;
				updateDijk(i,j,k,-1);
				updateCi(i,-1);
				updateCi(j,-1);
				updateCi(k,-1);
				updateCij(i,j,1);
				updateCij(i,k,1);
				updateCij(j,k,1);
				//				dijk[i][j][k] = dijk[i][j][k] - 1;
				//				ci[i] = ci[i] - 1;
				//				ci[j] = ci[j] - 1;
				//				ci[k] = ci[k] - 1;
				//				cij[i][j] = cij[i][j] + 1;
				//				cij[i][k] = cij[i][k] + 1;
				//				cij[j][k] = cij[j][k] + 1;

			} else if (j > 0) {

				k = k*-1;
				updateCi(k,1);
				updateCij(i,k,-1);
				updateCij(j,k,-1);
				updateDijk(i,j,k,1);
				//				ci[k] = ci[k] + 1;
				//				cij[i][k]-= cij[i][k] - 1;
				//				cij[j][k] = cij[j][k] - 1;
				//				dijk[i][j][k] = dijk[i][j][k] + 1;

			} else if (i > 0) {

				k = k*-1;
				j = j*-1;
				updateCij(j,k,1);
				updateDijk(i,j,k,-1);
				//				cij[j][k] = cij[j][k] + 1;
				//				dijk[i][j][k] = dijk[i][j][k] - 1;

			} else {

				k = k*-1;
				j = j*-1;
				i = i*-1;
				updateDijk(i,j,k,1);
				//				dijk[i][j][k] = dijk[i][j][k] + 1;

			}


		} else if (vars.size() == 2) {
			int i = vars.get(0);
			int j = vars.get(1);

			if (i > 0) {

				c++;
				updateCi(i,-1);
				updateCi(j,-1);
				updateCij(i,j,1);
				//				ci[i] = ci[i] - 1;
				//				ci[j] = ci[j] - 1;
				//				cij[i][j] = cij[i][j] + 1;

			} else if (j > 0) {

				i = i*-1;
				updateCi(i,1);
				updateCij(i,j,-1);
				//				ci[i] = ci[i] + 1;
				//				cij[i][j] = cij[i][j] - 1;

			} else {

				i = i*-1;
				j = j*-1;
				updateCij(i,j,1);
				cij[i][j] = cij[i][j] + 1;

			}

		}
	}

	public static void updateCi(int i, int change) {
		i = i - 1;
		ci[i] = ci[i] + change;
	}

	public static void updateCij(int i, int j, int change) {
		i = i - 1;
		j = j - 1;
		if (i == j) {
			System.err.println("i==j for: " + i);
		} else if (i < j) {
			cij[i][j-i] = cij[i][j-i] + change;
		} else {
			cij[j][i-j] = cij[j][i-j] + change;
		}
	}

	public static void updateDijk(int i, int j, int k, int change) {
		i = i - 1;
		j = j - 1;
		k = k - 1;
		if (i == j || i == k || j == k)  {
			System.err.println("i==j==k for: " + i+","+j+","+k);
		}
		if (i < j) {
			if (j < k) {
				dijk[i][j-i][k-j] = dijk[i][j-i][k-j] + change;
			} else if (k < i) {
				dijk[k][i-k][j-i] = dijk[k][i-k][j-i] + change;
			} else {
				dijk[i][k-i][j-k] = dijk[i][k-i][j-k] + change;
			}
		} else if (j < i) {
			if (i < k) {
				dijk[j][i-j][k-i] = dijk[j][i-j][k-i] + change;
			} else if (k < j) {
				dijk[k][j-k][i-j] = dijk[k][j-k][i-j] + change;
			} else {
				dijk[j][k-j][i-k] = dijk[j][k-j][i-k] + change;
			}
		} else {
			System.err.println("Unable to update Dijk");
		}
	}
}
