package com.iddej.gingerbread2.io;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;

public final class FontHandler {
	private static final Map<String, Font> fontsMap = new HashMap<String, Font>();

	private FontHandler() {
		return;
	}

	public static final void registerFont(final Object object, final String identifier, final String path) {
		try {
			final FileInputStream fileInputStream = new FileInputStream(new File(path));
			final Font font = Font.createFont(Font.TRUETYPE_FONT, fileInputStream);
			FontHandler.fontsMap.put(identifier, font);
		} catch (FontFormatException | IOException exception) {
			GlobalLogger.log(object, LogLevel.ERROR, "Couldn't load font from %s", path, exception.getMessage());
			return;
		}
		GlobalLogger.log(object, LogLevel.INFO, "Successfully loaded font from %s", path);
	}

	public static final Font retrieveFont(final Object object, final String identifier) {
		final Font font = FontHandler.fontsMap.get(identifier);
		if (font == null) {
			GlobalLogger.log(object, LogLevel.ERROR, "Couldn't retrieve font with id %s", identifier);
			return null;
		}
		return font;
	}
}
