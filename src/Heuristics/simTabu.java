package Heuristics;

import Runner.TestLogger;
import Solutions.ProblemSol;

/*
 * Simulated Annealing with a Tabu Search diversification
 */
public class simTabu extends Metaheuristic {

	private int maxIter;
	private long time;
	private int shiftTabu;

	/*
	 * Initialize parameters and init solution
	 */
	public simTabu(ProblemSol ps, int maxIter, long time) {
		super(ps);

		// Time/Iteration default values
		this.maxIter = 1000000;
		this.time = 60000000000L*5;
		this.shiftTabu = 0;
		if (maxIter != -1) {
			this.maxIter = maxIter;
		} 
		if (time != -1) {
			this.time = time;
		}
	}

	/*
	 * Run simulated annealing with tabu search when heuristic gets stuck
	 */
	public void run() {
		int stuck = 0;

		// Temperature/Alpha values
		double T = 0.3*current.getObj();
		double a = 0.99;
		TestLogger.logger.info(""+current.getValid());

		long start = System.nanoTime();
		long end = start;
		// Track time/iterations
		for (int iteration = 0; iteration < maxIter && (end-start) < time; iteration++) {
			// If stuck, try tabu search on best solution
			if (stuck > n*2) {
				TestLogger.logger.info("Starting Tabu Search");
				tabuSearch ts = new tabuSearch(best, n, time - (end-start));
				ts.run();

				// Update current and best from tabu search
				current = ts.getCurrent();
				ProblemSol tsBest = ts.getBest();
				if (tsBest.compareTo(best) > 0) {
					best = ProblemSol.copy(tsBest);
				}
				TestLogger.logger.info("Returning to Sim Anneal");
				T = 0.3*current.getObj();
				stuck = 0;
			}

			// Occasionally, check for a shift
			boolean shifted = false;;
			if (shiftTabu < iteration) {
				if (rnd.nextDouble() < 0.6) {
					int change = current.shift();
					// If shifted, a shift becomes tabu
					if (change != -1) {
						shiftTabu = iteration + (n/4);
						shifted = true;
					}
				}
			}

			if (!shifted) {
				// Get swaps
				ProblemSol swap;
				swap = current.mutate();
				if (swap != null) {
					// Check if swap better than current
					if (swap.compareTo(current) > 0) {
						current = swap;
					}
					// Otherwise, calculate probability
					else {
						// Calculate probabilities and compare
						double expProb = Math.exp((swap.getObj() - current.getObj())/T);
						double rdmDub = rnd.nextDouble();
						if (rdmDub <= expProb) {
							current = swap;
						}
					}
				}
			}

			// Check for invalid solution
			if (!current.getValid()) {
				current.healSol();
			}

			// Update Best
			if (current.compareTo(best) > 0) {
				best = ProblemSol.copy(current);
				TestLogger.logger.info("Best updated (SIM) at iteration " + iteration + " to " + current.getObj());
				stuck = 0;
			} else {
				stuck++;
			}

			// Update Temperature
			T = T * a;

			// Print iteration info
			TestLogger.logger.info(iteration + "- Cur: " + current.getObj() + " Best: " + best.getObj());
			TestLogger.logger.info(current.getX().toString());
			TestLogger.logger.info(""+current.getValid());

			end = System.nanoTime();

		}
	}
}
