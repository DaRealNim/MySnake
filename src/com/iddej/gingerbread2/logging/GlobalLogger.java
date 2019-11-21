package com.iddej.gingerbread2.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.iddej.gingerbread2.Gingerbread2;

public final class GlobalLogger {
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[hh:mm:ss]");

	private GlobalLogger() {
		return;
	}

	public static final void log(final Object object, final LogLevel logLevel, final String message, final Object... args) {
		if ((logLevel == LogLevel.SEVERE) || (logLevel == LogLevel.ERROR) || (logLevel == LogLevel.FATAL)) {
			System.err.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [" + object.getClass().getName() + " / " + logLevel.name() + "]: " + message + "\n", args);
		} else {
			if (logLevel == LogLevel.DEBUG) {
				if (Gingerbread2.isDebuggingEnabled()) {
					System.out.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [" + object.getClass().getName() + " / " + logLevel.name() + "]: " + message + "\n", args);
				}
			} else {
				System.out.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [" + object.getClass().getName() + " / " + logLevel.name() + "]: " + message + "\n", args);
			}
		}
	}

	public static enum LogLevel {
		INFO, DEBUG, SEVERE, ERROR, FATAL;
	}
}
