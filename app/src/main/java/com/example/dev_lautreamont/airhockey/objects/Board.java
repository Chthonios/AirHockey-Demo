package com.example.dev_lautreamont.airhockey.objects;

import com.example.dev_lautreamont.airhockey.data.VertexArray;
import com.example.dev_lautreamont.airhockey.programs.ObjectShaderProgram;

import java.util.List;

import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.DrawCommand;
import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.GeneratedData;
import static com.example.dev_lautreamont.airhockey.objects.ObjectBuilder.createBoard;
import static com.example.dev_lautreamont.airhockey.util.Geometry.Point;

/**
 * AirHockey
 * com.example.dev_lautreamont.airhockey.objects
 * Created by Leblanc, Frédéric on 2015-03-20, 11:56.
 * <p/>
 * Cette classe
 */
public class Board {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;

    public final float boardThickness;
    public final float boardHeight;
    public final float insideAreaWidth;
    public final float insideAreaLength;

    private final VertexArray vertexArray;
    private final VertexArray normalArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Board(float insideAreaWidth, float insideAreaLength, float boardThickness, float boardHeight) {
        GeneratedData generatedData = createBoard(new Point(0f, 0f, 0f), insideAreaWidth, insideAreaLength, boardThickness, boardHeight);

        this.boardHeight = boardHeight;
        this.boardThickness = boardThickness;
        this.insideAreaWidth = insideAreaWidth;
        this.insideAreaLength = insideAreaLength;

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
