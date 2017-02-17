package archive;
import java.util.ArrayList;
import java.util.Comparator;


public class KnapsackSol implements Comparable<KnapsackSol>, Comparator<KnapsackSol>{

	static Cubic c;
	private ArrayList<Integer> x;
	private ArrayList<Integer> r;
	private boolean[] xVals;
	private boolean valid;
	private long obj;
	private long objGA;
	private int totalA;

	public KnapsackSol(KnapsackSol ks) {
		xVals = new boolean[c.getN()];
		valid = ks.getValid();
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (Integer i : ks.getX()) {
			x.add(i);
			xVals[i] = true;
		}
		for (Integer i : ks.getR()) {
			r.add(i);
		}
		obj = ks.getObj();
		objGA = ks.getObj();
		totalA = ks.getTotalA();
	}

	public KnapsackSol(boolean[] xVals) {
		this.xVals = xVals;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				x.add(i);
			} else {
				r.add(i);
			}
		}
		obj = c.updateObj(x);
		objGA = obj;
		calcTotalA();
		updateValid();
	}

	public KnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		xVals = new boolean[c.getN()];
		this.x = x;
		this.r = r;
		for (Integer i: x) {
			xVals[i] = true;
		}
		this.obj = c.updateObj(x);
		this.objGA = obj;
	}

	public KnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r, long obj, int totalA) {
		xVals = new boolean[c.getN()];
		this.x = x;
		this.r = r;
		for (Integer i: x) {
			xVals[i] = true;
		}
		this.obj = obj;
		this.objGA = obj;
		this.totalA = totalA;
		updateValid();
	}

	public void update(ArrayList<Integer> x, ArrayList<Integer> r, long obj, int totalA) {
		this.x = new ArrayList<Integer>(x);
		this.r = new ArrayList<Integer>(r);
		for (Integer i: x) {
			xVals[i] = true;
		}
		for (Integer i: r) {
			xVals[i] = false;
		}
		this.obj = obj;
		this.objGA = obj;
		this.totalA = totalA;
		updateValid();
	}

	private void updateValid() {
		if (totalA <= c.getB()) {
			valid = true;
		} else {
			valid = false;
			objGA = 0;
		}
	}

	public long swapObj(int i, int j) {
		long oldObj = obj;
		oldObj = oldObj - c.getCi(i);
		oldObj = oldObj + c.getCi(j);
		for (int k = 0; k < x.size(); k++) {
			int xk = x.get(k);
			if (xk != i) {
				oldObj = oldObj - 2*c.getCij(i,xk);
				oldObj = oldObj + 2*c.getCij(j,xk);
				for (int l = k+1; l < x.size(); l++) {
					int xl = x.get(l);
					if (xl != i) {
						oldObj = oldObj - 6*c.getDijk(i,xk,xl);
						oldObj = oldObj + 6*c.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Swap the boolean values in the current x array at indexes i and j
	 * @param curX
	 * @param i
	 * @param j
	 */
	public void swap(long newObj, int i, int j) {
		this.obj = newObj;
		this.objGA = newObj;
		this.totalA = this.totalA + c.getA(j) - c.getA(i);
		updateValid();
		xVals[i] = false;
		xVals[j] = true;
		remove(x,i);
		x.add(j);
		remove(r,j);
		r.add(i);
	}

	// Try to add a variable to the solution
	public int tryImproveAdd() {
		if (x.size() == c.getN()) {
			return 0;
		}
		int b = c.getB();
		double maxRatio = Double.MIN_VALUE;
		int maxI = -1;
		for (Integer ri: r) {
			if (totalA + c.getA(ri) <= b) {
				double ratio = c.getRatio(ri);
				if (ratio > maxRatio) {
					int objChange = c.getCi(ri);
					for (int i = 0; i < x.size(); i++) {
						int xi = x.get(i);
						objChange += 2*c.getCij(ri,xi);
						for (int j = i+1; j < x.size(); j++) {
							int xj = x.get(j);
							objChange += 6*c.getDijk(ri, xi, xj);
						}
					}
					if (objChange > 0) {
						maxRatio = ratio;
						maxI = ri;
					}
				}
			}
		}
		if (maxI != -1) {
			xVals[maxI] = true;
			x.add(maxI);
			remove(r, maxI);
			totalA = totalA + c.getA(maxI);
			int objChange = c.getCi(maxI);
			for (int i = 0; i < x.size(); i++) {
				int xi = x.get(i);
				objChange += 2*c.getCij(maxI,xi);
				for (int j = i+1; j < x.size(); j++) {
					int xj = x.get(j);
					objChange += 6*c.getDijk(maxI, xi, xj);
				}
			}
			this.obj += objChange;
			this.objGA = obj;
			updateValid();
			return maxI;
		}
		return -1;
	}

	public int tryImproveSub() {
		if (x.size() <= 1) {
			return 0;
		}
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		for (Integer si: x) {
			double ratio = c.getRatio(si);
			if (ratio < minRatio) {
				int objChange = -1*c.getCi(si);
				for (int i = 0; i < x.size(); i++) {
					int xi = x.get(i);
					objChange -= 2*c.getCij(si,xi);
					for (int j = i+1; j < x.size(); j++) {
						int xj = x.get(j);
						objChange -= 6*c.getDijk(si, xi, xj);
					}
				}
				if (objChange > 0) {
					minRatio = ratio;
					minI = si;
				}
			}
		}
		if (minI != -1) {
			xVals[minI] = false;
			r.add(minI);
			remove(x, minI);
			totalA = totalA - c.getA(minI);
			int objChange = -1*c.getCi(minI);
			for (int i = 0; i < x.size(); i++) {
				int xi = x.get(i);
				objChange -= 2*c.getCij(minI,xi);
				for (int j = i+1; j < x.size(); j++) {
					int xj = x.get(j);
					objChange -= 6*c.getDijk(minI,xi,xj);
				}
			}
			this.obj += objChange;
			this.objGA = obj;
			updateValid();
			return minI;
		}
		return -1;
	}

	// Try to add a variable to the solution
	public int tryAdd() {
		if (x.size() == c.getN()) {
			return 0;
		}
		int b = c.getB();
		double maxRatio = Double.MIN_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + c.getA(i) <= b) {
				double ratio = c.getRatio(i);
				if (ratio > maxRatio) {
					maxRatio = ratio;
					maxI = i;
				}
			}
		}
		if (maxI != -1) {
			xVals[maxI] = true;
			x.add(maxI);
			remove(r, maxI);
			totalA = totalA + c.getA(maxI);
			int objChange = c.getCi(maxI);
			for (int i = 0; i < x.size(); i++) {
				int xi = x.get(i);
				objChange += 2*c.getCij(maxI,xi);
				for (int j = i+1; j < x.size(); j++) {
					int xj = x.get(j);
					objChange += 6*c.getDijk(maxI, xi, xj);
				}
			}
			this.obj += objChange;
			this.objGA = obj;
			updateValid();
			return maxI;
		}
		return -1;
	}

	public int trySub() {
		if (x.size() <= 1) {
			return -1;
		}
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		for (Integer i: x) {
			double ratio = c.getRatio(i);
			if (ratio < minRatio) {
				minRatio = ratio;
				minI = i;
			}
		}
		if (minI != -1) {
			xVals[minI] = false;
			r.add(minI);
			remove(x, minI);
			totalA = totalA - c.getA(minI);
			int objChange = -1*c.getCi(minI);
			for (int i = 0; i < x.size(); i++) {
				int xi = x.get(i);
				objChange -= 2*c.getCij(minI,xi);
				for (int j = i+1; j < x.size(); j++) {
					int xj = x.get(j);
					objChange -= 6*c.getDijk(minI,xi,xj);
				}
			}
			this.obj += objChange;
			this.objGA = obj;
			updateValid();
			return minI;
		}
		return -1;
	}

	public int getXSize() {
		return x.size();
	}

	public int getXItem(int i) {
		if (i >= 0 && i < x.size()) {
			return x.get(i);
		} else {
			return -1;
		}
	}

	public int getRSize() {
		return r.size();
	}

	public int getRItem(int i) {
		if (i >= 0 && i < r.size()) {
			return r.get(i);
		} else {
			return -1;
		}
	}

	public ArrayList<Integer> getX() {
		return x;
	}

	public ArrayList<Integer> getR() {
		return r;
	}

	public long getObj() {
		return obj;
	}

	public void setObj(long obj) {
		this.obj = obj;
	}

	public long getObjGA() {
		return objGA;
	}

	public void setObjGA(long objGA) {
		this.objGA = objGA;
	}

	public void calcTotalA() {
		int totalA = 0;
		for (Integer i: x) {
			totalA += c.getA(i);
		}
		setTotalA(totalA);
	}

	public int getTotalA() {
		return totalA;
	}

	public void setTotalA(int totalA) {
		this.totalA = totalA;
	}

	public boolean getValid() {
		return valid;
	}

	public void setValid(boolean validity) {
		valid = validity;
	}

	public boolean getXVals(int i) {
		return xVals[i];
	}

	private void remove(ArrayList<Integer> arr, int i) {
		arr.remove(Integer.valueOf(i));
	}

	@Override
	public int compareTo(KnapsackSol o) {
		long diff = this.getObjGA() - o.getObjGA();
		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		} else {
			long diff2 = this.getObj() - o.getObj();
			if (diff2 >= 0) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	@Override
	public boolean equals(Object object) {
		KnapsackSol ks2 = (KnapsackSol)object;
		if (this.getObj() == ks2.getObj()) {
			for (int i = 0; i < c.getN(); i++) {
				if (this.getXVals(i) != ks2.getXVals(i)) {
					return false;
				}
			}
			return true;
		} 
		return false;
	}

	public static void main(String[] args) {
		Cubic c = new Cubic(10,false);
		KnapsackSol.c = c;
		ArrayList<KnapsackSol> population = new ArrayList<KnapsackSol>();
		for (int i = 0; i < 10; i++) {
			ArrayList<Integer> x = new ArrayList<Integer>();
			ArrayList<Integer> r = new ArrayList<Integer>();
			//			c.genInit(new ArrayList<Integer>(),x,r);
			c.genRndInit(new ArrayList<Integer>(),x,r);
			long curObj = c.getIncObj();
			int curTotalA = 0;
			for (Integer j: x) {
				curTotalA += c.getA(j);
			}
			population.add(new KnapsackSol(x, r, curObj, curTotalA));
		}

		KnapsackSol ks = population.get(3);
		System.out.println(ks.getObj());

		KnapsackSol ks2 = new KnapsackSol(ks);
		ks2.setObj(12345);

		ks = population.get(3);
		System.out.println(ks.getObj());
	}

	@Override
	public int compare(KnapsackSol o1, KnapsackSol o2) {
		long diff = o1.getObjGA() - o2.getObjGA();
		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		} else {
			return 0;
		}
	}
}
