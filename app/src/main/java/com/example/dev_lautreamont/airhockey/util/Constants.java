package com.example.dev_lautreamont.airhockey.util;

/**
 * AirHockey1
 * com.example.dev_lautramont.airhockey1
 * Created by Leblanc, Frédéric on 2015-03-03, 10:42.
 * <p/>
 * Cette classe
 */
public class Constants {
    // Bytes constants
    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;

    // Table constants
    public static final float TABLE_LEFT_BOUND = -0.5f;
    public static final float TABLE_RIGHT_BOUND = 0.5f;
    public static final float TABLE_FAR_BOUND = -1.2f;
    public static final float TABLE_NEAR_BOUND = 0.8f;
    public static final float TABLE_CENTER_LINE = -0.2f;

    // Light position
    public static final float[] LIGHT_POSITION = {0.45f, 0.25f, -0.30f};
    //private float[] LIGHT_POSITION = {0.61f, -0.64f, -0.47f};

    // Board constant
    public static final float BOARD_INSIDE_AREA_WIDTH = Math.abs(TABLE_RIGHT_BOUND) + Math.abs(TABLE_LEFT_BOUND);
    public static final float BOARD_INSIDE_AREA_LENGTH = Math.abs(TABLE_NEAR_BOUND) + Math.abs(TABLE_FAR_BOUND);
    public static final float BOARD_HEIGHT = 0.06f;
    public static final float BOARD_THICKNESS = 0.04f;
}
