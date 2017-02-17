package Heuristics;

import java.util.Random;

import Problems.ProblemFactory;
import Runner.RndGen;
import Solutions.ProblemSol;

public abstract class Metaheuristic {
	
	public ProblemSol current;
	public ProblemSol best;
	public Random rnd = RndGen.getRnd();
	public int n;
	
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
