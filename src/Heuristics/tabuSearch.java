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
					ProblemSol swap = current.bestMutate();
					if (swap != null) {
						// Update current
						current = swap;
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
				ProblemSol[] swap = current.tabuMutate(i,tabuList);
				if (swap != null) {
					ProblemSol tabu = swap[0];
					ProblemSol nonTabu = swap[1];
					
					boolean swapped = false;
					// Check if tabu swap better than best
					if (tabu != null && tabu.compareTo(best) > 0) {
						current = tabu;
						swapped = true;
						makeSwapTabu(i);
					}
					// Otherwise, use nonTabu
					else {
						if (nonTabu != null) {
							current = nonTabu;
							swapped = true;
							makeSwapTabu(i);
						}
					}

					// Otherwise, stuck. Attempt shift after tabu duration passed
					if (!swapped) {
						TestLogger.logger.info("ERROR: Unable to Swap");
						stuck++;
						if (stuck > tabuDuration) {
							current.shift();
							stuck = 0;
						}
					} else {
						stuck = 0;
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
	private void makeSwapTabu(int iteration) {
		int i = current.getX().get(current.getXSize()-1);
		int j = current.getR().get(current.getRSize()-1);
		tabuList[i][j] = iteration + tabuDuration;
		tabuList[j][i] = iteration + tabuDuration;
	}
}
