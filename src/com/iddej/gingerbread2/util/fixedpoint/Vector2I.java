package com.iddej.gingerbread2.util.fixedpoint;

public class Vector2I {
	public int x, y;

	public Vector2I(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public Vector2I add(final Vector2I other) {
		return new Vector2I(this.x + other.x, this.y + other.y);
	}

	public Vector2I subtract(final Vector2I other) {
		return new Vector2I(this.x - other.x, this.y - other.y);
	}

	public Vector2I scale(final int scalar) {
		return new Vector2I(this.x * scalar, this.y * scalar);
	}

	public int dotProduct(final Vector2I other) {
		return (this.x * other.x) + (this.y * other.y);
	}

	public int length() {
		return (int) Math.sqrt((this.x * this.x) + (this.y * this.y));
	}

	public Vector2I normalize() {
		return new Vector2I(this.x / this.length(), this.y / this.length());
	}
}
