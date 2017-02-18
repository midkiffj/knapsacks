package Heuristics;

import java.util.Random;

import Problems.ProblemFactory;
import Runner.RndGen;
import Solutions.ProblemSol;

/**
 * Metaheuristic super class
 *	- Stores current and best solution found
 *	- Abstract run method to allow individual heuristic implementation
 *
 * @author midkiffj
 *
 */
public abstract class Metaheuristic {
	
	public ProblemSol current;
	public ProblemSol best;
	public Random rnd = RndGen.getRnd();
	public int n;
	
	/*
	 * Create from given problem solution
	 *  best = current = initial
	 */
	public Metaheuristic(ProblemSol initial) {
		current = ProblemSol.copy(initial);
		best = ProblemSol.copy(current);
		n = ProblemFactory.getProblem().getN();
	}
	
	public double getBestObj() {
		return best.getObj();
	}
	
	public ProblemSol getCurrent() {
		return current;
	}
	
	public ProblemSol getBest() {
		return best;
	}
	
	public abstract void run();
}
