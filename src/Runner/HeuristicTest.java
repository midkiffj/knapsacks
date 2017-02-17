package Runner;

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import Heuristics.*;
import Problems.*;
import Runner.TestLogger.sf;
import Solutions.*;

public class HeuristicTest {
	
	public static void main(String[] args) {
		for(Handler h: TestLogger.logger.getHandlers()) {
			TestLogger.logger.removeHandler(h);
		}
		ConsoleHandler ch = new ConsoleHandler();
		TestLogger.logger.setUseParentHandlers(false);
		TestLogger.logger.addHandler(ch);
		sf formatter = new sf();  
        ch.setFormatter(formatter);
        
//        TestLogger.logger.setLevel(Level.OFF);
		MaxProbability mp = new MaxProbability(100,false,123);
	
		ArrayList<Integer> x = new ArrayList<Integer>();
		x.add(0);
		x.add(1);
		System.out.println(mp.getObj(x));
		
//		boolean[] xVals = new boolean[10];
//		xVals[1] = true;
//		xVals[2] = true;
//		xVals[3] = true;
//		xVals[5] = true;
//		ArrayList<Integer> x = new ArrayList<Integer>();
//		x.add(1);
//		x.add(2);
//		x.add(3);
//		x.add(5);
//		CubicSol cs1 = new CubicSol(xVals);
//		System.out.println(cs1.getValid());
		
//		System.out.println(c.getObj(x));
//		MaxProbabilitySol mps = new MaxProbabilitySol("incumbents/30_0.5_false_1inc.txt");
		MaxProbabilitySol mps = new MaxProbabilitySol();
		mps.setHealing(false);
		System.out.println(mps.getValid());
		HeuristicRunner hr = new HeuristicRunner(mps);
//		Metaheuristic h = new tabuSearch(mps,-1,60000000000L*1);
//		h.run();
//		ProblemSol ps = h.getBest();
//		if (ps.getValid()) {
//			System.out.println("Valid");
//		}
//		System.out.println(ps.getObj());
	}
}
