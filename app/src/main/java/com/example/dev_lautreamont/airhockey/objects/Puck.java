package com.example.dev_lautreamont.airhockey.objects;

import com.example.dev_lautreamont.airhockey.data.VertexArray;
import com.example.dev_lautreamont.airhockey.programs.ObjectShaderProgram;

import java.util.List;

import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.DrawCommand;
import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.GeneratedData;
import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.createPuck;
import static com.example.dev_lautreamont.airhockey.util.Geometry.Cylinder;
import static com.example.dev_lautreamont.airhockey.util.Geometry.Point;

/**
 * AirHockey1
 * objects
 * Created by Leblanc, Frédéric on 2015-03-04, 04:07.
 * <p/>
 * Cette classe
 */
public class Puck {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;

    public final float radius, height;

    private final VertexArray vertexArray;
    private final VertexArray normalArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Puck(float radius, float height, int numPointsAroundPuck) {
        GeneratedData generatedData = createPuck(new Cylinder(new Point(0f, 0f, 0f), radius, height),
                numPointsAroundPuck);

        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generatedData.vertexData);
        normalArray = new VertexArray(generatedData.normalData);
        drawList = generatedData.drawList;
    }

    public void bindData(ObjectShaderProgram objectProgram) {
        vertexArray.setVertexAttribPointer(0, objectProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0);
        normalArray.setVertexAttribPointer(0, objectProgram.getNormalAttributeLocation(), NORMAL_COMPONENT_COUNT, 0);
    }

    public void draw() {
        for (DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
