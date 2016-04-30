package mobile_autonomous_robot.robotleash;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import mobile_autonomous_robot.robotleash.objects.ControlPad;
import mobile_autonomous_robot.robotleash.objects.ControlStick;
import mobile_autonomous_robot.robotleash.objects.Mallet;
import mobile_autonomous_robot.robotleash.objects.Puck;

import mobile_autonomous_robot.robotleash.programs.ColorShaderProgram;
import mobile_autonomous_robot.robotleash.programs.TextureShaderProgram;
import mobile_autonomous_robot.robotleash.util.TextureHelper;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Vegard on 24.04.2016.
 */
public class ControlRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "ControlRenderer";

    private final Context context;
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;
    private int texture;

    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    private ControlStick controlStick;
    private ControlPad controlPad;

    private Mallet mallet;
    private Puck puck;

    public ControlRenderer(Context context) {
    this.context = context;
}
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        controlStick = new ControlStick(0.05f, 0.7f, 32);
        controlPad = new ControlPad();

        mallet = new Mallet(0.08f, 0.15f, 32);
        puck = new Puck(0.06f, 0.02f, 32);

        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);
        texture = TextureHelper.loadTexture(context, R.drawable.control_pad2);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        GLES20.glViewport(0, 0, width, height);

        perspectiveM(projectionMatrix, 0, 45, (float) width
                / (float) height, 1f, 10f);
        setLookAtM(viewMatrix, 0, 0f, 0f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        positionControlPadInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        controlPad.bindData(textureProgram);
        controlPad.draw();

        // Draw the control stick.
        positionObjectInScene(0f, 0f, controlStick.height / 2f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.2f, 0.2f, 1f);
        controlStick.bindData(colorProgram);
        controlStick.draw();
/*
        positionObjectInScene(0f, 0.6f, mallet.height / 2f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
        mallet.bindData(colorProgram);
        mallet.draw();
  */
    }

    private void positionControlPadInScene() {
        setIdentityM(modelMatrix, 0);
        rotateM(modelMatrix, 0, 0f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

}
