package archive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class CubicGA {

	private int n;
	private Random rnd;
	private Cubic c;

	private ArrayList<KnapsackSol> population;
	private int numZero;
	private int removeAttempts;

	public CubicGA(Cubic c) {
		this.c = c;
		KnapsackSol.c = c;
		n = c.getN();
		rnd = new Random(1234);
		numZero = 0;
		removeAttempts = n/5;

		int popSize = 10;
		int numGens = 50*popSize;

		population = new ArrayList<KnapsackSol>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		c.genInit(new ArrayList<Integer>(),x,r);
		//		c.genRndInit(new ArrayList<Integer>(),x,r);
		long curObj = c.getIncObj();
		int curTotalA = c.getIncTotalA();
		population.add(new KnapsackSol(x, r, curObj, curTotalA));
		for (int i = 0; i < popSize-1; i++) {
			x = new ArrayList<Integer>();
			r = new ArrayList<Integer>();
			//			c.genInit(new ArrayList<Integer>(),x,r);
			c.genRndInit(new ArrayList<Integer>(),x,r);
			curObj = c.getIncObj();
			curTotalA = c.getIncTotalA();
			KnapsackSol ks = new KnapsackSol(x, r, curObj, curTotalA);
			boolean added = tryAdd(population, ks);
			if (!added) {
				i--;
			}
		}
		Collections.sort(population);

		for (int i = 0; i < numGens; i++) {
//			System.out.println("gen:" + i);
			updatePopulation();
			//			printPopulation(i);
		}
	}

	public long getBestObj() {
		return population.get(population.size()-1).getObjGA();
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			CubicGA cga = new CubicGA(new Cubic(args[0]));
		}
	}

	private void updatePopulation() {
		ArrayList<KnapsackSol> newPop = new ArrayList<KnapsackSol>();
		newPop.add(population.get(1));
		newPop.add(population.get(population.size()-1));
		for (int i = 0; i < population.size()-2; i++) {
//			System.out.println(i);
			boolean added = false;
			int j = 0;
			while (!added && j < 30) {
				KnapsackSol ks = generateIndividual();
				added = tryAdd(newPop, ks);
				j++;
			}
			while (!added) {
				ArrayList<Integer> x = new ArrayList<Integer>();
				ArrayList<Integer> r = new ArrayList<Integer>();
				c.genRndInit(new ArrayList<Integer>(),x,r);
				long curObj = c.getIncObj();
				int curTotalA = c.getIncTotalA();
				KnapsackSol ks = new KnapsackSol(x, r, curObj, curTotalA);
				added = tryAdd(newPop, ks);
			}
		}
		population = newPop;
		Collections.sort(population);
		numZero = 0;
		removeAttempts = 0;
	}

	private boolean tryAdd(ArrayList<KnapsackSol> newPop, KnapsackSol ks) {
		if (newPop.contains(ks))  {
			return false;
		} else {
			if (ks.getObjGA() == 0 && ks.getObj() != 0 && ks.getX().size() > 0 && numZero < 5) {
				newPop.add(ks);
				numZero++;
				return true;
			} else if (ks.getObjGA() != 0 && ks.getX().size() > 0) {
				newPop.add(ks);
				return true;
			} else {
				return false;
			}
		}
	}

	private void printPopulation(int generation) {
		System.out.println("Generation " + generation + ":");
		System.out.println("  #   |    Population  Objective    | X array");
		System.out.println("------|-----------------------------|--------");
		for (int i = 0; i < population.size(); i++) {
			KnapsackSol ks = population.get(i);
			String s = String.format(" %4d |   %10d (%10d)   |",i,ks.getObj(), ks.getObjGA());
			s = s + ks.getX().toString();
			System.out.println(s);
			System.out.println("------|-----------------------------|--------");
		}
		System.out.println();
	}

	/**
	 * Generate an individual using a crossover or mutation
	 * @return the generated individual
	 */
	private KnapsackSol generateIndividual() {
		KnapsackSol ks1 = tournament();
		if (rnd.nextDouble() < 0.7) {
			KnapsackSol ks2 = tournament();
			KnapsackSol ks3 = crossover2(ks1, ks2);
			if (ks3.getObjGA() == 0) {
//				System.out.println("found it");
			}
			return ks3;
		} else {
			KnapsackSol ks3 = mutate(ks1);
			return ks3;
		}
	}

	private KnapsackSol crossover(KnapsackSol ks1, KnapsackSol ks2) {
		boolean[] newXVals = new boolean[n];
		for (int i = 0; i < n/2; i++) {
			newXVals[i] = ks1.getXVals(i);
		}
		for (int i = n/2; i < n; i++) {
			newXVals[i] = ks2.getXVals(i);
		}
		return new KnapsackSol(newXVals);
	}

	private KnapsackSol crossover2(KnapsackSol ks1, KnapsackSol ks2) {
		boolean[] newXVals = new boolean[n];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		for (int i = 0; i < n; i++) {
			if (ks1.getXVals(i) == ks2.getXVals(i)) {
				newXVals[i] = ks1.getXVals(i);
				if (newXVals[i]) {
					x.add(i);
					newTotalA += c.getA(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);
		Collections.sort(ratio);

		while (ratio.size() > 0 && newTotalA < c.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + c.getA(rni.x) <= c.getB()) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				newTotalA += c.getA(rni.x);
			} else {
				ratio.remove(i);
			}
		}

		return new KnapsackSol(newXVals);
	}


	private ArrayList<ratioNode> computeRatios(ArrayList<Integer> x, ArrayList<Integer> r) {
		//		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		//		for (Integer i: r) {
		//			double iRatio = c.getCi(i);
		//			for (int j = 0; j < x.size(); j++) {
		//				int xj = x.get(j);
		//				iRatio += 2 * c.getCij(i, xj);
		//				for (int k = j+1; k < x.size(); k++) {
		//					int xk = x.get(k);
		//					iRatio += 6 * c.getDijk(i, xj, xk);
		//				}
		//			}
		//			ratioNode rni = new ratioNode(i, iRatio);
		//			ratio.add(rni);
		//		}
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, c.getRatio(i));
			ratio.add(rni);
		}
		return ratio;
	}

	private void updateRatios(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int added) {
		for (ratioNode rni: ratio) {
			int i = rni.x;
			double iRatio = 2 * c.getCij(i,added);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				iRatio += 6 * c.getDijk(i, xj, added);
			}
			rni.ratio += iRatio;
		}
	}

	private KnapsackSol mutate(KnapsackSol ks) {
		KnapsackSol newKS = new KnapsackSol(ks);
		if (rnd.nextDouble() < 0.5) {
			if (newKS.getRSize() == 0) {
				shift(newKS);
			} else {
				if (rnd.nextDouble() < 0.6) {
					maxMinSwap(newKS);
				} else {
					ratioMutate(newKS);
				}
			}
		} else {
			mutate2(newKS);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newKS;
	}

	private KnapsackSol mutate2(KnapsackSol ks) {
		// Remove s items from the solution
		ArrayList<Integer> x = new ArrayList<Integer>(ks.getX());
		ArrayList<Integer> r = new ArrayList<Integer>(ks.getR());
		int s = removeAttempts;
		if (s >= x.size()) {
			s = x.size()-1;
		}
		for (int i = 0; i < s; i++) {
			int j = rnd.nextInt(x.size());
			r.add(x.remove(j));
		}

		// Compute ratios
		ArrayList<ratioNode> ratio = computeRatios(x, r);
		Collections.sort(ratio);

		// Calc solution capacity
		int newTotalA = 0;
		for (Integer i: x) {
			newTotalA += c.getA(i);
		}
		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && newTotalA < c.getB()) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (newTotalA + c.getA(rni.x) <= c.getB()) {
				ratio.remove(ratio.size()-1);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += c.getA(rni.x);
			} else {
				ratio.remove(ratio.size()-1);
				r.remove(Integer.valueOf(rni.x));
			}
		}
		// Calculate obj of new solution
		long obj = c.updateObj(x);
		return new KnapsackSol(x,r,obj,newTotalA);
	}

	/**
	 * Two-individual tournament selection
	 * @return the 'better' knapsack solution
	 */
	private KnapsackSol tournament() {
		int r1 = rnd.nextInt(population.size());
		int r2 = rnd.nextInt(population.size());
		KnapsackSol ks1 = population.get(r1);
		KnapsackSol ks2 = population.get(r2);

		if (ks1.compareTo(ks2) >= 0) {
			return ks1;
		} else {
			return ks2;
		}
	}

	private void ratioMutate(KnapsackSol ks) {
		boolean swapped = false;
		int ki = 0;
		while (!swapped && ki < ks.getX().size()) {
			// Get index of min ratio
			int i = minRatio(ks, ki);

			// Swap with a random node and return
			int j = rnd.nextInt(ks.getRSize());
			j = ks.getRItem(j);
			int rndCount = 0;
			while (c.getA(j) - c.getA(i) > c.getB() - ks.getTotalA() && rndCount < ks.getRSize()*2) {
				j = rnd.nextInt(ks.getRSize());
				j = ks.getRItem(j);
				rndCount++;
			}
			if (c.getA(j) - c.getA(i) <= c.getB() - ks.getTotalA()) {
				long newObj = ks.swapObj(i, j);
				ks.swap(newObj, i, j);
				swapped = true;
			} else {
				ki++;
			}
		}
	}

	private void maxMinSwap(KnapsackSol ks) {
		// Occasionally perform a shift
		if (rnd.nextDouble() < 0.3) {
			int change = shift(ks);
			if (change != -1) {
				return;
			}
		}
		// Store b
		int b = c.getB();

		int i = minRatio(ks, 0);
		int j = maxRatio(ks, 0);
		int ki = 0;
		int kj = 0;
		boolean changeI = false;
		while (c.getA(j) - c.getA(i) > b - ks.getTotalA() && ki < n) {
			if (changeI) {
				ki++;
				i = minRatio(ks, ki);
				changeI = !changeI;
			}
			kj++;
			j = maxRatio(ks, kj);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		long newObj = ks.swapObj(i, j);

		ks.swap(newObj, i, j);
	}

	// Shift a variable in or out of the current solution
	private int shift(KnapsackSol ks) {
		if (ks.getXSize() < 2) {
			return ks.tryAdd();
		} else if (ks.getRSize() == 0) {
			return ks.trySub();
		} else {
			if (rnd.nextDouble() < 0.8) {
				return ks.tryAdd();
			} else {
				//				return ks.trySub();
				return -1;
			}
		}
	}

	private int minRatio(KnapsackSol ks, int k) {
		// Find the minimum ratio in the solution
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < ks.getXSize()) {
			for (Integer i: ks.getX()) {
				if (c.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = c.getRatio(i);
					minI = i;
				}
			}
			minRatio = Double.MAX_VALUE;
			bestIs.add(minI);
		}
		return minI;
	}

	private int maxRatio(KnapsackSol ks, int k) {
		// Find the maximum ratio not in the solution
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < ks.getRSize()) {
			for (Integer i: ks.getR()) {
				if (c.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = c.getRatio(i);
					maxI = i;
				}
			}
			maxRatio = -1*Double.MAX_VALUE;
			bestIs.add(maxI);
		}
		return maxI;
	}

	private class ratioNode implements Comparable<ratioNode>{
		int x;
		double ratio;

		public ratioNode(int x, double ratio) {
			this.x = x;
			this.ratio = ratio;
		}

		@Override
		public int compareTo(ratioNode o) {
			if (this.ratio - o.ratio > 0) {
				return 1;
			} else if (this.ratio - o.ratio < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
