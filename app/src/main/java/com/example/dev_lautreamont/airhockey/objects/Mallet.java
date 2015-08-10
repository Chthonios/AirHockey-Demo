package com.example.dev_lautreamont.airhockey.objects;

import com.example.dev_lautreamont.airhockey.data.VertexArray;
import com.example.dev_lautreamont.airhockey.programs.ObjectShaderProgram;

import java.util.List;

import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.DrawCommand;
import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.GeneratedData;
import static com.example.dev_lautreamont.airhockey.util.Geometry.Point;


/**
 * AirHockey1
 * objects
 * Created by Leblanc, Frédéric on 2015-03-03, 11:22.
 * <p/>
 * Cette classe
 */
public class Mallet {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;

    public final float radius;
    public final float height;

    private final VertexArray vertexArray;
    private final VertexArray normalArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Mallet(float radius, float height, int numPointsAroundMallet) {
        GeneratedData generatedData = ObjectBuilder.createMallet(new Point(0f, 0f, 0f), radius, height, numPointsAroundMallet);

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
