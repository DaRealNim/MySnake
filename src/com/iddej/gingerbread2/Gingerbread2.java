package com.iddej.gingerbread2;

import java.awt.Graphics;

import com.iddej.gingerbread2.core.Game;
import com.iddej.gingerbread2.core.ThreadContainer;
import com.iddej.gingerbread2.display.Screen;
import com.iddej.gingerbread2.input.Mouse;
import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;

public final class Gingerbread2 extends Game {
	private static boolean debug = true;

	private Gingerbread2() {
		super("gingerbread2", 60.0D);
	}

	public static final boolean isDebuggingEnabled() {
		return Gingerbread2.debug;
	}

	public static final void setDebuggingState(final boolean state) {
		Gingerbread2.debug = state;
	}

	@Override
	protected void create() {
		super.create();
	}

	@Override
	protected void destroy() {
		super.destroy();
		System.exit(ThreadContainer.EXIT_SUCCESS);
	}

	@Override
	public void update(final double delta) {
		// TODO Auto-generated method stub
		if (this.mouse.isButtonDownInFrame(Mouse.BUTTON_LEFT)) {
			GlobalLogger.log(this, LogLevel.INFO, "Left mouse button has been pressed");
		}
		if (this.mouse.isButtonDownInFrame(Mouse.BUTTON_MIDDLE)) {
			System.out.println("MIDDLE");
		}
		if (this.mouse.isButtonDownInFrame(Mouse.BUTTON_RIGHT)) {
			System.out.println("RIGHT");
		}
	}

	@Override
	public void render(final Graphics graphics) {
		Screen.setGraphics(graphics);
		Screen.clear(this.window);
	}

	public static void main(final String[] args) {
		GlobalLogger.redirectSystemOutput();
		final Gingerbread2 gingerbreadR = new Gingerbread2();
		gingerbreadR.createRegularWindow("Gingerbread 2", 800, 600, 2);
		gingerbreadR.start();
	}
}
