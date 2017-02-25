package Problems;

import java.util.ArrayList;

public abstract class MultipleKnapsack extends Problem {

	public abstract int getM();

	public abstract int getA(int i,int j);
	
	public abstract int getB(int i);
	
	public abstract int[] removeA(int i, int[] totalA);

	public abstract int[] addA(int i, int[] totalA);
	
	public abstract boolean swapTotalA(int[] totalA, int i, int j);
	
	public abstract boolean subTotalA(int[] totalA, int j);
	
	public abstract boolean addTotalA(int[] totalA, int j);
	
	public abstract int[] calcAllTotalA(ArrayList<Integer> x);
	
	public abstract boolean totalAValid(int[] totalA);
	
}
