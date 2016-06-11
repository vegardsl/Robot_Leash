package mobile_autonomous_robot.robotleash.objects;

/**
 * Created by Vegard on 30.04.2016.
 *
 * Code within this file is based on:
 * Kevin Brothaler. OpenGL ES 2 for Android. The Pragmatic Programmers, 2013.
 */
import java.util.List;

import mobile_autonomous_robot.robotleash.data.VertexArray;
import mobile_autonomous_robot.robotleash.objects.ObjectBuilder.DrawCommand;
import mobile_autonomous_robot.robotleash.objects.ObjectBuilder.GeneratedData;
import mobile_autonomous_robot.robotleash.programs.ColorShaderProgram;
import mobile_autonomous_robot.robotleash.util.Geometry.Point;

public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float radius;
    public final float height;

    private final VertexArray vertexArray;
    private final List<DrawCommand> drawList;

    public Mallet(float radius, float height, int numPointsAroundMallet) {
        GeneratedData generatedData = ObjectBuilder.createMallet(new Point(0f,
                0f, 0f), radius, height, numPointsAroundMallet);

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