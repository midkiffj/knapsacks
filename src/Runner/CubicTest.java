package Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Constructive.CubicDP;
import Constructive.CubicFillUp;
import Constructive.CubicGreedy;
import Constructive.CubicGreedyFill;
import Constructive.CubicGreedyMax;
import ExactMethods.Cubic_Forrester;
import Problems.Cubic;
import Problems.ProblemFactory;
import Solutions.CubicSol;
import Solutions.KnapsackSol;

/**
 * Cubic test bed runner
 * 
 * @author midkiffj
 */
public class CubicTest extends ProblemTest {

	// Test Bed specification
	private double[] densities = {0.25, 0.5, 0.75, 1};
	private int[] probSizes = {10, 20, 30, 50, 100, 200, 500, 1000};
	private int K = 10;

	// Method usage
	private boolean generate;
	private boolean runHeuristics;
	private boolean runMIP;

	// Folders
	private static final String incuFolder = "incumbents/cubic/";
	private static final String probFolder = "problems/cubic/";
	private static final String resFolder = "results/cubic/";

	/**
	 * Setup options for testing
	 * 
	 * @param gen - (T) generate problems
	 * @param rh - (T) run heuristics
	 * @param mip - (T) run MIP exact method
	 * @param useLog - (T) log testing
	 */
	public CubicTest(boolean gen, boolean rh, boolean mip, boolean useLog) {
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
		int seed = 200;
		for (int k = 0; k < K; k++) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					seed++;
					Cubic c1 = new Cubic(n,false,seed,density);
					KnapsackSol ks1 = (KnapsackSol)ProblemFactory.genInitSol();
					String file1 = n+"_"+density+"_false_"+k;
					System.out.println("--"+file1+"--");
					c1.toFile(probFolder+file1);
					ks1.writeSolution(incuFolder+file1+"inc.txt");

					testObj.put(file1, c1.getObj(test));

					seed++;
					Cubic c2 = new Cubic(n,true,seed,density);
					KnapsackSol ks2 = (KnapsackSol)ProblemFactory.genInitSol();
					String file2 = n+"_"+density+"_true_"+k;
					System.out.println("--"+file2+"--");
					c2.toFile(probFolder+file2);
					ks2.writeSolution(incuFolder+file2+"inc.txt");

					testObj.put(file2, c2.getObj(test));
				}
			}
		}
		readTestBed();
	}

	/**
	 * Read in the test bed and test calculated objective value 
	 * - Compare with previously found dummy solution
	 */
	private void readTestBed() {
		for (int k = 0; k < 10; k++) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];

					String file1 = n+"_"+density+"_false_"+k;
					Cubic c1 = new Cubic(probFolder+file1);
					if(c1.getObj(test) != testObj.get(file1)) {
						System.err.println(file1 + " incorrect");
					}

					String file2 = n+"_"+density+"_true_"+k;
					Cubic c2 = new Cubic(probFolder+file2);
					if(c2.getObj(test) != testObj.get(file2)) {
						System.err.println(file2 + " incorrect");
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
		pw = new PrintWriter(resFolder+"cubHeuristics.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,density,#,negCoef,incumbent,GA,SA,ST,TS");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < K; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					TestLogger.setFile("cubic/"+file1);
					System.out.println("--"+file1+"--");
					@SuppressWarnings("unused")
					Cubic c1 = new Cubic(probFolder+file1);
					CubicSol cs1 = new CubicSol(incuFolder+file1+"inc.txt");
					double incumbent1 = cs1.getObj();

					String result1;
					HeuristicRunner hr1 = new HeuristicRunner(cs1);
					result1 = hr1.getResults();


					if (k == 0) {
						pw.println(n+","+density+","+k+",false,"+incumbent1+","+result1);
					} else {
						pw.println(",,"+k+",false,"+incumbent1+","+result1);
					}
				}
				for (int k = 0; k < K; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					TestLogger.setFile("cubic/"+file2);
					System.out.println("--"+file2+"--");
					@SuppressWarnings("unused")
					Cubic c2 = new Cubic(probFolder+file2);
					CubicSol cs2 = new CubicSol(incuFolder+file2+"inc.txt");
					double incumbent2 = cs2.getObj();

					String result2;
					HeuristicRunner hr2 = new HeuristicRunner(cs2);
					result2 = hr2.getResults();


					if (k == 0) {
						pw.println(n+","+density+","+k+",true,"+incumbent2+","+result2);
					} else {
						pw.println(",,"+k+",true,"+incumbent2+","+result2);
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
		PrintWriter pw = new PrintWriter(resFolder+"cubMIP.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,density,#,negCoef,incumbent,MIP,gap,bestBound,timeout");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < K; k++) {
					long result1 = -1;
					double incObj1 = -1;
					String timeout1 = "";
					double gap1 = -1;
					double bestBound1 = -1;
					if (n <= 200) {
						String file1 = n+"_"+density+"_false_"+k;
						System.err.println(file1);
						@SuppressWarnings("unused")
						Cubic c1 = new Cubic(probFolder+file1);
						CubicSol cs1 = new CubicSol(incuFolder+file1+"inc.txt");
						incObj1 = cs1.getObj();
						String[] args1 = {file1};

						Cubic_Forrester.main(args1);
						result1 = Cubic_Forrester.getBestObj();
						gap1 = Cubic_Forrester.getGap();
						if (Cubic_Forrester.getTimeout()) {
							timeout1 = "*";
						}
						bestBound1 = (gap1*result1)+result1;
					}

					if (k == 0) {
						pw.println(n+","+density+","+k+",false,"+incObj1+","+result1+","+gap1+","+bestBound1+","+timeout1);
					} else {
						pw.println(",,"+k+",false,"+incObj1+","+result1+","+gap1+","+bestBound1+","+timeout1);
					}
				}
				for (int k = 0; k < K; k++) {
					long result2 = -1;
					double incObj2 = -1;
					String timeout2 = "";
					double gap2 = -1;
					double bestBound2 = -1;
					if (n <= 200) {
						String file2 = n+"_"+density+"_true_"+k;
						System.err.println(file2);
						@SuppressWarnings("unused")
						Cubic c2 = new Cubic(probFolder+file2);
						CubicSol cs2 = new CubicSol(incuFolder+file2+"inc.txt");
						incObj2 = cs2.getObj();
						String[] args2 = {file2};

						Cubic_Forrester.main(args2);
						result2 = Cubic_Forrester.getBestObj();
						gap2 = Cubic_Forrester.getGap();
						if (Cubic_Forrester.getTimeout()) {
							timeout2 = "*";
						}
						bestBound2 = (gap2*result2)+result2;
					}

					if (k == 0) {
						pw.println(n+","+density+","+k+",true,"+incObj2+","+result2+","+gap2+","+bestBound2+","+timeout2);
					} else {
						pw.println(",,"+k+",true,"+incObj2+","+result2+","+gap2+","+bestBound2+","+timeout2);
					}
				}
			}
		}
		pw.close();
	}

	/**
	 * Run and time the test bed on the 4 cubic constructive heuristics
	 * 
	 * @throws FileNotFoundException
	 */
	public void runConstructive() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"cubConst.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,density,#,negCoef,incumbent,DP,Greedy,GreedyMax,Fill,Hybrid,,Times(min):,incumbent,DP,Greedy,GreedyMax,Fill,Hybrid");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < K; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					TestLogger.setFile("cubic/"+file1);
					System.out.println("--"+file1+"--");
					Cubic c1 = new Cubic(probFolder+file1);

					String result1;
					result1 = runConst(c1);


					if (k == 0) {
						pw.println(n+","+density+","+k+",false,"+result1);
					} else {
						pw.println(",,"+k+",false,"+result1);
					}
				}
				for (int k = 0; k < K; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					TestLogger.setFile("cubic/"+file2);
					System.out.println("--"+file2+"--");
					Cubic c2 = new Cubic(probFolder+file2);

					String result2;
					result2 = runConst(c2);


					if (k == 0) {
						pw.println(n+","+density+","+k+",true,"+result2);
					} else {
						pw.println(",,"+k+",true,"+result2);
					}
				}
			}
		}
		pw.close();
	}

	/**
	 * Runs the cubic problem with all constructive heuristics
	 * - Returns a comma-delimited string of results
	 * 
	 * @param Cubic problem to solve
	 */
	private static String runConst(Cubic c) {
		System.out.println("--Starting Inc");
		long start = System.nanoTime();
		KnapsackSol ks1 = (KnapsackSol)ProblemFactory.genInitSol();
		long end = System.nanoTime();
		double incObj = ks1.getObj();
		double incTime = (double)(end-start)/60000000000L;

		double cdpBest = -1;
		double cdpTime = -1;
		if (c.getN() <= 200) {
			System.out.println("--Starting DP");
			CubicDP cdp = new CubicDP(c);
			cdp.run();
			cdpBest = cdp.getResult().getObj();
			cdpTime = cdp.getTime();
		}

		System.out.println("--Starting Greedy");
		CubicGreedy cg = new CubicGreedy(c);
		cg.run();
		double greedy = cg.getResult().getObj();
		double greedyTime = cg.getTime();

		System.out.println("--Starting GreedyMax");
		CubicGreedyMax cgm = new CubicGreedyMax(c);
		cgm.run();
		double greedyMax = cgm.getResult().getObj();
		double greedyMaxTime = cgm.getTime();

		double fill = -1;
		double fillTime = -1;
		if (c.getN() <= 200) {
			System.out.println("--Starting Fill");
			CubicFillUp cfu = new CubicFillUp(c);
			cfu.run();
			fill = cfu.getResult().getObj();
			fillTime = cfu.getTime();
		}

		double hybrid = -1;
		double hybridTime = -1;
		if (c.getN() <= 200) {
			System.out.println("--Starting Hybrid");
			CubicGreedyFill cgf = new CubicGreedyFill(c);
			cgf.run();
			hybrid = cgf.getResult().getObj();
			hybridTime = cgf.getTime();
		}

		String ret = incObj + "," + cdpBest + "," + greedy + "," + greedyMax + "," + fill + "," + hybrid + ",,," + incTime + "," + cdpTime + "," + greedyTime + "," + greedyMaxTime + "," + fillTime + "," + hybridTime;
		return ret;
	}

	/**
	 * Run and time the incumbent heuristic on the test bed
	 * 
	 * @throws FileNotFoundException
	 */
	public void timeIncumbent() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"cubIncumbent.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,density,#,negCoef,incumbent,time(min)");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < K; k++) {

					String file1 = n+"_"+density+"_false_"+k;
					System.out.println("--"+file1+"--");

					@SuppressWarnings("unused")
					Cubic c1 = new Cubic(probFolder+file1);
					long start = System.nanoTime();
					KnapsackSol ks1 = (KnapsackSol)ProblemFactory.genInitSol();
					long end = System.nanoTime();
					double incumbent1 = ks1.getObj();
					double duration1 = (double)(end-start)/60000000000L;

					if (k == 0) {
						pw.println(n+","+density+","+k+",false,"+incumbent1+","+duration1);
					} else {
						pw.println(",,"+k+",false,"+incumbent1+","+duration1);
					}
				}
				for (int k = 0; k < K; k++) {

					String file2 = n+"_"+density+"_true_"+k;
					System.out.println("--"+file2+"--");

					@SuppressWarnings("unused")
					Cubic c2 = new Cubic(probFolder+file2);
					long start = System.nanoTime();
					KnapsackSol ks2 = (KnapsackSol)ProblemFactory.genInitSol();
					long end = System.nanoTime();
					double incumbent2 = ks2.getObj();
					double duration2 = (double)(end-start)/60000000000L;

					if (k == 0) {
						pw.println(n+","+density+","+k+",false,"+incumbent2+","+duration2);
					} else {
						pw.println(",,"+k+",false,"+incumbent2+","+duration2);
					}
				}
			}
		}
		pw.close();
	}

	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runHeuristics()
	 */
	public void runHealHeuristics() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"cubHealHeuristics.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,density,#,negCoef,incumbent,GA,SA,ST,TS");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < K; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					TestLogger.setFile("cubic/"+file1);
					System.out.println("--"+file1+"--");
					@SuppressWarnings("unused")
					Cubic c1 = new Cubic(probFolder+file1);
					CubicSol cs1 = new CubicSol(incuFolder+file1+"inc.txt");
					cs1.setHealing(true);
					double incumbent1 = cs1.getObj();

					String result1;
					HeuristicRunner hr1 = new HeuristicRunner(cs1);
					result1 = hr1.getResults();


					if (k == 0) {
						pw.println(n+","+density+","+k+",false,"+incumbent1+","+result1);
					} else {
						pw.println(",,"+k+",false,"+incumbent1+","+result1);
					}
					cs1.setHealing(false);
				}
				for (int k = 0; k < K; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					TestLogger.setFile("cubic/"+file2);
					System.out.println("--"+file2+"--");
					@SuppressWarnings("unused")
					Cubic c2 = new Cubic(probFolder+file2);
					CubicSol cs2 = new CubicSol(incuFolder+file2+"inc.txt");
					cs2.setHealing(true);
					double incumbent2 = cs2.getObj();

					String result2;
					HeuristicRunner hr2 = new HeuristicRunner(cs2);
					result2 = hr2.getResults();


					if (k == 0) {
						pw.println(n+","+density+","+k+",true,"+incumbent2+","+result2);
					} else {
						pw.println(",,"+k+",true,"+incumbent2+","+result2);
					}
					cs2.setHealing(false);
				}
			}
		}
		pw.close();
	}
}
