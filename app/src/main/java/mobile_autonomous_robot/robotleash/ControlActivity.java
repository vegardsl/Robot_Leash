package mobile_autonomous_robot.robotleash;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Arrays;

import mobile_autonomous_robot.robotleash.bluetooth.BluetoothConnectionService;


public class ControlActivity extends Activity{
    // Debugging.
    private static final String TAG = "ControlActivity";

    public GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object tor the comm. service.
    private BluetoothConnectionService mBluetoothConnectionService;

    /**
     * Instances of static inner classes do not hold an implicit
     * reference to their outer class.
     */
    private static class MyHandler extends Handler {
        private final WeakReference<ControlActivity> mActivity;

        public MyHandler(ControlActivity activity) {
            mActivity = new WeakReference<ControlActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ControlActivity activity = mActivity.get();
            if (activity != null) {
                // ...
            }
            switch(msg.what){
                case Constants.MESSAGE_STICK_POSITION:
                    float[] pos = msg.getData().getFloatArray(Constants.STICK_POSITION);
                    Log.d(TAG,"Stick position: x = " + pos[0] + " y = " + pos[1]);
                    int x = Math.round(pos[0] * 400);
                    int y = Math.round(pos[1] * 400);
                    // Overflow checks and bounds.
                    if(x > 127){
                        x = 127;
                    }else if(x < -127){
                        x = -127;
                    }
                    if(y > 127){
                        y = 127;
                    }else if(y < -127){
                        y = -127;
                    }
                    byte[] array_x = BigInteger.valueOf(x).toByteArray();
                    byte[] array_y = BigInteger.valueOf(y).toByteArray();
                    int msg_size = array_x.length + array_y.length + 3;
                    int size_x = array_x.length;
                    int size_y = array_y.length;
                    Log.d(TAG, "x_size: " + size_x);
                    Log.d(TAG, "y_size: " + size_y);
                    byte[] combined = new byte[msg_size];
                    //combined[0] = 0x73;
                   // for (int i = 0; i < combined.length-1; ++i)
                  //  {
                  //      combined[i] = i < array_x.length ? array_x[i] : array_y[i - array_x.length];
                  //  }
                    combined[0] = 0x73;
                    combined[1] = array_x[0];
                    combined[2] = 0x61;
                    combined[3] = array_y[0];
                    combined[4] = 0x0A;
                    //for(int i = 0; i < array_x.length)
                    System.out.println(Arrays.toString(combined));
                    //combined[array_x.length + array_y.length] = 0x0A; // End of line in UTF-8. Robot reads line by line.
                    activity.sendMessage(combined);

            }
        }
    }

    private final MyHandler mRendererHandler = new MyHandler(this);

// ----- Activity-specific methods -----
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        Intent intent = getIntent();
        BluetoothDevice device = getIntent().getExtras().getParcelable(StartActivity.DEVICE_MESSAGE);
        Toast.makeText(getApplicationContext(),
                "You selected:" + device.getName(), Toast.LENGTH_SHORT).show();


        ParcelUuid list[] = device.getUuids();
        if(list != null){
            for(int i = 0; i < list.length; i++){
                Log.d(TAG, "Device uuid: " + list[i].toString());
            }
        }




        glSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();

        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        final ControlRenderer controlRenderer = new ControlRenderer(this, mRendererHandler);
        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);
            // Assign our renderer.
            glSurfaceView.setRenderer(controlRenderer);
            rendererSet = true;
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth is not available.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if(mBluetoothConnectionService == null){
            setupBtComm();
        }

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    /*
                    * Convert touch coordinates into normalized device
                    * coordinates, keeping in mind that Android's Y
                    * coordinates are inverted.
                    */
                    final float normalizedX =
                            (event.getX() / (float) v.getWidth()) * 2 - 1;
                    final float normalizedY =
                            -((event.getY() / (float) v.getHeight()) * 2 - 1);
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                controlRenderer.handleTouchPress(
                                        normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                controlRenderer.handleTouchDrag(
                                        normalizedX, normalizedY);
                            }
                        });
                    } else if(event.getAction() == MotionEvent.ACTION_UP){
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                controlRenderer.handleTouchRelease();
                            }
                        });
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        setContentView(glSurfaceView);

        connectDevice(device, true);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(rendererSet){
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(rendererSet){
            glSurfaceView.onResume();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupBtComm(){
        Log.d(TAG, "setupBtComm()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        //mBluetoothConnectionService = new BluetoothConnectionService(getActivity(), mHandler);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothConnectionService = new BluetoothConnectionService(this, mBtHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(byte[] message){//String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothConnectionService.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            //Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message;
            mBluetoothConnectionService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    private void connectDevice(BluetoothDevice device, boolean secure) {

        // Attempt to connect to the device
        mBluetoothConnectionService.connect(device, secure);
    }

    /**
     * The Handler that gets information back from the BluetoothConnectionService
     */
    private final Handler mBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Activity activity = this.
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            Log.d(TAG, "STATE_CONNECTED");
                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            Log.d(TAG, "STATE_CONNECTING");
                            break;
                        case BluetoothConnectionService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            Log.d(TAG, "STATE_NONE");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Log.d(TAG, "Connected to " + mConnectedDeviceName);
                    //if (null != activity) {
                   //     Toast.makeText(this, "Connected to "
                    //            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    //}

                    break;
                case Constants.MESSAGE_TOAST:
                    /*
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    */
                    break;
            }
        }
    };
/*
    private final Handler mRendererHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case Constants.MESSAGE_STICK_POSITION:
                    float[] pos = msg.getData().getFloatArray(Constants.STICK_POSITION);
                    Log.d(TAG,"Stick position: x = " + pos[0] + " y = " + pos[1]);
                    int x = Math.round(pos[0] * 400);
                    int y = Math.round(pos[1] * 400);
                    // Overflow checks and bounds.
                    if(x > 127){
                        x = 127;
                    }else if(x < -127){
                        x = -127;
                    }
                    if(y > 127){
                        y = 127;
                    }else if(y < -127){
                        y = -127;
                    }
                    byte[] array_x = BigInteger.valueOf(x).toByteArray();
                    byte[] array_y = BigInteger.valueOf(y).toByteArray();
                    byte[] combined = new byte[array_x.length + array_y.length];
                    for (int i = 0; i < combined.length; ++i)
                    {
                        combined[i] = i < array_x.length ? array_x[i] : array_y[i - array_x.length];
                    }
                    System.out.println(Arrays.toString(combined));
                    this.sendMessage(combined);

            }
        }
    };
*/
}
