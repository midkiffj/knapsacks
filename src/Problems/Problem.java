package Problems;

import java.util.ArrayList;

public abstract class Problem {

	protected Problem() {
		ProblemFactory.setProblem(this);
	}
	
	public abstract double swapObj(int i, int j, ArrayList<Integer> x, double oldObj);
	
	public abstract int trySub(ArrayList<Integer> x, boolean improveOnly);
	
	public abstract int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly);
	
	public abstract double subObj(int i, ArrayList<Integer> x, double oldObj);
	
	public abstract double addObj(int i, ArrayList<Integer> x, double oldObj);
	
	public abstract int removeA(int i, int totalA);
	
	public abstract int addA(int i, int totalA);
	
	public abstract boolean checkValid(ArrayList<Integer> x);
	
	public abstract double getObj(ArrayList<Integer> x);
	
	public abstract int getN();
	
	public abstract int calcTotalA(ArrayList<Integer> x);
	
	public abstract void genInit(ArrayList<Integer> x, ArrayList<Integer> r);
	
	public abstract void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r);
	
}
