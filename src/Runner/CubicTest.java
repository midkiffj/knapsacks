package Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Constructive.CubicDP;
import Constructive.CubicFillUp;
import Constructive.CubicGreedy;
import Constructive.CubicGreedyFill;
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
	boolean generate;
	boolean runHeuristics;
	boolean runMIP;
	
	public CubicTest(boolean gen, boolean rh, boolean mip, boolean useLog) {
		super(useLog);
		generate = gen;
		runHeuristics = rh;
		runMIP = mip;
	}
	
	@Override
	/*
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
	/*
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
					c1.toFile("problems/cubic/"+file1);
					ks1.writeSolution("incumbents/cubic/"+file1+"inc.txt");

					testObj.put(file1, c1.getObj(test));

					seed++;
					Cubic c2 = new Cubic(n,true,seed,density);
					KnapsackSol ks2 = (KnapsackSol)ProblemFactory.genInitSol();
					String file2 = n+"_"+density+"_true_"+k;
					c2.toFile("problems/cubic/"+file2);
					ks2.writeSolution("incumbents/cubic/"+file2+"inc.txt");

					testObj.put(file2, c2.getObj(test));
				}
			}
		}
		readTestBed();
	}
	
	/*
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
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					if(c1.getObj(test) != testObj.get(file1)) {
						System.err.println(file1 + " incorrect");
					}
					
					String file2 = n+"_"+density+"_true_"+k;
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					if(c2.getObj(test) != testObj.get(file2)) {
						System.err.println(file2 + " incorrect");
					}
				}
			}
		}
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runHeuristics()
	 */
	public void runHeuristics() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("tbResults.csv");
		pw.write("n,density,#,negCoef,incumbent,GA,SA,ST,TS\n");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					TestLogger.setFile("cubic/"+file1);
					System.out.println("--"+file1+"--");
					@SuppressWarnings("unused")
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					CubicSol cs1 = new CubicSol("incumbents/cubic/"+file1+"inc.txt");
					double incumbent1 = cs1.getObj();

					String result1;
					HeuristicRunner hr1 = new HeuristicRunner(cs1);
					result1 = hr1.getResults();
					

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent1+","+result1+"\n");
					}
				}
				for (int k = 0; k < 10; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					TestLogger.setFile("cubic/"+file2);
					System.out.println("--"+file2+"--");
					@SuppressWarnings("unused")
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					CubicSol cs2 = new CubicSol("incumbents/cubic/"+file2+"inc.txt");
					double incumbent2 = cs2.getObj();

					String result2;
					HeuristicRunner hr2 = new HeuristicRunner(cs2);
					result2 = hr2.getResults();
					

					if (k == 0) {
						pw.write(n+","+density+","+k+",true,"+incumbent2+","+result2+"\n");
					} else {
						pw.write(",,"+k+",true,"+incumbent2+","+result2+"\n");
					}
				}
			}
		}
		pw.close();
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runMIP()
	 */
	public void runMIP() throws FileNotFoundException {
		PrintWriter pw = new PrintWriter("tbResultsMIP" + probSizes.length + ".csv");
		pw.write("n,density,#,negCoef,incumbent,MIP\n");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					System.err.println(file1);
					@SuppressWarnings("unused")
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					CubicSol cs1 = new CubicSol("incumbents/cubic/"+file1+"inc.txt");
					String[] args1 = {file1};

					long result1;
					if (n >= 100) {
						result1 = -1;
					} else {
						Cubic_Forrester.main(args1);
						result1 = Cubic_Forrester.getBestObj();
					}

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+cs1.getObj()+","+result1+"\n");
					} else {
						pw.write(",,"+k+",false,"+cs1.getObj()+","+result1+"\n");
					}
				}
				for (int k = 0; k < 10; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					System.err.println(file2);
					@SuppressWarnings("unused")
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					CubicSol cs2 = new CubicSol("incumbents/cubic/"+file2+"inc.txt");
					String[] args2 = {file2};

					long result2;
					if (n >= 50) {
						result2 = -1;
					} else {
						Cubic_Forrester.main(args2);
						result2 = Cubic_Forrester.getBestObj();
					}

					if (k == 0) {
						pw.write(n+","+density+","+k+",true,"+cs2.getObj()+","+result2+"\n");
					} else {
						pw.write(",,"+k+",true,"+cs2.getObj()+","+result2+"\n");
					}
				}
			}
		}
		pw.close();
	}
	
	/*
	 * Run and time the test bed on the 4 cubic constructive heuristics
	 */
	public void runConstructive() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("tbResultsConst.csv");
		pw.write("n,density,#,negCoef,incumbent,DP,Greedy,Fill,Hybrid,,Times:,DP,Greedy,Fill,Hybrid\n");
		
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					TestLogger.setFile("cubic/"+file1);
					System.out.println("--"+file1+"--");
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					CubicSol cs1 = new CubicSol("incumbents/cubic/"+file1+"inc.txt");
					double incumbent1 = cs1.getObj();

					String result1;
					result1 = runConst(c1);
					

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent1+","+result1+"\n");
					}
				}
				for (int k = 0; k < 10; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					TestLogger.setFile("cubic/"+file2);
					System.out.println("--"+file2+"--");
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					CubicSol cs2 = new CubicSol("incumbents/cubic/"+file2+"inc.txt");
					double incumbent2 = cs2.getObj();

					String result2;
					result2 = runConst(c2);
					

					if (k == 0) {
						pw.write(n+","+density+","+k+",true,"+incumbent2+","+result2+"\n");
					} else {
						pw.write(",,"+k+",true,"+incumbent2+","+result2+"\n");
					}
				}
			}
		}
		pw.close();
	}
	
	/*
	 * Runs the cubic problem with all constructive heuristics
	 * - Returns a comma-delimited string of results
	 */
	private static String runConst(Cubic c) {
		System.err.println("--Starting DP");
		CubicDP cdp = new CubicDP(c);
		cdp.run();
		double cdpBest = cdp.getResult().getObj();
		long cdpTime = cdp.getTime();

		System.err.println("--Starting Greedy");
		CubicGreedy cg = new CubicGreedy(c);
		cg.run();
		double greedy = cg.getResult().getObj();
		long greedyTime = cg.getTime();

		System.err.println("--Starting Fill");
		CubicFillUp cfu = new CubicFillUp(c);
		cfu.run();
		double fill = cfu.getResult().getObj();
		long fillTime = cfu.getTime();

		System.err.println("--Starting Hybrid");
		CubicGreedyFill cgf = new CubicGreedyFill(c);
		cgf.run();
		double hybrid = cgf.getResult().getObj();
		long hybridTime = cgf.getTime();

		String ret = cdpBest + "," + greedy + "," + fill + "," + hybrid + ",,," + cdpTime + "," + greedyTime + "," + fillTime + "," + hybridTime;
		return ret;
	}
	
	/*
	 * Run and time the incumbent heuristic on the test bed
	 */
	public void timeIncumbent() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("tbIncumbent.csv");
		pw.write("n,density,#,negCoef,incumbent,time\n");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {

					String file1 = n+"_"+density+"_false_"+k;
					System.out.println("--"+file1+"--");

					@SuppressWarnings("unused")
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					long start = System.nanoTime();
					KnapsackSol ks1 = (KnapsackSol)ProblemFactory.genInitSol();
					long end = System.nanoTime();
					double incumbent1 = ks1.getObj();
					double duration1 = (double)(end-start)/60000000000L;

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent1+","+duration1+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent1+","+duration1+"\n");
					}
				}
				for (int k = 0; k < 10; k++) {

					String file2 = n+"_"+density+"_true_"+k;
					System.out.println("--"+file2+"--");

					@SuppressWarnings("unused")
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					long start = System.nanoTime();
					KnapsackSol ks2 = (KnapsackSol)ProblemFactory.genInitSol();
					long end = System.nanoTime();
					double incumbent2 = ks2.getObj();
					double duration2 = (double)(end-start)/60000000000L;

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent2+","+duration2+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent2+","+duration2+"\n");
					}
				}
			}
		}
		pw.close();
	}
}
