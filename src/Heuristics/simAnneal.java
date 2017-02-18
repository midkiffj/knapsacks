package Heuristics;

import Runner.TestLogger;
import Solutions.ProblemSol;

/**
 * Simulated Annealing implementation
 * 
 * @author midkiffj
 */
public class simAnneal extends Metaheuristic {

	private int maxIter;
	private long time;
	
	/*
	 * Setup with initial solution and iteration/time parameters
	 */
	public simAnneal(ProblemSol ps, int maxIter, long time) {
		super(ps);
		
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
	 * Run simulated annealing on the solution
	 */
	public void run() {
		// Temp and alpha value
		double T = 0.3*current.getObj();
		double a = 0.99;
		
		int expZero = 0;
		long start = System.nanoTime();
		long end = start;
		// Track time and iterations
		for (int iteration = 0; iteration < maxIter && (end-start) < time; iteration++) {			
			// Get swap mutation
			double[] swap;
			swap = current.mutate();
			if (swap != null) {
				double newObj = -1;
				int j = -1;
				int k = -1;
				// Check if swap better than current
				if (current.betterThan(swap[0])) {
					newObj = swap[0];
					j = (int)swap[1];
					k = (int)swap[2];
				}
				// Otherwise, calculate probability
				else {
					// Calculate probabilities and compare
					double expProb = Math.exp((swap[0] - current.getObj())/T);
					// Check for when T gets too small
					if (expProb == 0.0) {
						expZero++;
					}
					TestLogger.logger.info("expProb: " + expProb + " expZero: " + expZero);
					double rdmDub = rnd.nextDouble();
					if (rdmDub <= expProb) {
						newObj = swap[0];
						j = (int)swap[1];
						k = (int)swap[2];
					}
				}

				// Perform swap
				if (j != -1 && k != -1) {
					current.swap(newObj,j,k);
				} 
			}
			
			// Heal solution if invalid
			if (!current.getValid()) {
				current.healSol();
			}

			// Update Best
			if (current.compareTo(best) > 0) {
				best = ProblemSol.copy(current);
				TestLogger.logger.info("Best updated (SIM) at iteration " + iteration + " to " + current.getObj());
			}

			// Update temp
			T = T * a;
			TestLogger.logger.info("Iteration: " + iteration + " T:" + T);
			
			// Reset T after becomes too small
			if (expZero > 20) {
				T = 0.3*best.getObj();
				expZero = 0;
				current = ProblemSol.copy(best);
			}

			// Print iteration info
			TestLogger.logger.info(iteration + "- Cur: " + current.getObj() + " Best: " + best.getObj());
			TestLogger.logger.info(current.getX().toString());

			// Update time
			end = System.nanoTime();
		}
	}

}
