package com.example.dev_lautreamont.airhockey.programs;

import android.content.Context;

import com.example.dev_lautreamont.airhockey.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * AirHockey
 * com.example.dev_lautreamont.airhockey.programs
 * Created by Leblanc, Frédéric on 2015-03-23, 08:30.
 * <p/>
 * Cette classe
 */
public class ObjectShaderProgram extends ShaderProgram {

    private static final String TAG = "ObjectShaderProgram";

    // Uniform locations
    private final int uMVPMatrixLocation;
    private final int uMVMatrixLoacation;
    private final int uLightPosLocation;
    private final int uColorLocation;
    private final int uSpecularIntensityLocation;
    private final int uSpecularPowerLocation;
    private final int uEyePositionLoaction;
    // Attribute locations
    private final int aPositionLocation;
    private final int aNormalLocation;

    public ObjectShaderProgram(Context context) {
        super(context, R.raw.light_object_vertex_shader, R.raw.light_object_fragment_shader);
        // Retrieve uniform locations for the shader program.
        uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX);
        uMVMatrixLoacation = glGetUniformLocation(program, U_MV_MATRIX);
        uLightPosLocation = glGetUniformLocation(program, U_LIGHT_POSITION);
        uColorLocation = glGetUniformLocation(program, U_COLOR);
        uSpecularIntensityLocation = glGetUniformLocation(program, U_SPECULAR_INTENSITY);
        uSpecularPowerLocation = glGetUniformLocation(program, U_SPECULAR_POWER);
        uEyePositionLoaction = glGetUniformLocation(program, U_EYE_POSITION);
        //Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aNormalLocation = glGetAttribLocation(program, A_NORMAL);
    }

    public void setUniforms(float[] mvpMatrix, float[] mvMatrix, float[] lightPos, float[] eyePos, float r, float g, float b, float alpha , float specularIntensity,
                            float specularPower) {
        // Pass the uniforms into the shader program
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        glUniformMatrix4fv(uMVMatrixLoacation, 1, false, mvMatrix, 0);
        glUniform3fv(uLightPosLocation, 1, lightPos, 0);
        glUniform3fv(uEyePositionLoaction, 1, eyePos, 0);
        glUniform4f(uColorLocation, r, g, b, alpha);
        glUniform1f(uSpecularIntensityLocation, specularIntensity);
        glUniform1f(uSpecularPowerLocation, specularPower);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getNormalAttributeLocation() {
        return aNormalLocation;
    }
}
