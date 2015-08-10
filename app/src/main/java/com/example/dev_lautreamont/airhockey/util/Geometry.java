package com.example.dev_lautreamont.airhockey.util;

import android.util.FloatMath;

/**
 * AirHockey1
 * util
 * Created by Leblanc, Frédéric on 2015-03-04, 02:28.
 * <p/>
 * Cette classe
 */
public class Geometry {

    public static class Point {
        public final float x, y, z;
        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(x + vector.x, y + vector.y, z + vector.z);
        }
    }

    public static class Circle {
        public final Point center;
        public final float radius;

        public Circle(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    public static class Cylinder {
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height) {
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }

    public static class Cone {
        public final Circle circleTop;
        public final Circle circleBottom;
        public final float height;

        public Cone(Circle circleTop, Circle circleBottom, float height){
            this.circleTop = circleTop;
            this.circleBottom = circleBottom;
            this.height = height;
        }

    }

    public static class Ray {
        public final Point point;
        public final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    public static class Vector {
        public final float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return FloatMath.sqrt(x * x + y * y + z *z);
        }

        public Vector crossProduct(Vector other) {
            return new Vector(
                    (y * other.z) - (z * other.y),
                    (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x));
        }

        public float dotProduct(Vector other) {
            return x * other.x + y * other.y + z * other.z;
        }

        public Vector scale(float f) {
            return new Vector(x * f, y * f, z * f);
        }

        public Vector add(Vector other) {
            return new Vector(x + other.x, y + other.y, z + other.z);
        }

        public Vector rotateOnYPlane(float angle) {
            float x = (float) (this.x * Math.cos(angle) + this.z * Math.sin(angle));
            float z = (float) (this.x * -Math.sin(angle) + this.z * Math.cos(angle));
            return new Vector(x ,y ,z);
        }

        public Vector adjustOnYPlane(float yAdjustment) {
            return new Vector(x, yAdjustment, z);
        }

        public Vector normalize() {
            return scale(1f / length());
        }
    }

    public static class Sphere {
        public final Point center;
        public final float radius;

        public Sphere(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }
    }

    public static class Plane {
        public final Point point;
        public final Vector normal;

        public Plane(Point point, Vector normal) {
            this.point = point;
            this.normal = normal;
        }
    }

    public static class RectangularPrism {
        public final Point center;
        public final float height;
        public final float width;
        public final float length;

        public RectangularPrism(Point center, float height, float width, float length) {
            this.center = center;
            this.height = height;
            this.width = width;
            this.length = length;
        }
    }

    public static Vector vectorBetween(Point from, Point to) {
        return new Vector(to.x - from.x, to.y - from.y, to.z - from.z);
    }

    public static boolean intersects(Sphere sphere, Ray ray) {
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    public static float distanceBetween(Point point, Ray ray) {
        Vector p1ToPoint = vectorBetween(ray.point, point);
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);

        float areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length();
        float lengthOfBase = ray.vector.length();

        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;
    }

    public static float angleBetween(Vector v1, Vector v2) {
        float angle;
        Vector vector1 = new Vector(v1.x, 0f, v1.z);
        Vector vector2 = new Vector(v2.x, 0f, v2.z);
        angle = (float) Math.acos(vector1.normalize().dotProduct(vector2.normalize()));
        return angle;
    }

    public static Point intersectionPoint(Ray ray, Plane plane) {
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);

        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal) / ray.vector.dotProduct(plane.normal);

        Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
        return intersectionPoint;
    }
}
