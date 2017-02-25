package Problems;

import java.util.ArrayList;

public abstract class Knapsack extends Problem {
	
	public Knapsack() {
		super();
	}
	
	public abstract double swapObj(int i, int j, ArrayList<Integer> x, double oldObj);

	public abstract int trySub(ArrayList<Integer> x, boolean improveOnly);

	public abstract int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly);

	public abstract double subObj(int i, ArrayList<Integer> x, double oldObj);

	public abstract double addObj(int i, ArrayList<Integer> x, double oldObj);

	public abstract int calcTotalA(ArrayList<Integer> x);

	public abstract int removeA(int i, int totalA);

	public abstract int addA(int i, int totalA);
}
