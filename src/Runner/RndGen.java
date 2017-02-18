package Runner;

import java.util.Random;

/**
 * Random object for heuristics and problem solutions.
 * 
 * @author midkiffj
 *
 */
public class RndGen {
	
	private static Random rnd = new Random(1234);
	
	public static Random getRnd() {
		return rnd;
	}
}
