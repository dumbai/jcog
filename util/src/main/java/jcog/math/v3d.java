package jcog.math;

import jcog.Util;

/** double float (64-bit) 3D vector */
public final class v3d {

    /** TODO read-only impl */
    public static final v3d X_AXIS = new v3d(1, 0, 0);
    public static final v3d Y_AXIS = new v3d(0, 1, 0);
    public static final v3d Z_AXIS = new v3d(0, 0, 1);

    public double x;
    public double y;
    public double z;

    public v3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void set(v3d v) {
        this.x = v.x; this.y = v.y; this.z = v.z;
    }

    public v3d clone() {
        return new v3d(x, y, z);
    }

//    public boolean equals(vv3 v) {
//        return equals(v, Double.MIN_NORMAL);
//    }
    public boolean equals(v3d v, double epsilon) {
        return this == v ||
                (
                    Util.equals(x, v.x, epsilon) &&
                    Util.equals(y, v.y, epsilon) &&
                    Util.equals(z, v.z, epsilon)
                );
    }

    public v3d addThis(v3d v) {
        this.x += v.x; this.y += v.y; this.z += v.z;
        return this;
    }
    public v3d add(v3d v) {
        return new v3d(x + v.x, y + v.y, z + v.z);
    }

    public v3d addScale(v3d a, double b) {
        return new v3d(x + a.x * b, y + a.y * b, z + a.z * b);
    }

    public v3d minus(v3d v) {
        return new v3d(x - v.x, y - v.y, z - v.z);
    }

    public v3d scale(double s) {
        return new v3d(s * x, s * y, s * z);
    }
    public v3d scaleThis(double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        return this;
    }

    public double dot(v3d v) {
        return x*v.x + y*v.y + z*v.z;
    }

    public v3d cross(v3d v) {
        return new v3d(
            y*v.z - z*v.y,
            z*v.x - x*v.z,
            x*v.y - y*v.x
        );
    }

    public double lengthSquared() {
        return x*x + y*y + z*z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public v3d normalize() {
        if (hasZero()) {
            return this;
        }
        return scale(1 / length());
    }

    public v3d normalizeThis(double scale) {
        normalizeThis();
        return scaleThis(scale);
    }

    public v3d normalizeThis() {
        if (hasZero())
            return this;
        return scaleThis(1 / length());
    }

    private boolean hasZero() {
        return Math.abs(x) <= Double.MIN_NORMAL ||
               Math.abs(y) <= Double.MIN_NORMAL ||
               Math.abs(z) <= Double.MIN_NORMAL;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ')';
    }

    public void invertThis() {
        x = -x;
        y = -y;
        z = -z;
    }

    public double distanceSquared(v3d v) {
        if (this == v) return 0;
        return Util.sqr(x - v.x) + Util.sqr(y - v.y) + Util.sqr(z - v.z);
    }

}