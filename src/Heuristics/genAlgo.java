package Heuristics;

import Problems.Fractional;
import Problems.MaxProbability;
import Problems.ProblemFactory;
import Runner.TestLogger;
import Solutions.ProblemSol;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Genetic Algorithm Implementation
 * - Runs a specified problem in a population
 * - Uses a 2-Tournament selection and 1-elitist strategy
 * 
 * @author midkiffj
 *
 */
public class genAlgo extends Metaheuristic{

	private ArrayList<ProblemSol> population;
	private int numZero;
	private int removeAttempts;
	private int numGens;
	private long time;
	
	/*
	 * Setup the parameters and initial population
	 */
	public genAlgo(ProblemSol ps, int numGens, long time) {
		super(ps);
		numZero = 0;
		removeAttempts = n/5;
		// Default small population size ensures enough solutions can be generated
		int popSize = 10;
		
		// Set number of generations to run
		if (numGens == -1) {
			this.numGens = 50*popSize;
		} else {
			this.numGens = numGens;
		}
		
		// Set time to run
		if (time == -1) {
			this.time = 60000000000L*5;
		} else {
			this.time = time;
		}

		// Fill population with specified solution and random solutions
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
	
	/*
	 * Tracks time and generations ran
	 * - Stores the best solution generated
	 */
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

	/*
	 *  Update the population by 
	 *  - generating new individuals 
	 *  - adding non-duplicate solutions
	 */
	private void updatePopulation() {
		ArrayList<ProblemSol> newPop = new ArrayList<ProblemSol>();
		// 1-elitist strategy
		int elitist = 0;
		for (int i = population.size()-1; i >= 0 && elitist < 1; i--) {
			if (population.get(i).getValid()) {
				newPop.add(population.get(i));
				elitist++;
			}
		}
		newPop.add(population.get(1));
		// Check for invalid solutions
		for (ProblemSol ps: newPop) {
			if (!ps.getValid()) {
				numZero++;
			}
		}
		// Generate and add rest of population
		for (int i = 0; i < population.size()-2; i++) {
			boolean added = false;
			int j = 0;
			// Attempt to generate/add an individual
			while (!added && j < 30) {
				ProblemSol ps = generateIndividual();
				added = tryAdd(newPop, ps);
				j++;
			}
			// Otherwise, add a random solution
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

	/*
	 * Attempt to add the new solution to the population
	 * - Avoid duplicates
	 * - Avoid too many invalid solutions (if allowed)
	*/ 
	private boolean tryAdd(ArrayList<ProblemSol> newPop, ProblemSol ps) {
		// Check for duplicate
		if (newPop.contains(ps))  {
			return false;
		} else {
			// Add only 4 invalid answers
			if (ps.getHealing() && !ps.getValid() && ps.getX().size() > 0 && numZero < 4) {
				newPop.add(ps);
				numZero++;
				return true;
			} 
			// If too many invalid answers, try healing and adding
			else if (ps.getHealing() && !ps.getValid() && ps.getX().size() > 0 && numZero >= 4) {
				ps.healSol();
				return tryAdd(newPop,ps);
			}
			// If valid, add
			else if (ps.getValid() && ps.getX().size() > 0) {
				newPop.add(ps);
				return true;
			} else {
				return false;
			}
		}
	}

	/*
	 * Pretty Print generation
	 */
	private void printPopulation(int generation) {
		TestLogger.logger.info("Generation " + generation + ":");
		TestLogger.logger.info("  #   |    Population  Objective    | X array");
		TestLogger.logger.info("------|-----------------------------|--------");
		for (int i = 0; i < population.size(); i++) {
			ProblemSol ps = population.get(i);
			String s;
			if (ProblemFactory.getProblem() instanceof MaxProbability || ProblemFactory.getProblem() instanceof Fractional) {
				s = String.format(" %4d |   %10.4f (%b)   |",i,ps.getObj(), ps.getValid());
			} else {
				s = String.format(" %4d |   %10.0f (%b)   |",i,ps.getObj(), ps.getValid());
			}
			Collections.sort(ps.getX());
			s = s + ps.getX().toString();
			TestLogger.logger.info(s);
			TestLogger.logger.info("------|-----------------------------|--------");
		}
		TestLogger.logger.info("\n");
	}

	/*
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

	/*
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
