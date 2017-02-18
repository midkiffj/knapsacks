package Heuristics;

import Runner.TestLogger;
import Solutions.ProblemSol;

/**
 * Tabu Search implementation
 * 
 * @author midkiffj
 */
public class tabuSearch extends Metaheuristic {

	private int[][] tabuList;
	private int shiftTabu;
	private int tabuDuration;
	private int maxIter;
	private long time;

	/*
	 * Setup initial solution and parameters
	 */
	public tabuSearch(ProblemSol ps, int maxIter, long time) {
		super(ps);

		// Tabu tracking variables
		tabuList = new int[n][n];
		tabuDuration = (int)Math.round(7.5*Math.log(n));
		shiftTabu = 0;

		// Default parameters
		this.maxIter = 1000000;
		this.time = 60000000000L*5;
		if (maxIter != -1) {
			this.maxIter = maxIter;
		} 
		if (time != -1) {
			this.time = time;
		}
	}

	/*
	 * Run tabu search with a best-swap diversification technique
	 */
	public void run() {
		int stuck = 0;
		int bestNotUpdated = 0;
		int diversified = 1;
		long start = System.nanoTime();
		long end = start;
		
		// Track time/iterations
		for (int i = 1; i < maxIter && (end-start) < time; i++) {
			// If best hasn't been updated, try to diversify
			if (bestNotUpdated >= 10*n*diversified) {
				bestNotUpdated = 0;
				diversified++;
				TestLogger.logger.info("Diversifying...");
				// Attempt 20 best swaps
				for (int d = 0; d < 20; d++) {
					// Get swap
					double[] swap = current.bestMutate();
					if (swap != null) {
						double newObj = swap[0];
						int j = (int) swap[1];
						int k = (int) swap[2];
						// Update current
						current.swap(newObj,j,k);
						if (!current.getValid()) {
							TestLogger.logger.info("Healing...Current: " + current.getObj());
							current.healSol();
							TestLogger.logger.info("New Current: " + current.getObj());
						}
						// Update best
						if (current.compareTo(best) > 0) {
							best = ProblemSol.copy(current);
							TestLogger.logger.info("Best updated from diversification to " + current.getObj());
						}
					}
				}
				TestLogger.logger.info("Div: " + current.getObj());
			}

			// Occasionally, check for a shift
			boolean shifted = false;;
			if (shiftTabu < i) {
				if (rnd.nextDouble() < 0.6) {
					int change = current.shift();
					// If shifted, a shift becomes tabu
					if (change != -1) {
						shiftTabu = i + (n/4);
						shifted = true;
					}
				}
			}

			// Otherwise, do a swap
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
						k = (int)tabu[2];

					}
					// Otherwise, use nonTabu
					else {
						if (nonTabu[1] != -1 && nonTabu[2] != -1) {
							newObj = nonTabu[0];
							j = (int)nonTabu[1];
							k = (int)nonTabu[2];
						}
					}

					// Perform swap and make tabu
					if (j != -1 && k != -1) {
						current.swap(newObj,j,k);
						makeTabu(j,k,i);
						stuck = 0;
					} 
					// Otherwise, stuck. Attempt shift after tabu duration passed
					else {
						TestLogger.logger.info("ERROR: Unable to Swap");
						stuck++;
						if (stuck > tabuDuration) {
							current.shift();
						}
					}
				}
			}

			// Check for invalid solution
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
			
			// Print iteration details
			TestLogger.logger.info(i + "- Cur: " + current.getObj() + " Best: " + best.getObj());
			TestLogger.logger.info(current.getX().toString());
			TestLogger.logger.info("Xsize: " + current.getXSize());

			// Update time
			end = System.nanoTime();
		}
	}

	/*
	 * Set swapping indexes i and j tabu
	 */
	private void makeTabu(int i, int j, int iteration) {
		tabuList[i][j] = iteration + tabuDuration;
		tabuList[j][i] = iteration + tabuDuration;
	}
}
