package com.iddej.gingerbread2.core;

public abstract class ThreadContainer {
	public static final int EXIT_SUCCESS = 0, EXIT_FAILURE = -1, EXIT_OTHER = 2;

	protected final String identifier;
	protected final Thread thread;
	protected boolean running;

	public ThreadContainer(final String identifier) {
		this.identifier = identifier;
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				ThreadContainer.this.run();
			}
		}, identifier);
		this.running = false;
	}

	public synchronized final void start() {
		if (this.running) {
			return;
		} else {
			this.running = true;
			this.thread.start();
		}
	}

	protected abstract void create();

	public synchronized final void stop() {
		if (this.running) {
			this.running = false;
			this.thread.interrupt();
		} else {
			return;
		}
	}

	protected abstract void destroy();

	private final void run() {
		this.create();
		while (this.running) {
			this.loop();
		}
		this.destroy();
	}

	protected abstract void loop();

	public String getIdentifier() {
		return this.identifier;
	}
}
