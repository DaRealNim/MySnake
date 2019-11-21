package com.iddej.gingerbread2.util.fixedpoint;

public class Vector3I {
	public int x, y, z;

	public Vector3I(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3I add(final Vector3I other) {
		return new Vector3I(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public Vector3I subtract(final Vector3I other) {
		return new Vector3I(this.x - other.x, this.y - other.y, this.z - other.z);
	}

	public Vector3I scale(final int scalar) {
		return new Vector3I(this.x * scalar, this.y * scalar, this.z * scalar);
	}

	public int dotProduct(final Vector3I other) {
		return (this.x * other.x) + (this.y * other.y) + (this.z * other.z);
	}

	public Vector3I crossProduct(final Vector3I other) {
		final int x = (this.y * other.z) - (this.z * other.y);
		final int y = (this.z * other.x) - (this.x * other.z);
		final int z = (this.x * other.y) - (this.y * other.x);
		return new Vector3I(x, y, z);
	}

	public int length() {
		return (int) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
	}

	public Vector3I normalize() {
		return new Vector3I(this.x / this.length(), this.y / this.length(), this.z / this.length());
	}
}
