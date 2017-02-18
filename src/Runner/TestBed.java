package Runner;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import Constructive.*;
import ExactMethods.Cubic_Forrester;
import ExactMethods.MaxProb_Bill;
import Problems.*;
import Solutions.*;


public class TestBed {

	private static HashMap<String,Double> testObj;
	private static ArrayList<Integer> test;

	public static void main(String[] args) {
		testObj = new HashMap<String,Double>();
		test = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			test.add(i);
		}
//		TestLogger.setUseLog(false);
//		mainCubic();
		TestLogger.setUseLog(true);
		mainMaxProb();
		//		mainFractional();
	}

	private static void mainCubic() {
		double[] densities = {0.25, 0.5, 0.75, 1};
		//		double[] densities = {0.5};
		int[] probSizes = {10, 20, 30, 50, 100, 200};
		//		int[] probSizes = {1000};


//		genTestBed(densities, probSizes);
//		readTestBed(densities, probSizes);
		try {
			//			timeIncumbent(densities,probSizes);
			//			runMIP(densities, probSizes);
			//			System.out.println("Run Construction Heuristics");
			//			runTestBed(densities,probSizes,true);
			System.out.println("Run Metaheuristics");
			runTestBed(densities,probSizes,false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void mainMaxProb() {
		//		genMaxProb();
		try {
			//			runMaxProbMIP();
			runMaxProb();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void mainFractional() {
		genFractional();
	}

	private static void genFractional() {
		int[] probSizes = {100, 300, 1000};
		int seed = 4000;
		for (int n: probSizes) {
			for (int i = 0; i < 5; i++) {
				String file1 = n+"_false_"+i;
				System.out.println(file1);
				Fractional f1 = new Fractional(n, false, seed++);
				FractionalSol fs1 = (FractionalSol)ProblemFactory.genInitSol();
				if (!fs1.getValid()) {
					System.err.println("Invalid answer:" + file1);
				}
				f1.toFile("problems/fractional/"+file1);
				fs1.writeSolution("incumbents/fractional/"+file1+"inc.txt");

				testObj.put(file1, f1.getObj(test));

				Fractional f1t = new Fractional("problems/fractional/"+file1);
				if(f1t.getObj(test) != testObj.get(file1)) {
					System.err.println(file1 + " incorrect");
				}		

				//				String file2 = n+"_true_"+i;
				//				System.out.println(file2);
				//				Fractional f2 = new Fractional(n, true, seed++);
				//				FractionalSol fs2 = (FractionalSol)ProblemFactory.genInitSol();
				//				
				//				if (!fs2.getValid()) {
				//					System.err.println("Invalid answer:" + file2);
				//				}
				//				f2.toFile("problems/fractional/"+file2);
				//				fs2.writeSolution("incumbents/fractional/"+file2+"inc.txt");
				//
				//				testObj.put(file2, f2.getObj(test));
				//
				//				Fractional f2t = new Fractional("problems/fractional/"+file2);
				//				if(f2t.getObj(test) != testObj.get(file2)) {
				//					System.err.println(file2 + " incorrect");
				//				}	
			}
		}
	}

	private static void genMaxProb() {
		int[] possibleK = {65, 75, 85, 95};
		int[] possibleP = {5, 10, 30, 50, 75};
		for (int i = 0; i < 5; i++) {
			for (int p: possibleP) {
				for (int k: possibleK) {
					MaxProbability mp = new MaxProbability(100, false, i, k, p);
					MaxProbabilitySol mps = (MaxProbabilitySol)ProblemFactory.genInitSol();
					String file = "P"+p+"_K"+k+"_"+i;
					System.out.println(file);
					if (!mps.getValid()) {
						System.err.println("Invalid answer:" + file);
					}
					mp.toFile("problems/mp/"+file);
					mps.writeSolution("incumbents/mp/"+file+"inc.txt");

					testObj.put(file, mp.getObj(test));

					MaxProbability mp2 = new MaxProbability("problems/mp/"+file);
					if(mp2.getObj(test) != testObj.get(file)) {
						System.err.println(file + " incorrect");
					}
				}
			}
		}
	}

	private static void runMaxProb() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("maxProbResults.csv");
		pw.write("#,P,K,incumbent,GA,SA,ST,TS\n");
		int[] possibleK = {65, 75, 85, 95};
		int[] possibleP = {5, 10, 30, 50, 75};
		for (int i = 0; i < 5; i++) {
			for (int p: possibleP) {
				for (int k: possibleK) {
					String file = "P"+p+"_K"+k+"_"+i;
					MaxProbability mp2 = new MaxProbability("problems/mp/"+file);
					MaxProbabilitySol mps = new MaxProbabilitySol("incumbents/mp/"+file+"inc.txt");
					TestLogger.setFile(file);
					System.out.println("--"+file+"--");
					double incumbent1 = mps.getObj();

					HeuristicRunner hr = new HeuristicRunner(mps);
					String result1 = hr.getResults();

					if (k == 65) {
						pw.write(i+","+p+","+k+","+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+k+","+incumbent1+","+result1+"\n");
					}
				}
			}
		}
		pw.close();
	}

	private static void runMaxProbMIP() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("maxProbMIP.csv");
		pw.write("#,P,K,incumbent,MIP\n");
		int[] possibleK = {65, 75, 85, 95};
		int[] possibleP = {5, 10, 30, 50, 75};
		for (int i = 0; i < 5; i++) {
			for (int p: possibleP) {
				for (int k: possibleK) {
					String file = "P"+p+"_K"+k+"_"+i;
					System.out.println("--"+file+"--");
					MaxProbability mp = new MaxProbability("problems/mp/"+file);
					MaxProbabilitySol mps = new MaxProbabilitySol("incumbents/mp/"+file+"inc.txt");
					double incumbent1 = mps.getObj();

					String[] args = {file};
					MaxProb_Bill.main(args);

					double result1 = MaxProb_Bill.getBestObj();

					if (k == 65) {
						pw.write(i+","+p+","+k+","+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+k+","+incumbent1+","+result1+"\n");
					}
				}
			}
		}
		pw.close();
	}



	private static void genTestBed(double[] densities, int[] probSizes) {
		int seed = 200;
		for (int k = 0; k < 10; k++) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					//					int seed = n+i+j+k;
					seed++;

					Cubic c1 = new Cubic(n,false,seed,density);
					KnapsackSol ks1 = (KnapsackSol)ProblemFactory.genInitSol();
					String file1 = n+"_"+density+"_false_"+k;
					c1.toFile("problems/cubic/"+file1);
					ks1.writeSolution("incumbents/cubic/"+file1+"inc.txt");

					testObj.put(file1, c1.getObj(test));
					//					System.out.println("Test Solution [0,1,2,3]: " + c1.getObj(test));


					seed++;
					Cubic c2 = new Cubic(n,true,seed,density);
					KnapsackSol ks2 = (KnapsackSol)ProblemFactory.genInitSol();
					String file2 = n+"_"+density+"_true_"+k;
					c2.toFile("problems/cubic/"+file2);
					ks2.writeSolution("incumbents/cubic/"+file2+"inc.txt");

					testObj.put(file2, c2.getObj(test));
					//					System.out.println("Test Solution [0,1,2,3]: " + c2.getObj(test));

				}
			}
		}
	}

	private static void readTestBed(double[] densities, int[] probSizes) {
		ArrayList<Integer> test = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			test.add(i);
		}
		for (int k = 0; k < 10; k++) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];

					String file1 = n+"_"+density+"_false_"+k;
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					KnapsackSol ks1 = new CubicSol("incumbents/cubic/"+file1+"inc.txt");
					if(c1.getObj(test) != testObj.get(file1)) {
						System.err.println(file1 + " incorrect");
					}
					//					System.out.println(file1 + ": " + ks1.getObj());
					//					System.out.println("Test Solution [0,1,2,3]: " + c1.getObj(test));

					String file2 = n+"_"+density+"_true_"+k;
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					KnapsackSol ks2 = new CubicSol("incumbents/cubic/"+file2+"inc.txt");
					if(c2.getObj(test) != testObj.get(file2)) {
						System.err.println(file2 + " incorrect");
					}
					//					System.out.println(file2 + ": " + ks2.getObj());
					//					System.out.println("Test Solution [0,1,2,3]: " + c2.getObj(test));
				}
			}
		}
	}

	private static void timeIncumbent(double[] densities, int[] probSizes) throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("tbIncumbent.csv");
		pw.write("n,density,#,negCoef,incumbent,time\n");
		ArrayList<Integer> test = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			test.add(i);
		}
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {

					String file1 = n+"_"+density+"_false_"+k;
					System.out.println("--"+file1+"--");

					Cubic c1 = new Cubic("problems/cubic/"+file1);
					long start = System.nanoTime();
					KnapsackSol ks1 = (KnapsackSol)ProblemFactory.genInitSol();
					long end = System.nanoTime();
					double incumbent1 = ks1.getObj();
					long duration1 = end-start;

					//					c1.toFile("problems/cubic/"+file1);
					//					ks1.writeSolution("incumbents/cubic/"+file1+"inc.txt");


					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent1+","+duration1+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent1+","+duration1+"\n");
					}
				}
				for (int k = 0; k < 10; k++) {

					String file2 = n+"_"+density+"_true_"+k;
					System.out.println("--"+file2+"--");

					Cubic c2 = new Cubic("problems/cubic/"+file2);
					long start = System.nanoTime();
					KnapsackSol ks2 = (KnapsackSol)ProblemFactory.genInitSol();
					long end = System.nanoTime();
					double incumbent2 = ks2.getObj();
					long duration2 = end-start;

					//					c2.toFile("problems/cubic/"+file2);
					//					ks2.writeSolution("incumbents/cubic/"+file2+"inc.txt");

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

	private static void runMIP(double[] densities, int[] probSizes) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter("tbResultsMIP" + probSizes.length + ".csv");
		pw.write("n,density,#,negCoef,incumbent,MIP\n");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					System.err.println(file1);
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

	private static void runTestBed(double[] densities, int[] probSizes, boolean runConst) throws FileNotFoundException {
		PrintWriter pw;
		if (runConst) {
			pw = new PrintWriter("tbResultsConst.csv");
			pw.write("n,density,#,negCoef,incumbent,DP,Greedy,Fill,Hybrid,,Times:,DP,Greedy,Fill,Hybrid\n");
		} else {
			pw = new PrintWriter("tbResults.csv");
			pw.write("n,density,#,negCoef,incumbent,GA,SA,ST,TS\n");
		}
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 10; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					TestLogger.setFile(file1);
					System.out.println("--"+file1+"--");
					Cubic c1 = new Cubic("problems/cubic/"+file1);
					CubicSol cs1 = new CubicSol("incumbents/cubic/"+file1+"inc.txt");
					cs1.setHealing(true);
					double incumbent1 = cs1.getObj();

					String result1;
					if (runConst) {
						result1 = runConst(c1);
					} else {
						HeuristicRunner hr1 = new HeuristicRunner(cs1);
						result1 = hr1.getResults();
					}

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent1+","+result1+"\n");
					}
				}
				for (int k = 0; k < 10; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					TestLogger.setFile(file2);
					System.out.println("--"+file2+"--");
					Cubic c2 = new Cubic("problems/cubic/"+file2);
					CubicSol cs2 = new CubicSol("incumbents/cubic/"+file2+"inc.txt");
					cs2.setHealing(true);
					double incumbent2 = cs2.getObj();

					String result2;
					if (runConst) {
						result2 = runConst(c2);
					} else {
						HeuristicRunner hr2 = new HeuristicRunner(cs2);
						result2 = hr2.getResults();
					}

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
	//
	//	private static String run(Cubic c) {
	//		System.err.println("--Starting GA");
	//		CubicGA cga = new CubicGA(c);
	//		long cgaBest = cga.getBestObj();
	//
	//		System.err.println("--Starting SA");
	//		CubicSA csa = new CubicSA(c);
	//		long csaBest = csa.getBestObj();
	//
	//		System.err.println("--Starting TS");
	//		CubicTS cts = new CubicTS(c);
	//		long ctsBest = cts.getBestObj();
	//
	//		String ret = cgaBest + "," + csaBest + "," + ctsBest;
	//		return ret;
	//	}
	//	
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
}
