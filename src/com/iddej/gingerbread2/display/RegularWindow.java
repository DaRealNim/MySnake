package com.iddej.gingerbread2.display;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.iddej.gingerbread2.core.Game;

public class RegularWindow extends Window {

	public RegularWindow(final Game game, final String title, final int width, final int height, final int buffers) {
		super(game, title, width, height, buffers);
	}

	@Override
	public void create() {
		this.canvas.setMinimumSize(new Dimension(this.width, this.height));
		this.canvas.setMaximumSize(new Dimension(this.width, this.height));
		this.canvas.setPreferredSize(new Dimension(this.width, this.height));
		this.frame.setTitle(this.title);
		this.frame.add(this.canvas);
		this.frame.setResizable(false);
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				RegularWindow.this.closeRequested = true;
			}
		});
	}

	@Override
	public void destroy() {
		this.hide();
	}

	@Override
	public void show() {
		this.frame.setVisible(true);
	}

	@Override
	public void hide() {
		this.frame.setVisible(false);
	}

	@Override
	public void resize() {
		this.canvas.setMinimumSize(new Dimension(this.width, this.height));
		this.canvas.setMaximumSize(new Dimension(this.width, this.height));
		this.canvas.setPreferredSize(new Dimension(this.width, this.height));
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
	}
}
