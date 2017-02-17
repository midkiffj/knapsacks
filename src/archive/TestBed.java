package archive;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import ExactMethods.Cubic_Forrester;


public class TestBed {

	public static void main(String[] args) {
		double[] densities = {0.5};
		int[] probSizes = {30, 50, 100, 200};
//		int[] probSizes = {30, 50, 100, 200};
//		int[] probSizes = {10, 20};


//		genTestBed(densities, probSizes);
//		readTestBed(densities, probSizes);
		try {
//			System.out.println("Run Construction Heuristics");
//			runTestBed(densities,probSizes,true);
			System.out.println("Run Metaheuristics");
			runTestBed(densities,probSizes,false);
//			runMIP(densities, probSizes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void genTestBed(double[] densities, int[] probSizes) {
		ArrayList<Integer> test = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			test.add(i);
		}
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					int seed = n+i+j+k;

					Cubic c1 = new Cubic(n,false,seed,density);
					c1.genInit();
					String file1 = n+"_"+density+"_false_"+k;
					c1.toFile(file1);
//					System.out.println("Test Solution [0,1,2,3]: " + c1.updateObj(test));



					Cubic c2 = new Cubic(n,true,seed,density);
					c2.genInit();
					String file2 = n+"_"+density+"_true_"+k;
					c2.toFile(file2);
//					System.out.println("Test Solution [0,1,2,3]: " + c2.updateObj(test));
				}
			}
		}
	}

	private static void readTestBed(double[] densities, int[] probSizes) {
		ArrayList<Integer> test = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			test.add(i);
		}
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];

					String file1 = n+"_"+density+"_false_"+k;
					Cubic c1 = new Cubic(file1);
					System.out.println("Test Solution [0,1,2,3]: " + c1.updateObj(test));

					String file2 = n+"_"+density+"_true_"+k;
					Cubic c2 = new Cubic(file2);
					System.out.println("Test Solution [0,1,2,3]: " + c2.updateObj(test));
				}
			}
		}
	}
	
	private static void runMIP(double[] densities, int[] probSizes) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter("tbResultsMIP" + probSizes.length + ".csv");
		pw.write("n,density,#,negCoef,incumbent,MIP\n");
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 3; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					System.err.println(file1);
					Cubic c1 = new Cubic(file1);
					String[] args1 = {file1};
					Cubic_Forrester.main(args1);
					long result1 = Cubic_Forrester.getBestObj();

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+c1.getIncObj()+","+result1+"\n");
					} else {
						pw.write(",,"+k+",false,"+c1.getIncObj()+","+result1+"\n");
					}
				}
				for (int k = 0; k < 3; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					System.err.println(file2);
					Cubic c2 = new Cubic(file2);
					String[] args2 = {file2};
					Cubic_Forrester.main(args2);
					long result2 = Cubic_Forrester.getBestObj();

					if (k == 0) {
						pw.write(n+","+density+","+k+",true,"+c2.getIncObj()+","+result2+"\n");
					} else {
						pw.write(",,"+k+",true,"+c2.getIncObj()+","+result2+"\n");
					}
				}
			}
		}
		pw.close();
	}

	private static void runTestBed(double[] densities, int[] probSizes, boolean runConst) throws FileNotFoundException {
		PrintWriter pw;
		if (runConst) {
			pw = new PrintWriter("tbResultsConstArchive.csv");
			pw.write("n,density,#,negCoef,incumbent,DP,Greedy,Fill,Hybrid,Hybrid2,,Times:,Greedy,Fill,Hybrid,Hybrid2\n");
		} else {
			pw = new PrintWriter("tbResultsArchive.csv");
			pw.write("n,density,#,negCoef,incumbent,GA,SA,TS\n");
		}
		for (int i = 0; i < densities.length; i++) {
			double density = densities[i];
			for (int j = 0; j < probSizes.length; j++) {
				int n = probSizes[j];
				for (int k = 0; k < 3; k++) {
					String file1 = n+"_"+density+"_false_"+k;
					System.err.println(file1);
					Cubic c1 = new Cubic("problems/"+file1);
					long incumbent1 = c1.getIncObj();
					
					String result1;
					if (runConst) {
						result1 = runConst(c1);
					} else {
						result1 = run(c1);
					}

					if (k == 0) {
						pw.write(n+","+density+","+k+",false,"+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+k+",false,"+incumbent1+","+result1+"\n");
					}
				}
				for (int k = 0; k < 3; k++) {
					String file2 = n+"_"+density+"_true_"+k;
					System.err.println(file2);
					Cubic c2 = new Cubic("problems/"+file2);
					long incumbent2 = c2.getIncObj();

					String result2;
					if (runConst) {
						result2 = runConst(c2);
					} else {
						result2 = run(c2);
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

	private static String run(Cubic c) {	
		System.err.println("--Starting SA");
		CubicSA csa = new CubicSA(c);
		long csaBest = csa.getBestObj();
		System.err.println(csaBest);
		
		System.err.println("--Starting TS");
		CubicTS cts = new CubicTS(c);
		long ctsBest = cts.getBestObj();
		System.err.println(ctsBest);

		System.err.println("--Starting GA");
		CubicGA cga = new CubicGA(c);
		long cgaBest = cga.getBestObj();
		System.err.println(cgaBest);
		
		String ret = cgaBest + "," + csaBest + "," + ctsBest;
		return ret;
	}
	
	private static String runConst(Cubic c) {
		System.err.println("--Starting DP");
		CubicDP cdp = new CubicDP(c);
		long cdpBest = cdp.getBestObj();

		System.err.println("--Starting Const");
		CubicConst cc = new CubicConst(c);
		long greedy = cc.getGreedyObj();
		long fill = cc.getFillObj();
		long hybrid = cc.getHybridObj();
		long hybrid2 = cc.getHybrid2Obj();
		
		long greedyTime = cc.getGreedyTime();
		long fillTime = cc.getFillTime();
		long hybridTime = cc.getHybridTime();
		long hybrid2Time = cc.getHybrid2Time();

		String ret = cdpBest + "," + greedy + "," + fill + "," + hybrid + "," + hybrid2 + ",,," + greedyTime + "," + fillTime + "," + hybridTime + "," + hybrid2Time;
		return ret;
	}
}
