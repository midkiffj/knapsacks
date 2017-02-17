package archive;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class CubicTS {

	private int n;
	private Random rnd;


	private Cubic c;

	private KnapsackSol current;
	private KnapsackSol best;
	private int[][] tabuList;
	private int shiftTabu;
	private int tabuDuration;

	public CubicTS(Cubic c) {
		this.c = c;
		KnapsackSol.c = c;
		n = c.getN();
		rnd = new Random(1234);

		try {
			PrintStream ps = System.out;
			System.setOut(new PrintStream(n+"TS.txt"));
			System.setOut(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		c.genInit(new ArrayList<Integer>(),x,r);
		//		c.genRndInit(new ArrayList<Integer>(),x,r);
		long curObj = c.getIncObj();
		//		System.out.println("Starting Obj: " + curObj);
		//		System.out.println(x.toString());
		int curTotalA = c.getIncTotalA();
		current = new KnapsackSol(x, r, curObj, curTotalA);
		ArrayList<Integer> bx = new ArrayList<Integer>(x);
		ArrayList<Integer> br = new ArrayList<Integer>(r);
		best = new KnapsackSol(bx, br, curObj, curTotalA);
		// Time vars
		long min = 60000000000L;
		long time = 5*min;
		tabuSearch(time);
	}
	/**
	 * Setup and run a tabu search algorithm on a cubic knapsack problem
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			CubicTS cts = new CubicTS(new Cubic(args[0]));
		} else {
			Cubic c = new Cubic(30, false);
			CubicTS cts = new CubicTS(c);
		}
	}

	public long getBestObj() {
		return best.getObj();
	}

	private void tabuSearch(long time) {
		tabuList = new int[n][n];
		tabuDuration = (int)Math.round(7.5*Math.log(n));
		int stuck = 0;
		int bestNotUpdated = 0;
		int diversified = 1;
		// Penalty arrays
		int[] penPoints = new int[n];
		// Update points
		for (int p = 0; p < current.getXSize(); p++) {
			int xi = current.getXItem(p);
			penPoints[xi] = 1;
		}
		long start = System.nanoTime();
		long end = start;
		for (int i = 1; i < n*100000 && (end-start) < time; i++) {

			if (bestNotUpdated >= 10*n*diversified) {
				bestNotUpdated = 0;
				diversified++;
				//				System.out.println("Diversifying...");
				for (int d = 0; d < 20; d++) {
					long[][] swap = bestSwap(Integer.MAX_VALUE);
					if (swap != null) {
						long newObj = swap[1][0];
						int j = (int) swap[1][1];
						int k = (int) swap[1][2];
						current.swap(newObj,j,k);
						if (current.getObj() > best.getObj()) {
							best.update(current.getX(), current.getR(), current.getObj(), current.getTotalA());
							//							System.out.println("Best updated from diversification to " + current.getObj());
						}
					}
				}
				//				diversify(penPoints, i);
				//				System.out.println("Div: " + current.getObj() + " curTotalA: " + current.getTotalA());
				//				try {
				//					TimeUnit.SECONDS.sleep(2);
				//				} catch (InterruptedException e) {
				//					System.err.println("Timeunit error");
				//					e.printStackTrace();
				//				}
			}

			// Get best swaps
			long[][] swap = mutate(i);
			if (swap != null) {
				long[] tabu = swap[0];
				long[] nonTabu = swap[1];
				long newObj = -1;
				int j = -1;
				int k = -1;
				// Check if tabu swap better than best
				if (tabu[0] > best.getObj()) {
					newObj = tabu[0];
					j = (int)tabu[1];
					//			if (swapping) {
					k = (int)tabu[2];
					//			}

				}
				// Otherwise, use nonTabu
				else {
					if (nonTabu[1] != -1 && nonTabu[2] != -1) {
						newObj = nonTabu[0];
						j = (int)nonTabu[1];
						//			if (swapping) {
						k = (int)nonTabu[2];
						//			}
					}
				}

				// Perform and tabu swap
				if (j != -1 && k != -1) {
					current.swap(newObj,j,k);
					makeTabu(j,k,i);
					stuck = 0;
				} else {
					//					System.out.println("ERROR: Unable to Swap");
					stuck++;
					if (stuck > 3) {
						shift();
					}
				}
			}

			// Update Best
			if (current.getObj() > best.getObj()) {
				best.update(current.getX(), current.getR(), current.getObj(), current.getTotalA());
				//				System.out.println("Best updated at iteration " + i + " to " + current.getObj());
				bestNotUpdated = 0;
			} else {
				bestNotUpdated++;
			}
			//			System.out.println(i + "- Cur: " + current.getObj() + "(" + current.getObjGA() + ") : " + " Best: " + best.getObj() + "(" + best.getObjGA() + ")");
			//			System.out.println(i + "- curTotalA: " + current.getTotalA());
			//			System.out.println(current.getX().toString());

			// Update points
//			for (int p = 0; p < current.getXSize(); p++) {
//				int xi = current.getXItem(p);
//				penPoints[xi] += 1;
//			}
//			for (int p = 0; p < current.getRSize(); p++) {
//				int xi = current.getRItem(p);
//				penPoints[xi] = 0;
//			}
			end = System.nanoTime();
		}
	}

	private long[][] mutate(int iteration) {
		if (rnd.nextDouble() < 0.6) {
			return maxMinSwap(iteration);
		} else {
			long[] ratioSwap = ratioMutate(iteration);
			if (ratioSwap == null) {
				return null;
			}
			long[][] result = {ratioSwap, ratioSwap};
			return result;
		}
	}

	private long[][] maxMinSwap(int iteration) {
		// Occasionally perform a shift
		if (shiftTabu < iteration) {
			if (rnd.nextDouble() < 0.6) {
				int change = shift();
				if (change != -1) {
					shiftTabu = iteration + (n/4);
					tabuShift(change, iteration);
					return null;
				}
			}
		}
		// Store b
		int b = c.getB();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		long nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		long bObj = Integer.MIN_VALUE;

		int i = minRatio(0);
		int j = maxRatio(0);
		int ki = 0;
		int kj = 0;
		boolean changeI = true;
		while (c.getA(j) - c.getA(i) > b - current.getTotalA() && ki < n) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}
			kj++;
			j = maxRatio(kj);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		long newObj = current.swapObj(i, j);
		bi = i;
		bj = j;
		bObj = newObj;
		if (tabuList[i][j] < iteration) {
			ni = i;
			nj = j;
			nTObj = newObj;
		} else {
			boolean newMin = false;
			while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > b - current.getTotalA() && ki < n) {
				if (newMin) {
					ki++;
					i = minRatio(ki);
					newMin = !newMin;
				}
				kj++;
				j = maxRatio(kj);
				if (kj == n) {
					kj = -1;
					newMin = !newMin;
				}
			}
			if (tabuList[i][j] < iteration) {
				newObj = current.swapObj(i, j);
				ni = i;
				nj = j;
				nTObj = newObj;
				if (newObj > bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		// Compile and return data
		long[][] results = new long[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	private long[] ratioMutate(int iteration) {
		// Get index of min ratio
		int i = minRatio(0);

		// Swap with a random node and return
		int j = rnd.nextInt(current.getRSize());
		j = current.getRItem(j);
		int ki = 0;
		int kj = 0;
		boolean changeI = false;
		while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > c.getB() - current.getTotalA() && ki < n) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}
			kj++;
			j =  rnd.nextInt(current.getRSize());
			j = current.getRItem(j);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		if (c.getA(j) - c.getA(i) > c.getB() - current.getTotalA()) {
			return null;
		}
		long newObj = current.swapObj(i, j);
		long[] result = {newObj, i, j};

		return result;
	}

	private int minRatio(int k) {
		// Find the minimum ratio in the solution
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < current.getXSize()) {
			for (Integer i: current.getX()) {
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

	private int maxRatio(int k) {
		// Find the maximum ratio not in the solution
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < current.getRSize()) {
			for (Integer i: current.getR()) {
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

	// Find the best swap possible that keeps the knapsack feasible
	private long[][] bestSwap(int iteration) {
		// Occasionally perform a shift
		if (shiftTabu < iteration) {
			if (rnd.nextDouble() < 0.6) {
				int change = shift();
				if (change != -1) {
					shiftTabu = iteration + (n/4);
					tabuShift(change, iteration);
					return null;
				}
			}
		}
		// Store b
		int b = c.getB();
		int curTotalA = current.getTotalA();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		long nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		long bObj = Integer.MIN_VALUE;
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					long newObj = current.swapObj(i, j);
					if (newObj > nTObj && tabuList[i][j] < iteration) {
						ni = i;
						nj = j;
						nTObj = newObj;
					}
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		if (ni == -1 && bi == -1) {
			if (rnd.nextDouble() < 0.1) {
				current.trySub();
			}
			return null;
		}
		// Compile and return data
		long[][] results = new long[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	// Return the first improving swap that keeps the knapsack feasible
	private long[][] firstSwap(int iteration) {
		// Occasionally perform a shift
		if (shiftTabu < iteration) {
			if (rnd.nextDouble() < 0.6) {
				int change = shift();
				if (change != -1) {
					shiftTabu = iteration + (n/4);
					tabuShift(change, iteration);
					return null;
				}
			}
		}
		// Store b
		int b = c.getB();
		int curTotalA = current.getTotalA();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		long nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		long bObj = Integer.MIN_VALUE;
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					long newObj = current.swapObj(i, j);
					if (newObj > nTObj && tabuList[i][j] < iteration) {
						ni = i;
						nj = j;
						nTObj = newObj;
					}
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		// Compile and return data
		long[][] results = new long[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	private void tabuShift(int i, int iteration) {
		for (int j = 0; j < i; j++) {
			makeTabu(i,j,iteration);
		}
		for (int j = i+1; j < n; j++) {
			makeTabu(i,j,iteration);
		}
	}

	private void makeTabu(int i, int j, int iteration) {
		tabuList[i][j] = iteration + tabuDuration;
		tabuList[j][i] = iteration + tabuDuration;
	}

	// Shift a variable in or out of the current solution
	private int shift() {
		if (current.getXSize() < 2) {
			return current.tryAdd();
		} else {
			if (rnd.nextDouble() < 0.8) {
				return current.tryAdd();
			} else {
				return current.trySub();
			}
		}
	}

	private void diversify(int[] penPoints, int iteration) {
		ArrayList<Integer> ignore = new ArrayList<Integer>();

		for (int c = 0; c < n/4; c++) {
			int maxPoints = Integer.MIN_VALUE;
			int maxI = -1;
			for (int i = 0; i < n; i++) {
				if (penPoints[i] >= maxPoints && !ignore.contains(i)) {
					maxI = i;
					maxPoints = penPoints[i];
				}
			}
			ignore.add(maxI);
			for (int i = 0; i < n; i++) {
				tabuList[maxI][i] = iteration + tabuDuration;
				tabuList[i][maxI] = iteration + tabuDuration;
			}
		}

		c.genInit(ignore, current.getX(), current.getR());
		long curObj = c.getIncObj();
		int curTotalA = 0;
		for (Integer i: current.getX()) {
			curTotalA += c.getA(i);
		}
		current.setObj(curObj);
		current.setTotalA(curTotalA);
	}

}
