package Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Constructive.CubicMultFillUp;
import Constructive.CubicMultGreedy;
import Constructive.CubicMultGreedyFill;
import Constructive.CubicMultGreedyMax;
import ExactMethods.CubicMult_Forrester;
import Problems.CubicMult;
import Problems.ProblemFactory;
import Solutions.CubicMultSol;

/**
 * Cubic Multiple Knapsack test bed runner
 * 
 * @author midkiffj
 */
public class CubicMultTest extends ProblemTest {

	// Test bed specifications
	int[] knapsacks = {2, 3, 5, 10};
	private double[] densities = {0.25, 0.5, 0.75, 1};
	private int[] probSizes = {10, 20, 30, 50};
	private int K = 10;

	// Method usage
	boolean generate;
	boolean runHeuristics;
	boolean runMIP;

	// Folders
	private static final String incuFolder = "incumbents/cm/";
	private static final String probFolder = "problems/cm/";
	private static final String resFolder = "results/cm/";

	/**
	 * Setup options for testing
	 * 
	 * @param gen - (T) generate problems
	 * @param rh - (T) run heuristics
	 * @param mip - (T) run MIP exact method
	 * @param useLog - (T) log testing
	 */
	public CubicMultTest(boolean gen, boolean rh, boolean mip, boolean useLog) {
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
		int seed = 20000;
		for (int m: knapsacks) {
			for (int k = 0; k < K; k++) {
				for (int i = 0; i < densities.length; i++) {
					double density = densities[i];
					for (int j = 0; j < probSizes.length; j++) {
						int n = probSizes[j];
						seed++;

						CubicMult cm1 = new CubicMult(n,m,false,seed,density);
						CubicMultSol cms1 = (CubicMultSol)ProblemFactory.genInitSol();
						String file1 = n+"_"+m+"_"+density+"_false_"+k;
						cm1.toFile(probFolder+file1);
						cms1.writeSolution(incuFolder+file1+"inc.txt");

						testObj.put(file1, cm1.getObj(test));

						seed++;
						CubicMult cm2 = new CubicMult(n,m,true,seed,density);
						CubicMultSol cms2 = (CubicMultSol)ProblemFactory.genInitSol();
						String file2 = n+"_"+m+"_"+density+"_true_"+k;
						cm2.toFile(probFolder+file2);
						cms2.writeSolution(incuFolder+file2+"inc.txt");

						testObj.put(file2, cm2.getObj(test));
					}
				}
			}
		}
		readCubMult();
	}

	/**
	 * Read in the test bed and test calculated objective value 
	 * - Compare with previously found dummy solution
	 */
	private void readCubMult() {
		for (int m: knapsacks) {
			for (int k = 0; k < K; k++) {
				for (int i = 0; i < densities.length; i++) {
					double density = densities[i];
					for (int j = 0; j < probSizes.length; j++) {
						int n = probSizes[j];

						String file1 = n+"_"+m+"_"+density+"_false_"+k;
						System.out.println(file1);
						CubicMult cm1 = new CubicMult(probFolder+file1);
						if(cm1.getObj(test) != testObj.get(file1)) {
							System.err.println(file1 + " incorrect");
						}

						String file2 = n+"_"+m+"_"+density+"_true_"+k;
						System.out.println(file2);
						CubicMult cm2 = new CubicMult(probFolder+file2);
						if(cm2.getObj(test) != testObj.get(file2)) {
							System.err.println(file2 + " incorrect");
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
		pw = new PrintWriter(resFolder+"cubMultHeuristics.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,m,density,#,negCoef,incumbent,GA,SA,ST,TS");
		for (int m: knapsacks) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					for (int k = 0; k < K; k++) {
						String file1 = n+"_"+m+"_"+density+"_false_"+k;
						TestLogger.setFile("cm/"+file1);
						System.out.println("--"+file1+"--");
						@SuppressWarnings("unused")
						CubicMult c1 = new CubicMult(probFolder+file1);
						CubicMultSol cs1 = new CubicMultSol(incuFolder+file1+"inc.txt");
						double incumbent1 = cs1.getObj();

						String result1;
						HeuristicRunner hr1 = new HeuristicRunner(cs1);
						result1 = hr1.getResults();


						if (k == 0) {
							pw.println(n+","+m+","+density+","+k+",false,"+incumbent1+","+result1);
						} else {
							pw.println(",,,"+k+",false,"+incumbent1+","+result1);
						}
					}
					for (int k = 0; k < K; k++) {
						String file2 = n+"_"+m+"_"+density+"_true_"+k;
						TestLogger.setFile("cm/"+file2);
						System.out.println("--"+file2+"--");
						@SuppressWarnings("unused")
						CubicMult c2 = new CubicMult(probFolder+file2);
						CubicMultSol cs2 = new CubicMultSol(incuFolder+file2+"inc.txt");
						double incumbent2 = cs2.getObj();

						String result2;
						HeuristicRunner hr2 = new HeuristicRunner(cs2);
						result2 = hr2.getResults();

						if (k == 0) {
							pw.println(n+","+m+","+density+","+k+",true,"+incumbent2+","+result2);
						} else {
							pw.println(",,,"+k+",true,"+incumbent2+","+result2);
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
		int[] probSizes = {10,20,30};
		PrintWriter pw;
		pw = new PrintWriter(resFolder+"cubMultMIP.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,m,density,#,negCoef,incumbent,MIP,gap,bestBound,timeout");
		for (int m: knapsacks) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					for (int k = 0; k < K; k++) {
						String file1 = n+"_"+m+"_"+density+"_false_"+k;
						System.out.println("--"+file1+"--");
						@SuppressWarnings("unused")
						CubicMult c1 = new CubicMult(probFolder+file1);
						CubicMultSol cs1 = new CubicMultSol(incuFolder+file1+"inc.txt");
						double incumbent1 = cs1.getObj();

						String[] args1 = {file1};
						long result1;
						CubicMult_Forrester.main(args1);
						result1 = CubicMult_Forrester.getBestObj();
						double gap1 = CubicMult_Forrester.getGap();
						String timeout1 = "";
						if (CubicMult_Forrester.getTimeout()) {
							timeout1 = "*";
						}
						double bestBound1 = (gap1*result1)+result1;

						if (k == 0) {
							pw.println(n+","+m+","+density+","+k+",false,"+incumbent1+","+result1+","+gap1+","+bestBound1+","+timeout1);
						} else {
							pw.println(",,,"+k+",false,"+incumbent1+","+result1+","+gap1+","+bestBound1+","+timeout1);
						}
					}
					for (int k = 0; k < K; k++) {
						String file2 = n+"_"+m+"_"+density+"_true_"+k;
						System.out.println("--"+file2+"--");
						@SuppressWarnings("unused")
						CubicMult c2 = new CubicMult(probFolder+file2);
						CubicMultSol cs2 = new CubicMultSol(incuFolder+file2+"inc.txt");
						double incumbent2 = cs2.getObj();

						String[] args2 = {file2};
						long result2;
						CubicMult_Forrester.main(args2);
						result2 = CubicMult_Forrester.getBestObj();
						double gap2 = CubicMult_Forrester.getGap();
						String timeout2 = "";
						if (CubicMult_Forrester.getTimeout()) {
							timeout2 = "*";
						}
						double bestBound2 = (gap2*result2)+result2;

						if (k == 0) {
							pw.println(n+","+m+","+density+","+k+",true,"+incumbent2+","+result2+","+gap2+","+bestBound2+","+timeout2);
						} else {
							pw.println(",,,"+k+",true,"+incumbent2+","+result2+","+gap2+","+bestBound2+","+timeout2);
						}
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
		pw = new PrintWriter(resFolder+"cubMultConst.csv");
		pw = new PrintWriter(pw,true);
		pw.println("n,m,density,#,negCoef,incumbent,Greedy,GreedyMax,Fill,Hybrid,,Times(min):,incumbent,Greedy,GreedyMax,Fill,Hybrid");

		for (int m: knapsacks) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					for (int k = 0; k < K; k++) {
						String file1 = n+"_"+m+"_"+density+"_false_"+k;
						System.out.println("--"+file1+"--");
						CubicMult c1 = new CubicMult(probFolder+file1);

						String result1;
						result1 = runConst(c1);

						if (k == 0) {
							pw.println(n+","+m+","+density+","+k+",false,"+result1);
						} else {
							pw.println(",,,"+k+",false,"+result1);
						}
					}
					for (int k = 0; k < K; k++) {
						String file2 = n+"_"+m+"_"+density+"_true_"+k;
						System.out.println("--"+file2+"--");
						CubicMult c2 = new CubicMult(probFolder+file2);

						String result2;
						result2 = runConst(c2);

						if (k == 0) {
							pw.println(n+","+m+","+density+","+k+",true,"+result2);
						} else {
							pw.println(",,,"+k+",true,"+result2);
						}
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
	 * @param CubicMult problem for constructing solutions
	 */
	private static String runConst(CubicMult cm) {
		System.out.println("--Starting Inc");
		long start = System.nanoTime();
		CubicMultSol ks1 = (CubicMultSol)ProblemFactory.genInitSol();
		long end = System.nanoTime();
		double incObj = ks1.getObj();
		double incTime = (double)(end-start)/60000000000L;
		
		System.out.println("--Starting Greedy");
		CubicMultGreedy cg = new CubicMultGreedy(cm);
		cg.run();
		double greedy = cg.getResult().getObj();
		double greedyTime = cg.getTime();
		
		System.out.println("--Starting GreedyMax");
		CubicMultGreedyMax cgm = new CubicMultGreedyMax(cm);
		cgm.run();
		double greedyMax = cg.getResult().getObj();
		double greedyMaxTime = cg.getTime();

		System.out.println("--Starting Fill");
		CubicMultFillUp cfu = new CubicMultFillUp(cm);
		cfu.run();
		double fill = cfu.getResult().getObj();
		double fillTime = cfu.getTime();

		System.out.println("--Starting Hybrid");
		CubicMultGreedyFill cgf = new CubicMultGreedyFill(cm);
		cgf.run();
		double hybrid = cgf.getResult().getObj();
		double hybridTime = cgf.getTime();

		String ret =  incObj + "," + greedy + "," + greedyMax + "," + fill + "," + hybrid + ",,," + incTime + "," + greedyTime + "," + greedyMaxTime + "," + fillTime + "," + hybridTime;
		return ret;
	}

}
