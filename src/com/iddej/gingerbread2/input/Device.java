package com.iddej.gingerbread2.input;

public interface Device {
	public InputDeviceType getType();

	static enum InputDeviceType {
		KEYBOARD, MOUSE;
	}
}
