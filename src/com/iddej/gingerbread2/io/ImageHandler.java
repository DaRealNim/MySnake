package com.iddej.gingerbread2.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;

public final class ImageHandler {
	private static final Map<String, BufferedImage> imagesMap = new HashMap<String, BufferedImage>();

	private ImageHandler() {
		return;
	}

	public static final void registerImage(final Object object, final String identifier, final String path) {
		BufferedImage image;
		try {
			image = ImageIO.read(new File(path));
		} catch (final IOException ioException) {
			GlobalLogger.log(object, LogLevel.ERROR, "Couldn't load image from %s: %s", path, ioException.getMessage());
			return;
		}
		ImageHandler.imagesMap.put(identifier, image);
		GlobalLogger.log(object, LogLevel.INFO, "Successfully loaded image from %s", path);
	}

	public static final BufferedImage retrieveImage(final Object object, final String identifier) {
		final BufferedImage image = ImageHandler.imagesMap.get(identifier);
		if (image == null) {
			GlobalLogger.log(object, LogLevel.ERROR, "Couldn't retrieve image with id %s", identifier);
			return null;
		}
		return image;
	}
}
