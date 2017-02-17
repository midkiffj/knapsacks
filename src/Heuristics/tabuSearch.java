package Heuristics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import Problems.Cubic;
import Problems.ProblemFactory;
import Runner.TestLogger;
import Solutions.CubicSol;
import Solutions.KnapsackSol;
import Solutions.ProblemSol;


public class tabuSearch extends Metaheuristic {

	private int[][] tabuList;
	private int shiftTabu;
	private int tabuDuration;
	private int maxIter;
	private long time;

	public tabuSearch(ProblemSol ps, int maxIter, long time) {
		super(ps);

		tabuList = new int[n][n];
		tabuDuration = (int)Math.round(7.5*Math.log(n));
		shiftTabu = 0;

		this.maxIter = 1000000;
		this.time = 60000000000L*5;
		if (maxIter != -1) {
			this.maxIter = maxIter;
		} 
		if (time != -1) {
			this.time = time;
		}
	}

	public void run() {
		int stuck = 0;
		int bestNotUpdated = 0;
		int diversified = 1;
		long start = System.nanoTime();
		long end = start;
		for (int i = 1; i < maxIter && (end-start) < time; i++) {

			if (bestNotUpdated >= 10*n*diversified) {
				bestNotUpdated = 0;
				diversified++;
				TestLogger.logger.info("Diversifying...");
				for (int d = 0; d < 20; d++) {
					double[] swap = current.bestMutate();
					if (swap != null) {
						double newObj = swap[0];
						int j = (int) swap[1];
						int k = (int) swap[2];
						current.swap(newObj,j,k);
						if (!current.getValid()) {
							TestLogger.logger.info("Healing...Current: " + current.getObj());
							current.healSol();
							TestLogger.logger.info("New Current: " + current.getObj());
						}
						if (current.compareTo(best) > 0) {
							best = ProblemSol.copy(current);
							TestLogger.logger.info("Best updated from diversification to " + current.getObj());
						}
					}
				}
				TestLogger.logger.info("Div: " + current.getObj());
			}

			// Check for shifts
			boolean shifted = false;;
			if (shiftTabu < i) {
				if (rnd.nextDouble() < 0.6) {
					int change = current.shift();
					if (change != -1) {
						shiftTabu = i + (n/4);
						shifted = true;
					}
				}
			}

			if (!shifted) {
				double[][] swap = current.tabuMutate(i,tabuList);
				if (swap != null) {
					double[] tabu = swap[0];
					double[] nonTabu = swap[1];
					double newObj = -1;
					int j = -1;
					int k = -1;
					// Check if tabu swap better than best
					if (best.betterThan(tabu[0])) {
						newObj = tabu[0];
						j = (int)tabu[1];
						//			if (swapping) {
						k = (int)tabu[2];
						//			}

					}
					// Otherwise, use nonTabu
					else {
						if (nonTabu[1] != -1 && nonTabu[2] != -1) {
							newObj = nonTabu[0];
							j = (int)nonTabu[1];
							//			if (swapping) {
							k = (int)nonTabu[2];
							//			}
						}
					}

					// Perform and tabu swap
					if (j != -1 && k != -1) {
						current.swap(newObj,j,k);
						makeTabu(j,k,i);
						stuck = 0;
					} else {
						TestLogger.logger.info("ERROR: Unable to Swap");
						stuck++;
						if (stuck > 3) {
							current.shift();
						}
					}
				}
			}

			if (!current.getValid()) {
				TestLogger.logger.info("Healing...Current: " + current.getObj());
				current.healSol();
				TestLogger.logger.info("New Current: " + current.getObj());
			}

			// Update Best
			if (current.compareTo(best) > 0) {
				best = ProblemSol.copy(current);
				TestLogger.logger.info("Best updated at iteration " + i + " to " + current.getObj());
				bestNotUpdated = 0;
			} else {
				bestNotUpdated++;
			}
			TestLogger.logger.info(i + "- Cur: " + current.getObj() + " Best: " + best.getObj());
			TestLogger.logger.info(current.getX().toString());
			TestLogger.logger.info("Xsize: " + current.getXSize());

			end = System.nanoTime();
		}
	}



	private void tabuShift(int i, int iteration) {
		for (int j = 0; j < i; j++) {
			makeTabu(i,j,iteration);
		}
		for (int j = i+1; j < n; j++) {
			makeTabu(i,j,iteration);
		}
	}

	private void makeTabu(int i, int j, int iteration) {
		tabuList[i][j] = iteration + tabuDuration;
		tabuList[j][i] = iteration + tabuDuration;
	}

	//	public static void main(String[] args) {
	//		Cubic c = new Cubic("problems/20_0.25_true_0");
	//		CubicSol cs = new CubicSol("incumbents/20_0.25_true_0inc.txt");
	//		tabuSearch ts = new tabuSearch(cs,-1,-1);
	//		ts.run();
	//	}
}
