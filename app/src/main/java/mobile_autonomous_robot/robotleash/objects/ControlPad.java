package mobile_autonomous_robot.robotleash.objects;

import android.opengl.GLES20;

import static mobile_autonomous_robot.robotleash.Constants.BYTES_PER_FLOAT;

import mobile_autonomous_robot.robotleash.data.VertexArray;
import mobile_autonomous_robot.robotleash.programs.TextureShaderProgram;

/**
 * Created by vegarsl on 29.04.2016.
 */
public class ControlPad {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            // Order of coordinates: X, Y, S, T
            // Triangle Fan
            0f, 0f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0f, 1f,
            0.5f, -0.5f, 1f, 1f,
            0.5f, 0.5f, 1f, 0f,
            -0.5f, 0.5f, 0f, 0f,
            -0.5f, -0.5f, 0f, 1f };

    private final VertexArray vertexArray;
    public ControlPad() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);
    }
}
