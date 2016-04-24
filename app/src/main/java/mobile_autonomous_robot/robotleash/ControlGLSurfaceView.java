package mobile_autonomous_robot.robotleash;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Vegard on 24.04.2016.
 */
public class ControlGLSurfaceView extends GLSurfaceView{

    private final ControlGLRenderer mRenderer;

    public ControlGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        mRenderer = new ControlGLRenderer();

        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
