package com.iddej.gingerbread2.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.iddej.gingerbread2.Gingerbread2;

public final class GlobalLogger {
	private static final PrintStream defaultSystemOutput = System.out;
	private static final PrintStream defaultSystemErrorOutput = System.err;

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[hh:mm:ss]");

	private static boolean outputRedirected = false;

	private GlobalLogger() {
		return;
	}

	public static final void redirectSystemOutput() {
		if (GlobalLogger.outputRedirected) {
			return;
		} else {
			System.setOut(new GlobalLogger.GlobalLoggerPrintStream(GlobalLogger.defaultSystemOutput, GlobalLogger.defaultSystemErrorOutput));
			System.setErr(new GlobalLogger.GlobalLoggerPrintStream(GlobalLogger.defaultSystemErrorOutput, GlobalLogger.defaultSystemErrorOutput));
			GlobalLogger.outputRedirected = true;
			GlobalLogger.log(new GlobalLogger(), LogLevel.INFO, "Successfully redirected default output streams");
		}
	}

	public static final void log(final Object object, final LogLevel logLevel, final String message, final Object... args) {
		if ((logLevel == LogLevel.SEVERE) || (logLevel == LogLevel.ERROR) || (logLevel == LogLevel.FATAL)) {
			GlobalLogger.defaultSystemErrorOutput.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [" + object.getClass().getName() + " / " + logLevel.name() + "]: " + message + "\n", args);
		} else {
			if (logLevel == LogLevel.DEBUG) {
				if (Gingerbread2.isDebuggingEnabled()) {
					GlobalLogger.defaultSystemOutput.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [" + object.getClass().getName() + " / " + logLevel.name() + "]: " + message + "\n", args);
				}
			} else {
				GlobalLogger.defaultSystemOutput.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [" + object.getClass().getName() + " / " + logLevel.name() + "]: " + message + "\n", args);
			}
		}
	}

	public static enum LogLevel {
		INFO, DEBUG, SEVERE, ERROR, FATAL;
	}

	private static class GlobalLoggerPrintStream extends PrintStream {
		private final PrintStream printStream;

		public GlobalLoggerPrintStream(final OutputStream out, final PrintStream printStream) {
			super(out);
			this.printStream = printStream;
		}

		@Override
		public PrintStream printf(final String format, final Object... args) {
			return this.printStream.printf(GlobalLogger.simpleDateFormat.format(new Date()) + " [DIRECT PRINTF / [SEVERE/?]]: " + format, args);
		}

		@Override
		public PrintStream printf(final Locale l, final String format, final Object... args) {
			return this.printStream.printf(l, GlobalLogger.simpleDateFormat.format(new Date()) + " [DIRECT PRINTF / [SEVERE/?]]: " + format, args);
		}

		@Override
		public void print(final Object obj) {
			this.printStream.print(GlobalLogger.simpleDateFormat.format(new Date()) + " [DIRECT PRINT / [SEVERE/?]]: " + obj);
		}

		@Override
		public void print(final boolean b) {
			this.print((Object) b);
		}

		@Override
		public void print(final char c) {
			this.print((Object) c);
		}

		@Override
		public void print(final char[] s) {
			this.print((Object) s);
		}

		@Override
		public void print(final double d) {
			this.print((Object) d);
		}

		@Override
		public void print(final float f) {
			this.print((Object) f);
		}

		@Override
		public void print(final int i) {
			this.print((Object) i);
		}

		@Override
		public void print(final long l) {
			this.print((Object) l);
		}

		@Override
		public void print(final String s) {
			this.print((Object) s);
		}

		@Override
		public void println(final Object x) {
			this.printStream.println(GlobalLogger.simpleDateFormat.format(new Date()) + " [DIRECT PRINTLN / [SEVERE/?]]: " + x);
		}

		@Override
		public void println() {
			this.printStream.println();
		}

		@Override
		public void println(final boolean x) {
			this.println((Object) x);
		}

		@Override
		public void println(final char x) {
			this.println((Object) x);
		}

		@Override
		public void println(final char[] x) {
			this.println((Object) x);
		}

		@Override
		public void println(final double x) {
			this.println((Object) x);
		}

		@Override
		public void println(final float x) {
			this.println((Object) x);
		}

		@Override
		public void println(final int x) {
			this.println((Object) x);
		}

		@Override
		public void println(final long x) {
			this.println((Object) x);
		}

		@Override
		public void println(final String x) {
			this.println((Object) x);
		}
	}
}
