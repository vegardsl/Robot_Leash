package mobile_autonomous_robot.robotleash;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BluetoothConnectionClient extends Service {
    public BluetoothConnectionClient() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
