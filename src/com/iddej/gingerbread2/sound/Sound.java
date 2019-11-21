package com.iddej.gingerbread2.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public abstract class Sound {
	protected final Clip clip;

	public Sound(final String path) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		this(new File(path));
	}

	public Sound(final File file) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		this.clip = AudioSystem.getClip();
		final AudioInputStream audioInputStream = this.createReusableAudioInputStream(file);
		this.clip.open(audioInputStream);
	}

	public abstract void start();

	public abstract void stop();

	public abstract void reset();

	public abstract void pause();

	public abstract void resume();

	public abstract void goToPosition(int time);

	protected AudioInputStream createReusableAudioInputStream(final File file) throws IOException, UnsupportedAudioFileException {
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
			final byte[] buffer = new byte[32768];
			int bytesRead = 0;
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(buffer.length);
			while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}
			final AudioInputStream newAudioInputStream = new AudioInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), audioInputStream.getFormat(), -1L);
			return newAudioInputStream;
		} finally {
			if (audioInputStream != null) {
				audioInputStream.close();
			}
		}
	}
}