package Solutions;

/**
 * Node used to link items to their objective contribution and 
 * 	ratio of potential contribution to weight. Both differ by problem.
 * 
 * @author midkiffj
 */
public class ratioNode implements Comparable<ratioNode>{
	public int x;
	public long objChange;
	public double ratio;

	/**
	 * Construct a node with the given item and ratio
	 * 
	 * @param x - item
	 * @param ratio - potential contribution to weight
	 */
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
