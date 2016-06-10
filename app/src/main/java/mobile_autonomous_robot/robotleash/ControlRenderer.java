package mobile_autonomous_robot.robotleash;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import mobile_autonomous_robot.robotleash.objects.ControlPad;
import mobile_autonomous_robot.robotleash.objects.ControlStick;
import mobile_autonomous_robot.robotleash.programs.ColorShaderProgram;
import mobile_autonomous_robot.robotleash.programs.TextureShaderProgram;
import mobile_autonomous_robot.robotleash.util.Geometry;
import mobile_autonomous_robot.robotleash.util.Geometry.Plane;
import mobile_autonomous_robot.robotleash.util.Geometry.Point;
import mobile_autonomous_robot.robotleash.util.Geometry.Ray;
import mobile_autonomous_robot.robotleash.util.Geometry.Sphere;
import mobile_autonomous_robot.robotleash.util.Geometry.Vector;
import mobile_autonomous_robot.robotleash.util.TextureHelper;

import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
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
    private final float[] invertedViewProjectionMatrix = new float[16];

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;
    private int texture;

    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    private ControlStick controlStick;
    private ControlPad controlPad;

    private boolean controlStickPressed = false;
    private Point controlStickPosition;
    private Point prevControlStickPosition;
    private Vector deltaControlStickPosition;

    private final float leftBound = -0.5f;
    private final float rightBound = 0.5f;
    private final float upperBound = -0.8f;
    private final float lowerBound = 0.8f;

    private long currentTimeStamp_ms;
    private long lastTimeStamp_ms;
    private long deltaTime_ms;

    private final Handler mHandler;

    public ControlRenderer(Context context, Handler handler) {
        this.context = context;
        mHandler = handler;
    }
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        controlStick = new ControlStick(0.07f, 0.3f, 32);
        controlPad = new ControlPad();

        controlStickPosition = new Point(0f, 0f, controlStick.height / 2f);
        prevControlStickPosition = new Point(0f, 0f, controlStick.height / 2f);
        deltaControlStickPosition = Geometry.vectorBetween(controlStickPosition, prevControlStickPosition);
        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);
        texture = TextureHelper.loadTexture(context, R.drawable.control_pad2);

        lastTimeStamp_ms = SystemClock.currentThreadTimeMillis();
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
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        // Draw the control pad
        positionControlPadInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        controlPad.bindData(textureProgram);
        controlPad.draw();

        // Draw the control stick.
        positionObjectInScene(  controlStickPosition.x,
                                controlStickPosition.y,
                                controlStickPosition.z);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.2f, 0.2f, 1f);
        controlStick.bindData(colorProgram);
        controlStick.draw();

        lastTimeStamp_ms = currentTimeStamp_ms;
        currentTimeStamp_ms = SystemClock.currentThreadTimeMillis();
        deltaTime_ms = currentTimeStamp_ms - lastTimeStamp_ms;
        if(!controlStickPressed){

            Log.v(TAG, "dt = " +deltaTime_ms);
            centerControlStick();
        }

        // Send send stick position back to the Activity
        /*Message msg = mHandler.obtainMessage(Constants.MESSAGE_STICK_POSITION);
        Bundle bundle = new Bundle();
        float[] stickPos = {controlStickPosition.x, controlStickPosition.y};
        bundle.putFloatArray(Constants.STICK_POSITION,stickPos);
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        sendStickPosMsg(controlStickPosition.x, controlStickPosition.y);
    }

    private void sendStickPosMsg(float xPos, float yPos){
        // Send send stick position back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_STICK_POSITION);
        Bundle bundle = new Bundle();
        float[] stickPos = {xPos, yPos};
        bundle.putFloatArray(Constants.STICK_POSITION, stickPos);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
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

    private Ray convertNormalized2DPointToRay(
            float normalizedX, float normalizedY){
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};

        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        multiplyMV(nearPointWorld, 0,
                invertedViewProjectionMatrix, 0,
                nearPointNdc, 0);
        multiplyMV(farPointWorld, 0,
                invertedViewProjectionMatrix, 0,
                farPointNdc, 0);
        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        Point nearPointRay = new Point( nearPointWorld[0],
                                        nearPointWorld[1],
                                        nearPointWorld[2]);
        Point farPointRay = new Point(  farPointWorld[0],
                                        farPointWorld[1],
                                        farPointWorld[2]);
        Vector vector = Geometry.vectorBetween(nearPointRay, farPointRay);

        return new Ray(nearPointRay, vector);
    }

    private void divideByW(float[] vector){
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }


    public void handleTouchPress(float normalizedX, float normalizedY){
        Log.v(TAG, "handleTouchPress, x = " + normalizedX + " y =  " + normalizedY);
        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        // Now test if this ray intersects with the control stick by creating
        // a bounding sphere that wraps the stick.
        Sphere controlStickBoundingSphere = new Sphere(new Point(
                controlStickPosition.x,
                controlStickPosition.y,
                controlStickPosition.z),
                controlStick.height / 2f);
        // if the ray intersects with the bounding sphere,
        // then set controlStickPressed = true.
        controlStickPressed = Geometry.intersects(
                controlStickBoundingSphere, ray);
    }

    public void handleTouchDrag(float normalizedX, float normalizedY){

        if(controlStickPressed){
            Log.v(TAG, "handleTouchDrag, x = " + normalizedX + ", y = " + normalizedY );
            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            Plane plane = new Plane(new Point(0,0,0), new Vector(0, 0, 1));
            Point touchedPoint = Geometry.intersectionPoint(ray, plane);
            prevControlStickPosition = controlStickPosition;
            controlStickPosition = new Point(touchedPoint.x,
                                            touchedPoint.y,
                                            controlStick.height / 2f);
            deltaControlStickPosition = Geometry.vectorBetween(controlStickPosition,
                    prevControlStickPosition);
        }
    }

    public void handleTouchRelease(){
        controlStickPressed = false;
    }

    public void centerControlStick(){

        float xvel = 0f, yvel = 0f;
        float Fx = 0f, Fy = 0f;

        // Spring mass dampener system coefficients.
        float k = 300, c = 100f, m = 5f;

        float dt = 1f / (1000f * (float)deltaTime_ms); // In seconds;

        xvel = deltaControlStickPosition.x*dt;
        yvel = deltaControlStickPosition.y*dt;

        Fx = (-1f)*controlStickPosition.x*k;// + c*(deltaControlStickPosition.x / dt);
        Fy = (-1f)*controlStickPosition.y*k;// + c*(deltaControlStickPosition.y / dt);

        if(Math.abs(Fx) < 0.1f && Math.abs(Fy) < 0.1f){
            xvel = yvel = 0f;
        }else{
            xvel += (Fx/m)*dt;
            yvel += (Fy/m)*dt;
        }

        prevControlStickPosition = controlStickPosition;
        controlStickPosition = new Point(controlStickPosition.x + xvel,
                                        controlStickPosition.y + yvel,
                                        controlStickPosition.z);
    }
}
