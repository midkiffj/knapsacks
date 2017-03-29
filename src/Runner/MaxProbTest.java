package Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Constructive.MaxProbUMax;
import ExactMethods.MaxProb_Bill;
import Heuristics.Metaheuristic;
import Heuristics.genAlgo;
import Problems.MaxProbability;
import Problems.ProblemFactory;
import Solutions.MaxProbabilitySol;
import Solutions.ProblemSol;

/**
 * Max Probability test bed runner
 * 
 * @author midkiffj
 */
public class MaxProbTest extends ProblemTest {

	// Test bed specifications
	private int[] sizes = {100, 500, 1000};
	private int[] possibleK = {65, 75, 85, 95};
	private int[] possibleP = {5, 10, 30, 50, 75};
	private int num = 5;

	// Usage booleans
	boolean generate;
	boolean runHeuristics;
	boolean runMIP;

	// Folders
	private static final String incuFolder = "incumbents/mp/";
	private static final String probFolder = "problems/mp/";
	private static final String resFolder = "results/mp/";

	/**
	 * Setup options for testing
	 * 
	 * @param gen - (T) generate problems
	 * @param rh - (T) run heuristics
	 * @param mip - (T) run MIP exact method
	 * @param useLog - (T) log testing
	 */
	public MaxProbTest(boolean gen, boolean rh, boolean mip, boolean useLog) {
		super(useLog);
		generate = gen;
		runHeuristics = rh;
		runMIP = mip;
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#run()
	 */
	public void run() throws FileNotFoundException {
		if (generate) {
			generate();
		}

		if (runHeuristics) {
			runHeuristics();
		}

		if (runMIP) {
			runMIP();
		}
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#generate()
	 */
	public void generate() {
		for (int n: sizes) {
			for (int i = 0; i < num; i++) {
				for (int p: possibleP) {
					for (int k: possibleK) {
						MaxProbability mp = new MaxProbability(n, false, i+n, k, p);
						MaxProbabilitySol mps = (MaxProbabilitySol)ProblemFactory.genInitSol();
						String file = n+"_P"+p+"_K"+k+"_"+i;
						System.out.println(file);
						if (!mps.getValid()) {
							System.err.println("Invalid answer:" + file);
						}
						mp.toFile(probFolder+file);
						mps.writeSolution(incuFolder+file+"inc.txt");

						testObj.put(file, mp.getObj(test));

						MaxProbability mp2 = new MaxProbability("problems/mp/"+file);
						if(mp2.getObj(test) != testObj.get(file)) {
							System.err.println(file + " incorrect");
						}
					}
				}
			}
		}
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runHeuristics()
	 */
	public void runHeuristics() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"maxProbHeuristics.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,#,P,K,incumbent,GA,SA,ST,TS");
		for (int n: sizes) {
			for (int i = 0; i < num; i++) {
				for (int p: possibleP) {
					for (int k: possibleK) {
						String file = n+"_P"+p+"_K"+k+"_"+i;
						@SuppressWarnings("unused")
						MaxProbability mp2 = new MaxProbability(probFolder+file);
						MaxProbabilitySol mps = new MaxProbabilitySol(incuFolder+file+"inc.txt");
						TestLogger.setFile("mp/"+file);
						System.out.println("--"+file+"--");
						double incumbent1 = mps.getObj();

						HeuristicRunner hr = new HeuristicRunner(mps);
						String result1 = hr.getResults();

						if (k == 65) {
							pw.println(n+","+i+","+p+","+k+","+incumbent1+","+result1);
						} else {
							pw.println(",,,"+k+","+incumbent1+","+result1);
						}
					}
				}
			}
		}
		pw.close();
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runMIP()
	 */
	public void runMIP() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"maxProbMIP.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,#,P,K,incumbent,MIP,gap,bestBound,timeout");
		for (int n: sizes) {
			for (int i = 0; i < num; i++) {
				for (int p: possibleP) {
					for (int k: possibleK) {
						String file = n+"_P"+p+"_K"+k+"_"+i;
						System.out.println("--"+file+"--");
						@SuppressWarnings("unused")
						MaxProbability mp = new MaxProbability(probFolder+file);
						MaxProbabilitySol mps = new MaxProbabilitySol(incuFolder+file+"inc.txt");
						double incumbent1 = mps.getObj();

						String[] args = {file};
						MaxProb_Bill.main(args);

						double result1 = MaxProb_Bill.getBestObj();
						double gap1 = MaxProb_Bill.getGap();
						String timeout = "";
						if (MaxProb_Bill.getTimeout()) {
							timeout = "*";
						}
						double bestBound1 = (gap1*result1)+result1;

						if (k == 65) {
							pw.println(n+","+i+","+p+","+k+","+incumbent1+","+result1+","+gap1+","+bestBound1+","+timeout);
						} else {
							pw.println(",,,"+k+","+incumbent1+","+result1+","+gap1+","+bestBound1+","+timeout);
						}
					}
				}
			}
		}
		pw.close();
	}

	/**
	 * Run the genetic algorithm on the test bed
	 * 
	 * @throws FileNotFoundException
	 */
	public void runMaxProbGA() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"maxProbGA.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,#,P,K,incumbent,GA");
		for (int n: sizes) {
			for (int i = 0; i < num; i++) {
				for (int p: possibleP) {
					for (int k: possibleK) {
						String file = n+"_P"+p+"_K"+k+"_"+i;
						@SuppressWarnings("unused")
						MaxProbability mp = new MaxProbability(probFolder+file);
						MaxProbabilitySol mps = new MaxProbabilitySol(incuFolder+file+"inc.txt");
						TestLogger.setFile("mp/"+file);
						System.out.println("--"+file+"--");
						double incumbent1 = mps.getObj();

						TestLogger.setLogger("GA");
						genAlgo ga = new genAlgo(mps,-1,-1);
						System.out.println("--GA--");
						String gas = run(ga);

						if (k == 65) {
							pw.println(n+","+i+","+p+","+k+","+incumbent1+","+gas);
						} else {
							pw.println(",,,"+k+","+incumbent1+","+gas);
						}
					}
				}
			}
		}
		pw.close();
	}

	/**
	 * Runs a given heuristic and returns the best solution
	 * 
	 * @param m - metaheuristic to run
	 * @return String containing best objective found
	 */
	private static String run(Metaheuristic m) {
		long start = System.nanoTime();
		m.run();
		long end = System.nanoTime();
		double duration = (double)(end-start)/60000000000L;
		System.out.println("Time taken(min): " + duration);
		String best = ""+m.getBestObj();

		// Detect invalid solution
		ProblemSol ps = m.getBest();
		if (ps.getValid()) {
			return best;
		} else {
			System.out.println("Invalid Solution found: " + ps.getObj());
			return "INVALID";
		}
	}
	
	/**
	 * Run the genetic algorithm on the test bed
	 * 
	 * @throws FileNotFoundException
	 */
	public void runMaxProbConst() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"maxProbUMax.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,#,P,K,uMax,uMaxCplex,,Time(min):,uMax,uMaxCplex");
		for (int n: sizes) {
			for (int i = 0; i < num; i++) {
				for (int p: possibleP) {
					for (int k: possibleK) {
						String file = n+"_P"+p+"_K"+k+"_"+i;
						MaxProbability mp = new MaxProbability(probFolder+file);
						System.out.println("--"+file+"--");

						System.out.println("--UMax--");
						MaxProbUMax mpum = new MaxProbUMax(mp, false);
						mpum.run();
						String uMaxObj = "" + mpum.getResult().getObj();
						String uMaxTime = "" + mpum.getTime();
						
						System.out.println("--UMaxCplex--");
						MaxProbUMax mpumc = new MaxProbUMax(mp, true);
						mpumc.run();
						String uMaxCObj = "" + mpumc.getResult().getObj();
						String uMaxCTime = "" + mpumc.getTime();

						if (k == 65) {
							pw.println(n+","+i+","+p+","+k+","+uMaxObj+","+uMaxCObj+",,,"+uMaxTime+","+uMaxCTime);
						} else {
							pw.println(",,,"+k+","+uMaxObj+","+uMaxCObj+",,,"+uMaxTime+","+uMaxCTime);
						}
					}
				}
			}
		}
		pw.close();
	}

}
