package com.iddej.gingerbread2.display;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.iddej.gingerbread2.core.Game;

public class FullscreenWindow extends Window {

	public FullscreenWindow(final Game game, final String title, final int buffers) {
		super(game, title, Window.DEFAULT_DEVICE_WIDTH, Window.DEFAULT_DEVICE_HEIGHT, buffers);
	}

	@Override
	public void create() {
		this.canvas.setMinimumSize(new Dimension(this.width, this.height));
		this.canvas.setMaximumSize(new Dimension(this.width, this.height));
		this.canvas.setPreferredSize(new Dimension(this.width, this.height));
		this.frame.setTitle(this.title);
		this.frame.add(this.canvas);
		this.frame.setResizable(false);
		this.frame.setLocationRelativeTo(null);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				FullscreenWindow.this.closeRequested = true;
			}
		});
		this.frame.setUndecorated(true);
	}

	@Override
	public void destroy() {
		Window.DEFAULT_DEVICE.setFullScreenWindow(null);
	}

	@Override
	public void show() {
		Window.DEFAULT_DEVICE.setFullScreenWindow(this.frame);
	}

	@Override
	public void hide() {
		Window.DEFAULT_DEVICE.setFullScreenWindow(null);
		this.frame.setVisible(false);
	}

	@Override
	public void resize() {
		return;
	}
}
