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

	// Folders
	private static final String incuFolder = "incumbents/fractional/";
	private static final String probFolder = "problems/fractional/";
	private static final String resFolder = "results/fractional/";

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
					f1.toFile(probFolder+file1);
					fs1.writeSolution(incuFolder+file1+"inc.txt");

					testObj.put(file1, f1.getObj(test));

					Fractional f1t = new Fractional("problems/fractional/"+file1);
					if(f1t.getObj(test) != testObj.get(file1)) {
						System.err.println(file1 + " incorrect");
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
		pw = new PrintWriter(resFolder+"fractionalHeuristics.csv");
		pw.write("n,m,#,incumbent,GA,SA,ST,TS\n");
		for (int n: probSizes) {
			for (int m: mSizes) {
				for (int i = 0; i < num; i++) {
					String file1 = n+"_"+m+"_false_"+i;
					@SuppressWarnings("unused")
					Fractional f1 = new Fractional(probFolder+file1);
					FractionalSol fs1 = new FractionalSol(incuFolder+file1+"inc.txt");
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
		pw = new PrintWriter(resFolder+"fractionalMIP.csv");
		pw.write("n,m,#,incumbent,MIP\n");
		for (int n: probSizes) {
			for (int m: mSizes) {
				for (int i = 0; i < num; i++) {
					String file1 = n+"_"+m+"_false_"+i;
					System.out.println("--"+file1+"--");
					@SuppressWarnings("unused")
					Fractional f1 = new Fractional(probFolder+file1);
					FractionalSol fs1 = new FractionalSol(incuFolder+file1+"inc.txt");
					double incumbent1 = fs1.getObj();

					String[] args = {file1};
					Fractional_Borrero.main(args);

					double result1 = Fractional_Borrero.getBestObj();
					String timeout1 = "";
					if (Fractional_Borrero.getTimeout()) {
						timeout1 = "*";
					}

					if (i == 0) {
						pw.write(n+","+m+","+i+","+incumbent1+","+result1+","+timeout1+"\n");
					} else {
						pw.write(",,"+i+","+incumbent1+","+result1+","+timeout1+"\n");
					}
				}
			}
		}
		pw.close();
	}

}
