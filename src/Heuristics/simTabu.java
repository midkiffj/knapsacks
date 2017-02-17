package Heuristics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import Problems.ProblemFactory;
import Runner.TestLogger;
import Solutions.KnapsackSol;
import Solutions.ProblemSol;


public class simTabu extends Metaheuristic {

	private int[][] tabuList;
	private int shiftTabu;
	private int tabuDuration;
	private int maxIter;
	private long time;

	public simTabu(ProblemSol ps, int maxIter, long time) {
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
		HashSet<Long> solutions = new HashSet<Long>();
		int stuck = 0;

		double T = 0.3*current.getObj();
		double a = 0.99;
		TestLogger.logger.info(""+current.getValid());

		long start = System.nanoTime();
		long end = start;
		for (int iteration = 0; iteration < maxIter && (end-start) < time; iteration++) {
			if (stuck > n*2) {
				TestLogger.logger.info("Starting Tabu Search");
				tabuSearch ts = new tabuSearch(current, 2*n,time);
				ts.run();
				current = ts.getCurrent();
				ProblemSol tsBest = ts.getBest();
				if (tsBest.compareTo(best) > 0) {
					best = ProblemSol.copy(tsBest);
				}
				TestLogger.logger.info("Returning to Sim Anneal");
				T = 0.3*current.getObj();
				stuck = 0;
			}
			// Get swaps 1657185
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
					//						System.out.println("Exp Prob: " + expProb);
					double rdmDub = rnd.nextDouble();
					if (rdmDub <= expProb) {
						newObj = swap[0];
						j = (int)swap[1];
						k = (int)swap[2];
					}
				}

				// Perform swap
				if (j != -1 && k != -1) {
					//					System.out.println("Swapping ("+j+","+k+")");
					current.swap(newObj,j,k);
				} else {
					//					if (rnd.nextDouble() < 0.2) {
					//						int change = shift();
					//						System.out.println("Attempted Shift");
					//					}
				}
			}

			if (!current.getValid()) {
				current.healSol();
			}
			//			if (solutions.add(current.getObj())) {
			//				stuck = 0;
			//			} else {
			//				stuck += 1;
			//			}

			// Update Best
			if (current.compareTo(best) > 0) {
				best = ProblemSol.copy(current);
				TestLogger.logger.info("Best updated (SIM) at iteration " + iteration + " to " + current.getObj());
				stuck = 0;
			} else {
				stuck++;
			}

			T = T * a;

			// Print iteration info
			TestLogger.logger.info(iteration + "- Cur: " + current.getObj() + " Best: " + best.getObj());
			TestLogger.logger.info(current.getX().toString());
			TestLogger.logger.info(""+current.getValid());

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
	
}
