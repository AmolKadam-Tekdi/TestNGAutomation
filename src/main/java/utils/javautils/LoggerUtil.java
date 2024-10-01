package utils.javautils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * LoggerUtil class encapsulates several utility methods that helps logging. It
 * consolidates all logs in one single file and creates new file for every run.
 * The log file is generated in 'logs' directory. It provides convenient methods
 * for: - measuring time of execution. - Dumping stack trace in log files
 *
 * It also sets pattern of logs with TimeStamp and Log level
 *
 * @author Mandar Wadhavekar
 *
 */
public class LoggerUtil {

	private static FileHandler logFileHandler = null;
	private static SimpleFormatter simpleFormatter = null;
	private static Logger logger = null;
	//	private static String logFileDir = "./logs/";
	private static Instant startInstant = null; // to measure time
	private static FileReader fr;
	private static Properties prop;
	static String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());


	private static final String LOG_FILE_DIR = "." + File.separator + "logs" + File.separator;

	// Static initializer block to initialize logger and file handler
	static {
		String logFileName = "logfile.log";

		try {
			// Create log directory if it doesn't exist
			File file = new File(LOG_FILE_DIR);
			if (!file.exists()) {
				file.mkdir();
			}

			// Initialize file handler for logging
			logFileHandler = new FileHandler(LOG_FILE_DIR + logFileName, true); // Set append to true to continue logging to the same file
			simpleFormatter = new SimpleFormatter();
			logFileHandler.setFormatter(simpleFormatter);

			// Initialize logger
			logger = Logger.getLogger("Logger");
			logger.setLevel(Level.INFO); // Set logger level to INFO
			logger.addHandler(logFileHandler); // Add file handler to logger
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
		}
	}

	// Method to set log file name dynamically
	public static void setLogFileName(String fileName) {
		closeLogFileHandler(); // Close existing file handler if any

		try {
			// Generate current date and time to append to the file name
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String timestamp = dateFormat.format(new Date());

			// Create new file handler with the specified file name + timestamp
			logFileHandler = new FileHandler(LOG_FILE_DIR + fileName + "_" + timestamp + ".log", true); // Set append to true to continue logging to the same file
			simpleFormatter = new SimpleFormatter();
			logFileHandler.setFormatter(simpleFormatter);

			// Add new file handler to logger
			logger.addHandler(logFileHandler);

			// Optionally, initialize extent report manager with the new file name
			ExtentReportManager.initializeReport(fileName);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
		}
	}

	// Method to close the current log file handler
	private static void closeLogFileHandler() {
		if (logFileHandler != null) {
			logFileHandler.close();
			logFileHandler = null;
		}
	}



	public static FileHandler getLogFileHandler() {
		return logFileHandler;
	}

	/**
	 * This methods returns Logger so that classes can put in log statements
	 *
	 * @return Logger
	 */
	public static Logger getLogger() {

		return logger;
	}

	/**
	 * Log details of exception - exception's message and stack trace
	 *
	 * @param exception
	 */
	public static void logException(Exception exception) {

		logException(exception, "");
	}

	/**
	 * Log details of exception - exception's error message, custom message and
	 * stack trace
	 *
	 * @param exception
	 */
	public static void logException(Exception exception, String customMessage) {

		logger.severe("!!!    EXCEPTION Occurred    !!!");
		logger.severe(customMessage);
		logger.severe("------------------- Exception Error Message is: --------------");
		logger.severe(exception.getMessage());

		/*
		 * This method does not print stack trace logger.log(Level.SEVERE,
		 * customMessage+"trying logger.log with throwable ", exception );
		 */
		logger.severe("=================== Exception stack trace ====================");
		logger.severe(convertStackTraceToString(exception));

	}

	/**
	 * Starts measurement of time using Instant. Returns java.time.Instant class but
	 * caller method can ignore it if they just want to measure time and not
	 * interested in other details of the Instant
	 *
	 * @return Instant
	 */
	public static Instant startTimeMeasurement() {
		startInstant = Instant.now();
		logger.log(Level.INFO, "Started measuring time " + startInstant); // TODO: Change level to FINER - Mandar
		// Wadhavekar
		return startInstant;
	}

	/**
	 * Stop Time Measurement and print time taken since last startTimeMeasurement.
	 */
	public static void stopTimeMeasurement() {
		stopTimeMeasurement(null);
	}

	/**
	 * Stop Time Measurement and print time taken since last startTimeMeasurement
	 * and also print the custom message. This can be utilized in following
	 *
	 * @param message
	 */
	public static void stopTimeMeasurement(String message) {
		Instant finishInstant = Instant.now();
		long timeElapsed = Duration.between(startInstant, finishInstant).toMillis();
		if (message != null) {
			logger.log(Level.INFO, message);
		}
		logger.log(Level.INFO,
				"Time elapsed - [In milli-seconds: " + timeElapsed + "] [In seconds:" + timeElapsed / 1000.0 + "]");

	}

	/**
	 * Converts stack trace to String Mainly to dump it in log file.
	 *
	 * @param throwable the exception for which stack trace is to be converted into
	 *                  string
	 * @return
	 */
	private static String convertStackTraceToString(Throwable throwable) {
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			throwable.printStackTrace(pw);
			return sw.toString();
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}



	// To delete old log files


	private static LoggerUtil instance;
	private String directoryPath;

	private LoggerUtil() {
		// Set the directory path where the report files are stored
		directoryPath = System.getProperty("user.dir") + File.separator+ "logs"+ File.separator;
	}

	public static LoggerUtil getInstance() {
		if (instance == null) {
			synchronized (LoggerUtil.class) {
				if (instance == null) {
					instance = new LoggerUtil();
				}
			}
		}
		return instance;
	}

	public void deleteOldLogFiles(int days) {
		// Calculate the deletion threshold date

		/*here locldate object from java.time used to calculate date usign minus () in that
		 * mention days then it will get substracted from current day
		 * suppose day 5running then it will delete 3day
		 *
		 * */
		LocalDate deletionThreshold = LocalDate.now().minus(days, ChronoUnit.DAYS);

		// Get the list of files in the directory
		File directory = new File(directoryPath);
		File[] files = directory.listFiles();

		// Iterate through the files and delete if older than the threshold
		for (File file : files) {
			LocalDate fileDate = LocalDate.ofEpochDay(file.lastModified() / (24 * 60 * 60 * 1000));

			if (fileDate.isBefore(deletionThreshold)) {
				// Delete the file
				if (file.delete()) {
					System.out.println("Deleted file: " + file.getName());
				} else {
					System.out.println("Failed to delete file: " + file.getName());
				}
			}
		}
	}

}