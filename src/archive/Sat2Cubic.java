package archive;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Sat2Cubic {

	// File to read
	private static String file = "C:/Users/midkiffj/Downloads/aim/aim-50-1_6-no-1.cnf";

	// Coefficient arrays
	private static int c;
	private static int[] ci;
	private static int[][] cij;
	private static int[][][] dijk;

	public static void main(String[] args) {
		Scanner scr;
		try {
			// Initialize scanner
			scr = new Scanner(new FileInputStream(file));

			// Skip header until problem definition
			// -- until first character in string is 'p'
			String line = scr.nextLine();
			while (!(line.substring(0, 1).equals("p"))) {
				line = scr.nextLine();
			} 

			// Split the problem line for the problem size and number of coefficients
			String[] split = line.split(" ");
			int n = Integer.parseInt(split[2])+1; // Ignore 0 row [1,n]
			int constraints = Integer.parseInt(split[3]);

			// Define objective coefficient matrices
			ci = new int[n];
			cij = new int[n][n];
			dijk = new int[n][n][n];

			// Read in each line
			for (int i = 0; i < constraints; i++) {
				line = scr.nextLine();
				split = line.split(" ");
				ArrayList<Integer> vars = new ArrayList<Integer>();
				// Add each number to a list
				for (int j = 0; j < 3; j++) {
					int k = Integer.parseInt(split[j]);
					if (k != 0) {
						vars.add(k);
					} else {
						j = 3;
					}
				}				
				// Translate numbers into coefficients
				translate(vars);
			}

			
			// Pretty Print -- unnecessary
			System.out.println(c);
			for (int i = 0; i < n; i++) {
				System.out.print(ci[i] + "x"+i+" ");
			}
			System.out.println();

			for (int i = 0; i < n; i++) {
				System.out.print(i+": ");
				for (int j = 0; j < n; j++) {
					System.out.print(cij[i][j] + "x"+i+","+j+" ");
				}
				System.out.println();
			}
			
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					System.out.print(i+","+j+": ");
					for (int k = 0; k < n; k++) {
						System.out.print(dijk[i][j][k] + "x"+i+","+j+","+k+" ");
					}
					System.out.println();
				}
				System.out.println();
			}

			scr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Translate a list of positive and negative variables 
	 * 	into penalty function constraint coefficients
	 * 
	 * @param vars
	 */
	private static void translate(ArrayList<Integer> vars) {
		// Sort the numbers
		Collections.sort(vars);
		
		// If 3-SAT
		if (vars.size() == 3) {
			int k = vars.get(0);
			int j = vars.get(1);
			int i = vars.get(2);

			// xi, xj, xk
			if (k > 0) {
				// Update coefficients
				c++;
				updateDijk(i,j,k,-1);
				updateCi(i,-1);
				updateCi(j,-1);
				updateCi(k,-1);
				updateCij(i,j,1);
				updateCij(i,k,1);
				updateCij(j,k,1);
			} 
			// xi, xj, ~xk
			else if (j > 0) {
				// Negate for index
				k = k*-1;
				// Update coefficients
				updateCi(k,1);
				updateCij(i,k,-1);
				updateCij(j,k,-1);
				updateDijk(i,j,k,1);
			} 
			// xi, ~xj, ~xk
			else if (i > 0) {
				// Negate for index
				k = k*-1;
				j = j*-1;
				// Update coefficients
				updateCij(j,k,1);
				updateDijk(i,j,k,-1);
			} 
			// ~xi, ~xj, ~xk
			else {
				// Negate for index
				k = k*-1;
				j = j*-1;
				i = i*-1;
				// Update coefficients
				updateDijk(i,j,k,1);
			}
		} 
		// Else if 2-SAT (or sample problem is incorrect...)
		else if (vars.size() == 2) {
			int i = vars.get(0);
			int j = vars.get(1);

			// xi, xj
			if (i > 0) {
				// Update coefficients
				c++;
				updateCi(i,-1);
				updateCi(j,-1);
				updateCij(i,j,1);
			} 
			// ~xi, xj
			else if (j > 0) {
				// Negate for index
				i = i*-1;
				// Update coefficients
				updateCi(i,1);
				updateCij(i,j,-1);
			} 
			// ~xi, ~xj
			else {
				// Negate for index
				i = i*-1;
				j = j*-1;
				// Update coefficients
				updateCij(i,j,1);
				cij[i][j] = cij[i][j] + 1;
			}
		}
	}
	
	/*
	 * Update functions used to change the coefficients of the cubic function
	 * -- Used to keep i < j < k (upper triangular)
	 * 
	 * @param i,j,k the indexes of the coefficients to change
	 * @param change the value to increment the coefficients by
	 */
	
	public static void updateCi(int i, int change) {
		ci[i] = ci[i] + change;
	}

	public static void updateCij(int i, int j, int change) {
		if (i == j) {
			System.err.println("i==j for: " + i);
		} else if (i < j) {
			cij[i][j] = cij[i][j] + change;
		} else {
			cij[j][i] = cij[j][i] + change;
		}
	}

	public static void updateDijk(int i, int j, int k, int change) {
		if (i == j || i == k || j == k)  {
			System.err.println("i==j==k for: " + i+","+j+","+k);
		}
		if (i < j) {
			if (j < k) {
				dijk[i][j][k] = dijk[i][j][k] + change;
			} else if (k < i) {
				dijk[k][i][j] = dijk[k][i][j] + change;
			} else {
				dijk[i][k][j] = dijk[i][k][j] + change;
			}
		} else if (j < i) {
			if (i < k) {
				dijk[j][i][k] = dijk[j][i][k] + change;
			} else if (k < j) {
				dijk[k][j][i] = dijk[k][j][i] + change;
			} else {
				dijk[j][k][i] = dijk[j][k][i] + change;
			}
		} else {
			System.err.println("Unable to update Dijk");
		}
	}
}
