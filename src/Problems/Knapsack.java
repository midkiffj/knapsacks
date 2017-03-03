package Problems;

/**
 * Knapsack abstract class to add a/b accessors
 *  (used in KnapsackSol)
 * 
 * @author midkiffj
 */
public abstract class Knapsack extends Problem {
	
	public Knapsack() {
		super();
	}
	
	public abstract int getA(int i);
	
	public abstract int getB();
}
