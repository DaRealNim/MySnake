package com.iddej.gingerbread2.exceptions;

import com.iddej.gingerbread2.core.Game;

public class MissingWindowException extends Exception {
	private static final long serialVersionUID = 855644175919236205L;

	public MissingWindowException(final Game game) {
		super(String.format("Game id %s tried to start with no window created!", game.getIdentifier()));
	}
}
