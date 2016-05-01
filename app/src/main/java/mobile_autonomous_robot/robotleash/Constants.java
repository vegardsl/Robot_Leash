package mobile_autonomous_robot.robotleash;

/**
 * Created by vegarsl on 29.04.2016.
 */
public class Constants {
    public static final int BYTES_PER_FLOAT = 4;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //Control input message tags for the ControlRenderer Handler
    public static final int MESSAGE_STICK_POSITION = 6;
    public static final String STICK_POSITION = "stick_position";
}
