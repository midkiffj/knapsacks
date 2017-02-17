package Runner;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TestLogger {

	public static Logger logger = Logger.getLogger("test"); 
	private static String filename = "test";
	private static boolean useLog = true;

	public static void setUseLog(boolean useLg) {
		useLog = useLg;
	}
	
	public static void setFile(String file) {
		filename = file;
	}

	public static void setLogger(String test) {
		logger = Logger.getLogger(filename+test);
		if (!useLog) {
			TestLogger.logger.setLevel(Level.OFF);
		}
		for (Handler h: logger.getHandlers()) {
			logger.removeHandler(h);
		}
		if (useLog) {
			FileHandler fh;
			try {
				File file = new File("logging/"+filename+"/");
				if (!file.exists()) {
					file.mkdirs();
				}

				File log = new File("logging/"+filename+"/"+test+".log");
				if (log.exists()) {
					log.delete();
				}

				fh = new FileHandler("logging/"+filename+"/"+test+".log");
				logger.setUseParentHandlers(false);
				logger.addHandler(fh);
				sf formatter = new sf();  
				fh.setFormatter(formatter);

				logger.info("Starting: " + filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class sf extends SimpleFormatter {
		public sf() {
			super();
		}

		public String format(LogRecord record) {
			return record.getMessage() + "\n";
		}
	}
}
