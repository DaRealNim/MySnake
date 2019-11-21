package com.iddej.gingerbread2.display;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;

import com.iddej.gingerbread2.core.Game;
import com.iddej.gingerbread2.input.Device;

public abstract class Window {
	public static final GraphicsDevice DEFAULT_DEVICE = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
	public static final int DEFAULT_DEVICE_WIDTH = Window.DEFAULT_DEVICE.getDisplayMode().getWidth(), DEFAULT_DEVICE_HEIGHT = Window.DEFAULT_DEVICE.getDisplayMode().getHeight();

	protected String title;
	protected int width, height;

	protected final int buffers;
	protected final Game game;

	protected final Frame frame;
	protected final Canvas canvas;

	protected boolean closeRequested;

	public Window(final Game game, final String title, final int width, final int height, final int buffers) {
		this.game = game;
		this.title = title;
		this.width = width;
		this.height = height;
		this.buffers = buffers;
		this.frame = new Frame();
		this.canvas = new Canvas();
		this.closeRequested = false;
	}

	public abstract void create();

	public abstract void destroy();

	public abstract void show();

	public abstract void hide();

	public abstract void resize();

	public final void registerInput(final Device device) {
		switch (device.getType()) {
		case KEYBOARD:
			this.canvas.addKeyListener((KeyListener) device);
			break;
		case MOUSE:
			this.canvas.addMouseListener((MouseListener) device);
			this.canvas.addMouseMotionListener((MouseMotionListener) device);
			this.canvas.addMouseWheelListener((MouseWheelListener) device);
			break;
		default:
			return;
		}
	}

	public final void requestFocus() {
		this.canvas.requestFocus();
	}

	public final BufferStrategy getBufferStrategy() {
		final BufferStrategy bufferStrategy = this.canvas.getBufferStrategy();
		if (bufferStrategy == null) {
			this.canvas.createBufferStrategy(this.buffers);
			return this.getBufferStrategy();
		}
		return bufferStrategy;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
		this.frame.setTitle(title);
	}

	public int getWidth() {
		return this.canvas.getWidth();
	}

	public void setWidth(final int width) {
		this.width = width;
		this.resize();
	}

	public int getHeight() {
		return this.canvas.getHeight();
	}

	public void setHeight(final int height) {
		this.height = height;
		this.resize();
	}

	public int getBuffers() {
		return this.buffers;
	}

	public Game getGame() {
		return this.game;
	}

	public boolean isCloseRequested() {
		return this.closeRequested;
	}
}
