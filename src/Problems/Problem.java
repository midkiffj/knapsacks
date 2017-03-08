package Problems;

import java.util.ArrayList;

/**
 * Problem super class.
 * - Sets the problem in the problem factory
 * 
 * @author midkiffj
 */
public abstract class Problem {

	protected Problem() {
		ProblemFactory.setProblem(this);
	}
	
	public abstract double getObj(ArrayList<Integer> x);
	
	public abstract int getN();
	
	public abstract double getRatio(int i);
	
	public abstract void genInit(ArrayList<Integer> x, ArrayList<Integer> r);
	
	public abstract void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r);
	
}
