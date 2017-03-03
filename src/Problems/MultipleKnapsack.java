package Problems;

/**
 * Multiple Knapsack abstract class for accessors
 *  (used in MultKnapsackSol)
 *  
 * @author midkiffj
 */
public abstract class MultipleKnapsack extends Problem {

	public MultipleKnapsack() {
		super();
	}
	
	public abstract int getM();

	public abstract int getA(int i,int j);
	
	public abstract int getB(int i);
	
}
