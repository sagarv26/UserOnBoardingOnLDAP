package svh;

import java.util.Properties;

import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;

public class LoggerUtil {

	static Logger logger = null;

	static {

		try {

			Properties props = new Properties();

			// Properties file location

			String strFileName = "/config/log4j.properties";

			// Loading the properties file

			props.load(LoggerUtil.class.getResourceAsStream(strFileName));

			// Configuring the Property configurator

			PropertyConfigurator.configure(props);

		} catch (Exception e) {

			// System.out.println(e);

		}

	}

	/*
	 * 
	 * Method which will be called when trace logs are being logged
	 */

	/*public static void traceLog(String message) {

		getLogger("default");

		logger.trace(message);

	}*/

	/*
	 * 
	 * Method which will be called when debug logs are being logged
	 */

	public static void debugLog(String message) {

		getLogger("default");

		logger.debug(message);

	}

	/*
	 * 
	 * Method which will be called when warning logs are being logged
	 */

	public static void warnLog(String message) {

		getLogger("default");

		logger.warn(message);

	}

	/*
	 * 
	 * Method which will be called when fatal logs are being logged
	 */

	public static void fatalLog(String message) {

		getLogger("default");

		logger.fatal(message);

	}

	/*
	 * 
	 * Method which will be called when error logs are being logged
	 */

	public static void errorLog(String message) {

		getLogger("default");

		logger.error(message);

	}

	/*
	 * 
	 * Method which will be called when info logs are being logged
	 */

	public static void infoLog(String message) {

		getLogger("default");

		logger.info(message);

	}

	// Method for getting the logger object

	private static Logger getLogger(String name) {

		logger = Logger.getLogger(name);

		return logger;

	}

}
