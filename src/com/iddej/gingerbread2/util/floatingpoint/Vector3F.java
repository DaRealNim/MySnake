package com.iddej.gingerbread2.util.floatingpoint;

public class Vector3F {
	public double x, y, z;

	public Vector3F(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3F add(final Vector3F other) {
		return new Vector3F(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public Vector3F subtract(final Vector3F other) {
		return new Vector3F(this.x - other.x, this.y - other.y, this.z - other.z);
	}

	public Vector3F scale(final double scalar) {
		return new Vector3F(this.x * scalar, this.y * scalar, this.z * scalar);
	}

	public double dotProduct(final Vector3F other) {
		return (this.x * other.x) + (this.y * other.y) + (this.z * other.z);
	}

	public Vector3F crossProduct(final Vector3F other) {
		final double x = (this.y * other.z) - (this.z * other.y);
		final double y = (this.z * other.x) - (this.x * other.z);
		final double z = (this.x * other.y) - (this.y * other.x);
		return new Vector3F(x, y, z);
	}

	public double length() {
		return Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
	}

	public Vector3F normalize() {
		return new Vector3F(this.x / this.length(), this.y / this.length(), this.z / this.length());
	}
}
