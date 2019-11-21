package com.iddej.gingerbread2.core;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import com.iddej.gingerbread2.display.FullscreenWindow;
import com.iddej.gingerbread2.display.RegularWindow;
import com.iddej.gingerbread2.display.Window;
import com.iddej.gingerbread2.exceptions.MissingWindowException;
import com.iddej.gingerbread2.input.Keyboard;
import com.iddej.gingerbread2.input.Mouse;
import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;

public abstract class Game extends ThreadContainer {
	private final double refreshRate;

	protected Window window;

	protected Keyboard keyboard;
	protected Mouse mouse;

	public Game(final String identifier, final double refreshRate) {
		super(identifier);
		this.refreshRate = refreshRate;
		this.keyboard = new Keyboard();
		this.mouse = new Mouse();
		GlobalLogger.log(this, LogLevel.DEBUG, "Creating game container id: %s, refresh rate: %.0f", identifier, refreshRate);
	}

	public void createRegularWindow(final String title, final int width, final int height, final int buffers) {
		if (this.running) {
			GlobalLogger.log(this, LogLevel.SEVERE, "Can not create window after game start!");
			return;
		}
		GlobalLogger.log(this, LogLevel.DEBUG, "Creating non-fullscreen window, title: \"%s\", width x height: %dx%d, buffers: %d", title, width, height, buffers);
		this.window = new RegularWindow(this, title, width, height, buffers);
	}

	public final void createFullscreenWindow(final String title, final int buffers) {
		if (this.running) {
			GlobalLogger.log(this, LogLevel.SEVERE, "Can not create window after game start!");
			return;
		}
		GlobalLogger.log(this, LogLevel.DEBUG, "Creating fullscreen window, title: \"%s\", buffers: %d", title, buffers);
		this.window = new FullscreenWindow(this, title, buffers);
	}

	@Override
	protected void create() {
		if (this.window == null) {
			try {
				throw new MissingWindowException(this);
			} catch (final MissingWindowException createdException) {
				GlobalLogger.log(this, LogLevel.FATAL, "MissingWindowException raised, createWindow() has not been called for game id %s\n\tException message: %s", this.getIdentifier(), createdException.getMessage());
				System.exit(ThreadContainer.EXIT_FAILURE);
			}
		}
		GlobalLogger.log(this, LogLevel.DEBUG, "Creating window for game id %s...", this.getIdentifier());
		this.window.create();
		GlobalLogger.log(this, LogLevel.DEBUG, "Register input device class %s", this.keyboard.getClass().getName());
		this.window.registerInput(this.keyboard);
		GlobalLogger.log(this, LogLevel.DEBUG, "Register input device class %s", this.mouse.getClass().getName());
		this.window.registerInput(this.mouse);
		this.window.show();
		this.window.requestFocus();
		GlobalLogger.log(this, LogLevel.INFO, "Window for game id %s has been succesfully created and displayed", this.getIdentifier());
	}

	@Override
	protected void destroy() {
		this.window.hide();
		this.window.destroy();
		GlobalLogger.log(this, LogLevel.INFO, "Window for game id %s has been succesfully hidden and disposed of", this.getIdentifier());
	}

	@Override
	protected void loop() {
		long startTime = System.nanoTime();
		final double nanosecondsPerTick = 1000000000D / this.refreshRate;
		int frames = 0;
		int updates = 0;
		long resetTime = System.currentTimeMillis();
		double delta = 0.0D;
		while (this.running) {
			final long endTime = System.nanoTime();
			delta += (endTime - startTime) / nanosecondsPerTick;
			startTime = endTime;
			boolean shouldRender = false;
			while (delta >= 1) {
				updates++;
				if (this.window.isCloseRequested()) {
					this.stop();
				}
				this.update(delta);
				this.keyboard.update();
				this.mouse.update();
				delta -= 1;
				shouldRender = true;
			}
			if (shouldRender) {
				frames++;
				final BufferStrategy bufferStrategy = this.window.getBufferStrategy();
				final Graphics graphics = bufferStrategy.getDrawGraphics();
				this.render(graphics);
				graphics.dispose();
				bufferStrategy.show();
			}
			if ((System.currentTimeMillis() - resetTime) >= 1000) {
				resetTime += 1000;
				GlobalLogger.log(this, LogLevel.DEBUG, "Last second updates: %d - Last second framerate: %d", updates, frames);
				frames = 0;
				updates = 0;
			}
		}
	}

	public abstract void update(double delta);

	public abstract void render(Graphics graphics);

	@Override
	public String getIdentifier() {
		return super.getIdentifier();
	}

	public double getRefreshRate() {
		return this.refreshRate;
	}

	public Keyboard getKeyboard() {
		return this.keyboard;
	}

	public Mouse getMouse() {
		return this.mouse;
	}

	public Window getWindow() {
		return this.window;
	}
}
