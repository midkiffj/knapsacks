package Solutions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import Problems.Problem;
import Problems.ProblemFactory;
import Runner.RndGen;

public abstract class ProblemSol implements Comparable<ProblemSol>, Comparator<ProblemSol>{

	static Problem p = ProblemFactory.getProblem();
	static boolean useHealing = false;
	
	public int n = p.getN();
	public Random rnd = RndGen.getRnd();
	
	public ProblemSol() {
		updateProblem();
	}
	
	private void updateProblem() {
		p = ProblemFactory.getProblem();
		n = p.getN();
	}
	
	public void setHealing(boolean useHeal) {
		useHealing = useHeal;
	}
	
	public boolean getHealing() {
		return useHealing;
	}
	
	public abstract double getObj();
	
	public abstract void swap(double newObj, int i, int j);
	
	public abstract ArrayList<Integer> getX();

	public abstract ArrayList<Integer> getR();
	
	public abstract int getRItem(int i);
	
	public abstract int getXItem(int i);
	
	public abstract int getRSize();
	
	public abstract int getXSize();
	
	public abstract double[] bestMutate();
	
	public abstract double[][] tabuBestMutate(int iteration, int[][] tabuList);
	
	public abstract double[][] tabuMutate(int iteration, int[][] tabuList);
	
	public abstract double[] mutate();
	
	public abstract int shift();
	
	public abstract ProblemSol crossover(ProblemSol ps2);
	
	public abstract ProblemSol genMutate(int removeAttempts);
	
	public abstract boolean getValid();
	
	public abstract void healSol();
	
	public abstract boolean getXVals(int i);
	
	public static ProblemSol copy(ProblemSol ps) {
		if (ps instanceof CubicSol) {
			return new CubicSol((CubicSol)ps);
		} else if (ps instanceof UnconstrainedSol) {
			return new UnconstrainedSol((UnconstrainedSol)ps);
		} else if (ps instanceof MaxProbabilitySol) {
			return new MaxProbabilitySol((MaxProbabilitySol)ps);
		}
		return null;
	}
	
	@Override
	public abstract int compareTo(ProblemSol o);

	public abstract boolean betterThan(double newObj);

	@Override
	public boolean equals(Object object) {
		ProblemSol ks2 = (ProblemSol)object;
		if (this.getObj() == ks2.getObj()) {
			for (int i = 0; i < p.getN(); i++) {
				if (this.getXVals(i) != ks2.getXVals(i)) {
					return false;
				}
			}
			return true;
		} else {
			boolean allSame = true;
			for (int i = 0; i < p.getN(); i++) {
				if (this.getXVals(i) != ks2.getXVals(i)) {
					allSame = false;
				}
			}
			if (allSame) {
				System.out.println("Duplicate solution with different objective");
			}
		}
		return false;
	}

	@Override
	public int compare(ProblemSol o1, ProblemSol o2) {
		return o1.compareTo(o2);
	}
}
