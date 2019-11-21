package com.iddej.gingerbread2.util;

public final class GlobalMath {
	private GlobalMath() {
		return;
	}

	public static final float mapf(final float value, final float valueMin, final float valueMax, final float targetMin, final float targetMax) {
		return (((value - valueMin) / (valueMax - valueMin)) * (targetMax - targetMin)) + targetMin;
	}

	public static final int mapi(final int value, final int valueMin, final int valueMax, final int targetMin, final int targetMax) {
		return (((value - valueMin) / (valueMax - valueMin)) * (targetMax - targetMin)) + targetMin;
	}
}
