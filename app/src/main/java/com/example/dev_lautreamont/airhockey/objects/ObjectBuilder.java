package com.example.dev_lautreamont.airhockey.objects;

import android.util.FloatMath;
import android.util.Log;

import com.example.dev_lautreamont.airhockey.util.Geometry.Circle;
import com.example.dev_lautreamont.airhockey.util.Geometry.Cylinder;
import com.example.dev_lautreamont.airhockey.util.Geometry.Point;
import com.example.dev_lautreamont.airhockey.util.Geometry.RectangularPrism;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static com.example.dev_lautreamont.airhockey.util.Geometry.Vector;
import static com.example.dev_lautreamont.airhockey.util.Geometry.vectorBetween;

/**
 * AirHockey1
 * objects
 * Created by Leblanc, Frédéric on 2015-03-04, 02:49.
 * <p/>
 * Cette classe
 */
public class ObjectBuilder {

    static interface DrawCommand {
        void draw();
    }

    static class GeneratedData {
        final float[] vertexData;
        final float[] normalData;
        final List<DrawCommand> drawList;

        GeneratedData(float[] vertexData, float[] normalData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.normalData = normalData;
            this.drawList = drawList;
        }
    }

    private static final String TAG = "ObjectBuilder";
    private static final int FLOATS_PER_VERTEX = 3;
    private static final int FLOATS_PER_NORMAL = 3;
    private final float[] vertexData;
    private final float[] normalData;
    private int offsetVertex = 0;
    private int offsetNormal = 0;
    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    private ObjectBuilder(int sizeInVertices) {

        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
        normalData = new float[sizeInVertices * FLOATS_PER_NORMAL];
    }

    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    private static int sizeOfOpenCylinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }

    private static int sizeOfHalfSphereInVertices(int numPoints) {
        return 1 + (numPoints * numPoints / 4) + numPoints;
    }

    /*private static int sizeOfSideRingInVertices(int numPoints) {
        return (numPoints + 1) * (numPoints / 2 + 1);
    }*/
    private static int sizeOfSideRingInVertices(int numPoints) {
        return ((numPoints + 1) * 2);
    }

    static GeneratedData createPuck(Cylinder puck, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints)
                + sizeOfOpenCylinderInVertices(numPoints);

        ObjectBuilder builder = new ObjectBuilder(size);

        Circle puckTop = new Circle(
                puck.center.translateY(puck.height / 2f),
                puck.radius);

        builder.appendCircle(puckTop, numPoints);
        builder.appendOpenCylinder(puck, numPoints);

        return builder.build();
    }

    static GeneratedData createMallet(Point center, float radius, float height, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) * 2
                + sizeOfOpenCylinderInVertices(numPoints) * 2
                + sizeOfHalfSphereInVertices(numPoints)
                + sizeOfSideRingInVertices(numPoints) * 3;

        ObjectBuilder builder = new ObjectBuilder(size);

        // generate the mallet base.
        float baseHeight = height * 0.50f;

        //Circle baseCircle = new Circle(center.translateY(-baseHeight * 0.25f), radius);
        Circle baseCircle = new Circle(center.translateY(-(height * 0.25f)), radius);
        Cylinder baseCylinder = new Cylinder(center.translateY(-baseHeight * 0.5f), radius, baseHeight);

        builder.appendOutsideRing(center, radius * 0.75f + ((radius - radius * 0.75f) / 2), (radius - radius * 0.75f) / 2, numPoints);
        builder.appendInsideRing(center, radius * 0.75f + ((radius - radius * 0.75f) / 2), (radius - radius * 0.75f) / 2, numPoints);
        builder.appendTruncCone(center.translateY(-height * 0.25f), baseCircle.scale(0.60f).radius, baseCircle.scale(0.75f).radius, height * 0.25f, numPoints);
        builder.appendCircle(baseCircle.scale(0.60f), numPoints);
        builder.appendOpenCylinder(baseCylinder, numPoints);

        // generate the handle.
        float handleHeight = height * 0.75f;
        float handleRadius = radius * 0.40f;

        Circle handleCircle = new Circle(center.translateY(handleHeight - height * 0.25f), handleRadius);
        Cylinder handleCylinder = new Cylinder(center.translateY(handleHeight * 0.5f - height * 0.25f), handleRadius, handleHeight);

        builder.appendCircle(handleCircle, numPoints);
        builder.appendOpenCylinder(handleCylinder, numPoints);
        builder.appendHalfSphere(handleCircle, numPoints);

        Log.e(TAG, "Size : " + size);
        Log.e(TAG, "OffsetVertex : " + (builder.offsetVertex / 3));
        Log.e(TAG, "Last vertex : " + builder.vertexData[builder.offsetVertex - 1]);

        return builder.build();
    }

    /*
     * insideAreaWidth and length of the whole board arround the plane.
     */
    static GeneratedData createBoard(Point centerPoint, float insideAreaWidth, float insideAreaLength, float boardThickness, float boardHeight) {
        // Six vertices for six faces by four prisms, could it be reduce?
        int size = 36 * 4;
        ObjectBuilder builder = new ObjectBuilder(size);

        // See for the right (height, insideAreaWidth, length) order for each prism, also see for startPoint.
        //RectangularPrism boardBottom = new RectangularPrism(centerPointOfPrism, height, insideAreaWidth, length);
        RectangularPrism boardBottom = new RectangularPrism(
                new Point(centerPoint.x, centerPoint.y - ((insideAreaLength / 2) + (boardThickness / 2)) + 0.2f,
                        centerPoint.z + boardHeight / 2),
                boardThickness, insideAreaWidth, boardHeight);
        RectangularPrism boardTop = new RectangularPrism(
                new Point(centerPoint.x, centerPoint.y + ((insideAreaLength / 2) + (boardThickness / 2)) + 0.2f,
                        centerPoint.z + boardHeight / 2),
                boardThickness, insideAreaWidth, boardHeight);


        RectangularPrism boardLeft = new RectangularPrism(
                new Point(centerPoint.x - ((insideAreaWidth / 2) + (boardThickness / 2)), centerPoint.y + 0.2f,
                        centerPoint.z + boardHeight / 2),
                insideAreaLength + (boardThickness * 2), boardThickness, boardHeight);
        RectangularPrism boardRight = new RectangularPrism(
                new Point(centerPoint.x + ((insideAreaWidth / 2) + (boardThickness / 2)), centerPoint.y + 0.2f,
                        centerPoint.z + boardHeight / 2),
                insideAreaLength + (boardThickness * 2), boardThickness, boardHeight);

        // 36 points for a single prism
        builder.appendRectangularPrism(boardBottom, size / 4);
        builder.appendRectangularPrism(boardTop, size / 4);
        builder.appendRectangularPrism(boardLeft, size / 4);
        builder.appendRectangularPrism(boardRight, size / 4);

        return builder.build();
    }

    private void appendRectangularPrism(RectangularPrism prism, final int numVertices) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;

        Point center = new Point(prism.center.x, prism.center.y, prism.center.z + prism.length / 2);
        appendRectangleBottomUp(center, prism.width, prism.height, prism.length);

        center = new Point(prism.center.x, prism.center.y, prism.center.z - prism.length / 2);
        appendRectangleBottomUp(center, prism.width, prism.height, prism.length);

        center = new Point(prism.center.x - prism.width / 2, prism.center.y, prism.center.z);
        appendRectangleRightLeft(center, prism.width, prism.height, prism.length);

        center = new Point(prism.center.x + prism.width / 2, prism.center.y, prism.center.z);
        appendRectangleRightLeft(center, prism.width, prism.height, prism.length);

        center = new Point(prism.center.x, prism.center.y + prism.height / 2, prism.center.z);
        appendRectangleNearFar(center, prism.width, prism.height, prism.length);

        center = new Point(prism.center.x, prism.center.y - prism.height / 2, prism.center.z);
        appendRectangleNearFar(center, prism.width, prism.height, prism.length);

    }

    private void appendRectangleNearFar(Point center, float width, float height, float length) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numPoints = 6;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x - width / 2;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z - length / 2;

        vertexData[offsetVertex++] = center.x + width / 2;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z - length / 2;

        vertexData[offsetVertex++] = center.x + width / 2;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z + length / 2;

        vertexData[offsetVertex++] = center.x - width / 2;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z + length / 2;

        vertexData[offsetVertex++] = center.x - width / 2;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z - length / 2;

        Point point1 = new Point(center.x, center.y, center.z);
        Point point2 = new Point(center.x + width / 2, center.y, center.z + length / 2);
        Point point3 = new Point(center.x - width / 2, center.y, center.z + length / 2);

        Vector vector1 = vectorBetween(point1, point2);
        Vector vector2 = vectorBetween(point1, point3);
        Vector normal = vector1.crossProduct(vector2).normalize();

        for (int i = 0; i < 6; i++) {
            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numPoints);
            }
        });

    }

    private void appendRectangleRightLeft(Point center, float width, float height, float length) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numPoints = 6;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y - height / 2;
        vertexData[offsetVertex++] = center.z - length / 2;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y + height / 2;
        vertexData[offsetVertex++] = center.z - length / 2;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y + height / 2;
        vertexData[offsetVertex++] = center.z + length / 2;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y - height / 2;
        vertexData[offsetVertex++] = center.z + length / 2;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y - height / 2;
        vertexData[offsetVertex++] = center.z - length / 2;

        Point point1 = new Point(center.x, center.y, center.z);
        Point point2 = new Point(center.x, center.y + height / 2, center.z + length / 2);
        Point point3 = new Point(center.x, center.y - height / 2, center.z + length / 2);

        Vector vector1 = vectorBetween(point1, point2);
        Vector vector2 = vectorBetween(point1, point3);
        Vector normal = vector1.crossProduct(vector2).normalize();

        // Normal for Face 1.
        for (int i = 0; i < 6; i++) {
            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numPoints);
            }
        });

    }

    private void appendRectangleBottomUp(Point center, float width, float height, float length) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numPoints = 6;

        vertexData[offsetVertex++] = center.x;
        vertexData[offsetVertex++] = center.y;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x - width / 2;
        vertexData[offsetVertex++] = center.y - height / 2;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x + width / 2;
        vertexData[offsetVertex++] = center.y - height / 2;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x + width / 2;
        vertexData[offsetVertex++] = center.y + height / 2;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x - width / 2;
        vertexData[offsetVertex++] = center.y + height / 2;
        vertexData[offsetVertex++] = center.z;

        vertexData[offsetVertex++] = center.x - width / 2;
        vertexData[offsetVertex++] = center.y - height / 2;
        vertexData[offsetVertex++] = center.z;

        Point point1 = new Point(center.x, center.y, center.z);
        Point point2 = new Point(center.x + width / 2, center.y + height / 2, center.z);
        Point point3 = new Point(center.x + width / 2, center.y - height / 2, center.z);

        Vector vector1 = vectorBetween(point1, point2);
        Vector vector2 = vectorBetween(point3, point1);
        Vector normal = vector1.crossProduct(vector2).normalize();

        // Normal for Face 1.
        for (int i = 0; i < 6; i++) {
            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numPoints);
            }
        });

    }

    //TODO: portion of cone, see cylinder code...
    private void appendOutsideRing(Point center, float distanceFromCenter, float radiusCircles, int numPoints) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfSideRingInVertices(numPoints);
        Point vertex;
        Vector normal;

        vertex = center;
        normal = vectorBetween(vertex, center).normalize();

        //for (int j = 0; j < 3; j++) {
            //TODO: each half circle around center
            // Around the center
            for (int i = 0; i <= numPoints; i++) {
                float angleInRadians =
                        ((float) i / (float) numPoints)
                                * ((float) Math.PI * 2f);

                // position of the center of the small circles around the center of the mallet.
                Point centerCircle = new Point(center.x + distanceFromCenter * FloatMath.cos(angleInRadians), center.y, center.z
                        + distanceFromCenter * FloatMath.sin(angleInRadians));
                Vector axisOfCenters = vectorBetween(center, centerCircle).normalize();

                //for (int j = 0; j < 3; j++) {
                //float angleInRadiansAroundCenterCircle = ((float) j / (float) numPoints / 2)
                //        * ((float) Math.PI * 2f);

                //switch (j) {
                //    case 0:

                vertex = new Point(centerCircle.x,
                        centerCircle.y + radiusCircles,
                        centerCircle.z);

                normal = vectorBetween(vertex, centerCircle).normalize();
                //TODO: x and z translate on the axis center/centerCircle
                //TODO: y is relative to angleInRadiansAroundCenterCircle

                vertexData[offsetVertex++] = vertex.x;
                vertexData[offsetVertex++] = vertex.y;
                vertexData[offsetVertex++] = vertex.z;

                normalData[offsetNormal++] = -normal.x;
                normalData[offsetNormal++] = normal.y;
                normalData[offsetNormal++] = -normal.z;

                vertex = new Point(centerCircle.x + radiusCircles * FloatMath.cos(angleInRadians),
                        centerCircle.y,
                        centerCircle.z + radiusCircles * FloatMath.sin(angleInRadians));

                normal = vectorBetween(vertex, centerCircle).normalize();
                //TODO: x and z translate on the axis center/centerCircle
                //TODO: y is relative to angleInRadiansAroundCenterCircle

                vertexData[offsetVertex++] = vertex.x;
                vertexData[offsetVertex++] = vertex.y;
                vertexData[offsetVertex++] = vertex.z;

                normalData[offsetNormal++] = -normal.x;
                normalData[offsetNormal++] = normal.y;
                normalData[offsetNormal++] = -normal.z;

            }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    private void appendInsideRing(Point center, float distanceFromCenter, float radiusCircles, int numPoints) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfSideRingInVertices(numPoints);
        Point vertex;
        Vector normal;

        vertex = center;
        normal = vectorBetween(vertex, center).normalize();

        //for (int j = 0; j < 3; j++) {
        //TODO: each half circle around center
        // Around the center
        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            // position of the center of the small circles around the center of the mallet.
            Point centerCircle = new Point(center.x + distanceFromCenter * FloatMath.cos(angleInRadians), center.y, center.z
                    + distanceFromCenter * FloatMath.sin(angleInRadians));
            Vector axisOfCenters = vectorBetween(center, centerCircle).normalize();

            //for (int j = 0; j < 3; j++) {
            //float angleInRadiansAroundCenterCircle = ((float) j / (float) numPoints / 2)
            //        * ((float) Math.PI * 2f);

            //switch (j) {
            //    case 0:

            vertex = new Point(centerCircle.x,
                    centerCircle.y + radiusCircles,
                    centerCircle.z);

            normal = vectorBetween(vertex, centerCircle).normalize();
            //TODO: x and z translate on the axis center/centerCircle
            //TODO: y is relative to angleInRadiansAroundCenterCircle

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;

            vertex = new Point(centerCircle.x - radiusCircles * FloatMath.cos(angleInRadians),
                    centerCircle.y,
                    centerCircle.z - radiusCircles * FloatMath.sin(angleInRadians));

            normal = vectorBetween(vertex, centerCircle).normalize();
            //TODO: x and z translate on the axis center/centerCircle
            //TODO: y is relative to angleInRadiansAroundCenterCircle

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normalData[offsetNormal++] = -normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = -normal.z;

        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    private void appendTruncCone(Point centerBottom, float radiusBottom, float radiusTop, float height, int numPoints) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfSideRingInVertices(numPoints);
        Point vertex;
        Vector normal;

        vertex = centerBottom;
        normal = vectorBetween(vertex, centerBottom).normalize();

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            vertex = new Point(centerBottom.x + radiusBottom * FloatMath.cos(angleInRadians),
                    centerBottom.y,
                    centerBottom.z + radiusBottom * FloatMath.sin(angleInRadians));

            normal = vectorBetween(vertex, centerBottom).normalize();

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;

            vertex = new Point(centerBottom.x + radiusTop * FloatMath.cos(angleInRadians),
                    centerBottom.y + height,
                    centerBottom.z + radiusTop * FloatMath.sin(angleInRadians));

            normal = vectorBetween(vertex, centerBottom.translateY(height)).normalize();

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;

        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    private void appendHalfSphere(Circle baseCircle, int numPointsBase) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfHalfSphereInVertices(numPointsBase);
        Point vertex;
        Vector normal;

        Point pole = new Point(baseCircle.center.x, baseCircle.center.y + baseCircle.radius, baseCircle.center.z);
        vertexData[offsetVertex++] = pole.x;
        vertexData[offsetVertex++] = pole.y;
        vertexData[offsetVertex++] = pole.z;

        normal = vectorBetween(pole, baseCircle.center).normalize();

        normalData[offsetNormal++] = normal.x;
        normalData[offsetNormal++] = normal.y;
        normalData[offsetNormal++] = normal.z;

        int numRings = numPointsBase / 4;
        for (int i = 0; i <= numRings; i++) {
            float angleInRadiansForRing = ((float) i / (float) numRings) * ((float) Math.PI / 2f);
            float ringRadius = (baseCircle.radius * FloatMath.sin(angleInRadiansForRing));
            float heightBetweenRings = (i * (baseCircle.radius / numRings) * FloatMath.sin(angleInRadiansForRing));
            Circle ring = new Circle(new Point(baseCircle.center.x, pole.y - heightBetweenRings, baseCircle.center.z),
                    ringRadius);
            for (int j = 0; j < numPointsBase; j++) {
                float angleInRadians = ((float) j / (float) numPointsBase) * ((float) Math.PI * 2f);

                vertex = new Point(ring.center.x + ringRadius * FloatMath.cos(angleInRadians), ring.center.y,
                        ring.center.z + ringRadius * FloatMath.sin(angleInRadians));


                vertexData[offsetVertex++] = vertex.x;
                vertexData[offsetVertex++] = vertex.y;
                vertexData[offsetVertex++] = vertex.z;

                normal = vectorBetween(vertex, baseCircle.center).normalize();

                normalData[offsetNormal++] = -normal.x;
                normalData[offsetNormal++] = normal.y;
                normalData[offsetNormal++] = -normal.z;
            }
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }

    private void appendCircle(Circle circle, int numPoints) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);
        Point vertex;
        Vector normal;

        // Center center of fan
        vertexData[offsetVertex++] = circle.center.x;
        vertexData[offsetVertex++] = circle.center.y;
        vertexData[offsetVertex++] = circle.center.z;

        normal = vectorBetween(circle.center, circle.center.translateY(-1)).normalize();

        normalData[offsetNormal++] = normal.x;
        normalData[offsetNormal++] = normal.y;
        normalData[offsetNormal++] = normal.z;

        // Fan around center center. <= is used because we want to generate
        // the center at the starting angle twice to complete the fan.
        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            vertex = new Point(circle.center.x + circle.radius * FloatMath.cos(angleInRadians), circle.center.y, circle.center.z
                    + circle.radius * FloatMath.sin(angleInRadians));

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normal = vectorBetween(vertex, vertex.translateY(-1)).normalize();

            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }

    private void appendOpenCylinder(Cylinder cylinder, int numPoints) {
        final int startVertex = offsetVertex / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
        final Point pointStart = cylinder.center.translateY(-cylinder.height / 2f);
        final Point poinEnd = cylinder.center.translateY(cylinder.height / 2f);
        Point vertex;
        Vector normal;

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            vertex = new Point(cylinder.center.x + cylinder.radius * FloatMath.cos(angleInRadians), pointStart.y, cylinder.center.z
                    + cylinder.radius * FloatMath.sin(angleInRadians));

            normal = vectorBetween(pointStart, vertex).normalize();

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;

            vertex = new Point(cylinder.center.x + cylinder.radius * FloatMath.cos(angleInRadians), poinEnd.y, cylinder.center.z
                    + cylinder.radius * FloatMath.sin(angleInRadians));

            normal = vectorBetween(poinEnd, vertex).normalize();

            vertexData[offsetVertex++] = vertex.x;
            vertexData[offsetVertex++] = vertex.y;
            vertexData[offsetVertex++] = vertex.z;

            normalData[offsetNormal++] = normal.x;
            normalData[offsetNormal++] = normal.y;
            normalData[offsetNormal++] = normal.z;
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    private GeneratedData build() {
        return new GeneratedData(vertexData, normalData, drawList);
    }
}
