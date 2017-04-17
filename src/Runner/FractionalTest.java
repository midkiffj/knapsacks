package Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Constructive.FractionalDP;
import Constructive.FractionalFillUp;
import Constructive.FractionalGreedy;
import Constructive.FractionalGreedyFill;
import Constructive.FractionalGreedyMax;
import Constructive.FractionalIncumbent;
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
	boolean[] numSize = {false,true};
	boolean[] denSize = {false,true};
	int num = 5;

	// Method usage
	boolean generate;
	boolean runHeuristics;
	boolean runMIP;

	// Folders
	private static final String incuFolder = "incumbents/fractional/";
	private static final String probFolder = "problems/fractional/";
	private static final String resFolder = "results/fractional/";

	/**
	 * Setup options for testing
	 * 
	 * @param gen - (T) generate problems
	 * @param rh - (T) run heuristics
	 * @param mip - (T) run MIP exact method
	 * @param useLog - (T) log testing
	 */
	public FractionalTest(boolean gen, boolean rh, boolean mip, boolean useLog) {
		super(useLog);
		generate = gen;
		runHeuristics = rh;
		runMIP = mip;
	}

	private String numDenFolder(boolean ln, boolean ld) {
		if (ln && ld) {
			return "LN-LD/";
		} else if (ln) {
			return "LN-SD/";
		} else if (ld) {
			return "SN-LD/";
		} else {
			return "SN-SD/";
		}
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
		int seed = 4000;
		for (int n: probSizes) {
			for (int m: mSizes) {
				for (boolean ln: numSize) {
					for (boolean ld: denSize) {
						String subFolder = numDenFolder(ln,ld);
						for (int i = 0; i < num; i++) {
							String file1 = subFolder+n+"_"+m+"_false_"+i;
							System.out.println("--"+file1+"--");
							Fractional f1 = new Fractional(n, m, false, seed++, ln, ld);
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
		}
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runHeuristics()
	 */
	public void runHeuristics() throws FileNotFoundException {
		PrintWriter pw;
		for (boolean ln: numSize) {
			for (boolean ld: denSize) {
				String subFolder = numDenFolder(ln,ld);
				pw = new PrintWriter(resFolder+subFolder+"fractionalHeuristics.csv");
				pw = new PrintWriter(pw,true);
				pw.println("n,m,#,incumbent,GA,SA,ST,TS");
				for (int n: probSizes) {
					for (int m: mSizes) {
						for (int i = 0; i < num; i++) {
							String file1 = subFolder+n+"_"+m+"_false_"+i;
							@SuppressWarnings("unused")
							Fractional f1 = new Fractional(probFolder+file1);
							FractionalSol fs1 = new FractionalSol(incuFolder+file1+"inc.txt");
							double incumbent1 = fs1.getObj();

							TestLogger.setFile("fractional/"+file1);
							System.out.println("--"+file1+"--");

							HeuristicRunner hr = new HeuristicRunner(fs1);
							String result1 = hr.getResults();


							if (i == 0) {
								pw.println(n+","+m+","+i+","+incumbent1+","+result1);
							} else {
								pw.println(",,"+i+","+incumbent1+","+result1);
							}
						}
					}
				}
				pw.close();
			}
		}
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see Runner.ProblemTest#runMIP()
	 */
	public void runMIP() throws FileNotFoundException {
		PrintWriter pw;
		for (boolean ln: numSize) {
			for (boolean ld: denSize) {
				String subFolder = numDenFolder(ln,ld);
				pw = new PrintWriter(resFolder+subFolder+"fractionalMIP.csv");
				pw = new PrintWriter(pw,true);
				pw.println("n,m,#,incumbent,mip,gap,bestBound,timeout");
				for (int n: probSizes) {
					for (int m: mSizes) {
						for (int i = 0; i < num; i++) {
							String file1 = subFolder+n+"_"+m+"_false_"+i;
							System.out.println("--"+file1+"--");
							@SuppressWarnings("unused")
							Fractional f1 = new Fractional(probFolder+file1);
							FractionalSol fs1 = new FractionalSol(incuFolder+file1+"inc.txt");
							double incumbent1 = fs1.getObj();

							String[] args = {file1};
							Fractional_Borrero.main(args);

							double result1 = Fractional_Borrero.getBestObj();
							double gap1 = Fractional_Borrero.getGap();
							String timeout1 = "";
							if (Fractional_Borrero.getTimeout()) {
								timeout1 = "*";
							}
							double bestBound1 = (gap1*result1)+result1;

							if (i == 0) {
								pw.println(n+","+m+","+i+","+incumbent1+","+result1+","+gap1+","+bestBound1+","+timeout1);
							} else {
								pw.println(",,"+i+","+incumbent1+","+result1+","+gap1+","+bestBound1+","+timeout1);
							}
						}
					}
				}
				pw.close();
			}
		}
	}

	public void runConstructive() throws FileNotFoundException {
		PrintWriter pw;
		for (boolean ln: numSize) {
			for (boolean ld: denSize) {
				String subFolder = numDenFolder(ln,ld);
				pw = new PrintWriter(resFolder+subFolder+"fractionalConstr.csv");
				pw = new PrintWriter(pw,true);
				pw.println("n,m,#,incumbent,Greedy,GreedyMax,GreedyFill,FillUp,DP,,Time(min):,incumbent,Greedy,GreedyMax,GreedyFill,FillUp,DP");
				for (int n: probSizes) {
					for (int m: mSizes) {
						for (int i = 0; i < num; i++) {
							String file1 = subFolder+n+"_"+m+"_false_"+i;
							Fractional f1 = new Fractional(probFolder+file1);
							System.out.println("--"+file1+"--");
							
							System.out.println("--Incumbent--");
							FractionalIncumbent fi = new FractionalIncumbent(f1);
							fi.run();
							String incumbent1 = "" + fi.getResult().getObj();
							String incTime = "" + fi.getTime();

							System.out.println("--Greedy--");
							FractionalGreedy fg = new FractionalGreedy(f1);
							fg.run();
							String greedyObj = "" + fg.getResult().getObj();
							String greedyTime = "" + fg.getTime();

							System.out.println("--GreedyMax--");
							FractionalGreedyMax fgm = new FractionalGreedyMax(f1);
							fgm.run();
							String greedyMaxObj = "" + fgm.getResult().getObj();
							String greedyMaxTime = "" + fgm.getTime();

							System.out.println("--FillUp--");
							FractionalFillUp ff = new FractionalFillUp(f1);
							ff.run();
							String fillObj = "" + ff.getResult().getObj();
							String fillTime = "" + ff.getTime();
							
							System.out.println("--GreedyFill--");
							FractionalGreedyFill fgf = new FractionalGreedyFill(f1);
							fgf.run();
							String greedyFillObj = "" + fgf.getResult().getObj();
							String greedyFillTime = "" + fgf.getTime();

							System.out.println("--DP--");
							FractionalDP fdp = new FractionalDP(f1);
							fdp.run();
							String dpObj = "" + fdp.getResult().getObj();
							String dpTime = "" + fdp.getTime();


							if (i == 0) {
								pw.println(n+","+m+","+i+","+incumbent1+","+greedyObj+","+greedyMaxObj+","+greedyFillObj+","+fillObj+","+dpObj+",,,"+incTime+","+greedyTime+","+greedyMaxTime+","+greedyFillTime+","+fillTime+","+dpTime);
							} else {
								pw.println(",,"+i+","+incumbent1+","+greedyObj+","+greedyMaxObj+","+greedyFillObj+","+fillObj+","+dpObj+",,,"+incTime+","+greedyTime+","+greedyMaxTime+","+greedyFillTime+","+fillTime+","+dpTime);
							}
						}
					}
				}
				pw.close();
			}
		}
	}

}
