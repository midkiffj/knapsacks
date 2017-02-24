package Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import ExactMethods.Fractional_Borrero;
import Problems.Fractional;
import Problems.ProblemFactory;
import Solutions.FractionalSol;

/**
 * Fractional test bed runner
 * 
 * @author midkiffj
 */
public class FractionalTest extends ProblemTest {

	// Test bed specification
	int[] probSizes = {100, 300, 1000};
	int[] mSizes = {1, 3, 5};
	int num = 5;
	
	// Method usage
	boolean generate;
	boolean runHeuristics;
	boolean runMIP;
	
	public FractionalTest(boolean gen, boolean rh, boolean mip, boolean useLog) {
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
		int seed = 4000;
		for (int n: probSizes) {
			for (int m: mSizes) {
				for (int i = 0; i < num; i++) {
					String file1 = n+"_"+m+"_false_"+i;
					System.out.println("--"+file1+"--");
					Fractional f1 = new Fractional(n, m, false, seed++);
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
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runHeuristics()
	 */
	public void runHeuristics() throws FileNotFoundException {
		PrintWriter pw;
		pw = new PrintWriter("fractionalResults.csv");
		pw.write("n,m,#,incumbent,GA,SA,ST,TS\n");
		for (int n: probSizes) {
			for (int m: mSizes) {
				for (int i = 0; i < num; i++) {
					String file1 = n+"_"+m+"_false_"+i;
					@SuppressWarnings("unused")
					Fractional f1 = new Fractional("problems/fractional/"+file1);
					FractionalSol fs1 = new FractionalSol("incumbents/fractional/"+file1+"inc.txt");
					double incumbent1 = fs1.getObj();

					TestLogger.setFile("fractional/"+file1);
					System.out.println("--"+file1+"--");

					HeuristicRunner hr = new HeuristicRunner(fs1);
					String result1 = hr.getResults();


					if (i == 0) {
						pw.write(n+","+m+","+i+","+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+i+","+incumbent1+","+result1+"\n");
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
		PrintWriter pw;
		pw = new PrintWriter("fractionalMIP.csv");
		pw.write("n,m,#,incumbent,MIP\n");
		for (int n: probSizes) {
			for (int m: mSizes) {
				for (int i = 0; i < num; i++) {
					String file1 = n+"_"+m+"_false_"+i;
					System.out.println("--"+file1+"--");
					@SuppressWarnings("unused")
					Fractional f = new Fractional("problems/fractional/"+file1);
					FractionalSol fs = new FractionalSol("incumbents/fractional/"+file1+"inc.txt");
					double incumbent1 = fs.getObj();

					String[] args = {file1};
					Fractional_Borrero.main(args);

					double result1 = Fractional_Borrero.getBestObj();

					if (i == 0) {
						pw.write(n+","+m+","+i+","+incumbent1+","+result1+"\n");
					} else {
						pw.write(",,"+i+","+incumbent1+","+result1+"\n");
					}
				}
			}
		}
		pw.close();
	}

}
