package Runner;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * JUL Logger for Heuristics
 * @author midkiffj
 *
 */
public class TestLogger {

	public static Logger logger = Logger.getLogger("test"); 
	private static String filename = "test";
	private static boolean useLog = true;

	/**
	 *  Toggle logging (for larger tests)
	 *  
	 * @param useLg
	 */
	public static void setUseLog(boolean useLg) {
		useLog = useLg;
	}
	
	/**
	 * Update the file for logging
	 * 
	 * @param file - problem being tested
	 */
	public static void setFile(String file) {
		filename = file;
	}

	/**
	 * Get a unique logger for each test and update logging file
	 * 
	 * @param test - heuristic being used
	 */
	public static void setLogger(String test) {
		logger = Logger.getLogger(filename+test);
		// Set logger level
		if (!useLog) {
			logger.setLevel(Level.OFF);
		} else {
			logger.setLevel(Level.INFO);
		}
		// Remove pre-existing, default handlers
		for (Handler h: logger.getHandlers()) {
			logger.removeHandler(h);
		}
		if (useLog) {
			FileHandler fh;
			try {
				// Create logging directory if doesn't exist
				File file = new File("logging/"+filename+"/");
				if (!file.exists()) {
					file.mkdirs();
				}

				// Delete pre-existing log file
				File log = new File("logging/"+filename+"/"+test+".log");
				if (log.exists()) {
					log.delete();
				}

				// Add file handler to logger
				fh = new FileHandler("logging/"+filename+"/"+test+".log");
				logger.setUseParentHandlers(false);
				logger.addHandler(fh);
				sf formatter = new sf();  
				fh.setFormatter(formatter);

				// Initial log
				logger.info("Starting: " + filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Dummy formatter used for logging. Appends new-line character to each log.
	 *  
	 * @author midkiffj
	 */
	public static class sf extends SimpleFormatter {
		public sf() {
			super();
		}

		public String format(LogRecord record) {
			return record.getMessage() + "\n";
		}
	}
}
