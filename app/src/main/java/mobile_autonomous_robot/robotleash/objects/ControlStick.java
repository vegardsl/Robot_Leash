package mobile_autonomous_robot.robotleash.objects;

import java.nio.FloatBuffer;
import java.util.List;

import mobile_autonomous_robot.robotleash.data.VertexArray;
import mobile_autonomous_robot.robotleash.programs.ColorShaderProgram;
import mobile_autonomous_robot.robotleash.util.Geometry;
import mobile_autonomous_robot.robotleash.objects.ObjectBuilder.DrawCommand;
import mobile_autonomous_robot.robotleash.objects.ObjectBuilder.GeneratedData;

/**
 * Created by Vegard on 21.04.2016.
 *
 * Code within this file is based on:
 * Kevin Brothaler. OpenGL ES 2 for Android. The Pragmatic Programmers, 2013.
 *
 */
public class ControlStick {
    private FloatBuffer vertexBuffer;

    private static final int POSITION_COMPONENT_COUNT = 3;
    public final float radius, height;
    private final VertexArray vertexArray;
    private final List<DrawCommand> drawList;
    public ControlStick(float radius, float height, int numPointsAroundStick) {
        GeneratedData generatedData = ObjectBuilder.createControlStick(new Geometry.Cylinder(
                new Geometry.Point(0f, 0f, 0f), radius, height), numPointsAroundStick);
        this.radius = radius;
        this.height = height;
        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }
    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0,
                colorProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }
    public void draw() {
        for (DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
