package Runner;

import java.util.Random;

public class RndGen {
	
	private static Random rnd = new Random(1234);
	
	public static Random getRnd() {
		return rnd;
	}
}
