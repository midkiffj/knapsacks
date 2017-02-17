package Heuristics;

import Problems.MaxProbability;
import Problems.ProblemFactory;
import Runner.TestLogger;
import Solutions.ProblemSol;

import java.util.ArrayList;
import java.util.Collections;

public class genAlgo extends Metaheuristic{

	private ArrayList<ProblemSol> population;
	private int numZero;
	private int removeAttempts;
	private int numGens;
	private long time;
	
	public genAlgo(ProblemSol ps, int numGens, long time) {
		super(ps);
		numZero = 0;
		removeAttempts = n/5;

		int popSize = 10;
		if (numGens == -1) {
			this.numGens = 50*popSize;
		} else {
			this.numGens = numGens;
		}
		
		
		if (time == -1) {
			this.time = 60000000000L*5;
		} else {
			this.time = time;
		}

		population = new ArrayList<ProblemSol>();
		population.add(ps);
		
		ProblemSol ps2;
		for (int i = 0; i < popSize-1; i++) {
			ps2 = ProblemFactory.genRndSol();
			boolean added = tryAdd(population, ps2);
			if (!added) {
				i--;
			}
		}
		Collections.sort(population);
	}
	
	public void run() {
		long start = System.nanoTime();
		long end = start;
		printPopulation(-1);
		for (int i = 0; i < numGens && (end-start) < time; i++) {
			updatePopulation();
			printPopulation(i);
			end = System.nanoTime();
		}
		for (int i = population.size()-1; i >= 0; i--) {
			if (population.get(i).getValid()) {
				this.best = population.get(i);
				i = -1;
			}
		}
	}

	private void updatePopulation() {
		ArrayList<ProblemSol> newPop = new ArrayList<ProblemSol>();
		newPop.add(population.get(1));
		int elitist = 0;
		for (int i = population.size()-1; i >= 0 && elitist < 1; i--) {
			if (population.get(i).getValid()) {
				newPop.add(population.get(i));
				elitist++;
			}
		}
		for (ProblemSol ps: newPop) {
			if (!ps.getValid()) {
				numZero++;
			}
		}
		for (int i = 0; i < population.size()-2; i++) {
			boolean added = false;
			int j = 0;
			while (!added && j < 30) {
				ProblemSol ps = generateIndividual();
				added = tryAdd(newPop, ps);
				j++;
			}
			while (!added) {
				ProblemSol ps = ProblemFactory.genRndSol();
				added = tryAdd(newPop, ps);
			}
		}
		population = newPop;
		Collections.sort(population);
		numZero = 0;
		removeAttempts = 0;
	}

	private boolean tryAdd(ArrayList<ProblemSol> newPop, ProblemSol ps) {
//		if (!ps.getValid()) {
//			ps.healSol();
//		}
		if (newPop.contains(ps))  {
			return false;
		} else {
			if (ps.getHealing() && !ps.getValid() && ps.getObj() != 0 && ps.getX().size() > 0 && numZero < 5) {
				newPop.add(ps);
				numZero++;
				return true;
			} else if (ps.getValid() && ps.getX().size() > 0) {
				newPop.add(ps);
				return true;
			} else {
				return false;
			}
		}
	}

	private void printPopulation(int generation) {
		TestLogger.logger.info("Generation " + generation + ":");
		TestLogger.logger.info("  #   |    Population  Objective    | X array");
		TestLogger.logger.info("------|-----------------------------|--------");
		for (int i = 0; i < population.size(); i++) {
			ProblemSol ps = population.get(i);
			String s;
			if (ProblemFactory.getProblem() instanceof MaxProbability) {
				s = String.format(" %4d |   %10.4f (%b)   |",i,ps.getObj(), ps.getValid());
			} else {
				s = String.format(" %4d |   %10.0f (%b)   |",i,ps.getObj(), ps.getValid());
			}
			s = s + ps.getX().toString();
			TestLogger.logger.info(s);
			TestLogger.logger.info("------|-----------------------------|--------");
		}
		TestLogger.logger.info("\n");
	}

	/**
	 * Generate an individual using a crossover or mutation
	 * @return the generated individual
	 */
	private ProblemSol generateIndividual() {
		ProblemSol ps1 = tournament();
		if (rnd.nextDouble() < 0.7) {
			ProblemSol ps2 = tournament();
			ProblemSol ps3 = ps1.crossover(ps2);
			return ps3;
		} else {
			ProblemSol ps2 = ps1.genMutate(removeAttempts);
			return ps2;
		}
	}

	/**
	 * Two-individual tournament selection
	 * @return the 'better' knapsack solution
	 */
	private ProblemSol tournament() {
		int r1 = rnd.nextInt(population.size());
		int r2 = rnd.nextInt(population.size());
		ProblemSol ps1 = population.get(r1);
		ProblemSol ps2 = population.get(r2);

		if (ps1.compareTo(ps2) >= 0) {
			return ps1;
		} else {
			return ps2;
		}
	}
	
}
