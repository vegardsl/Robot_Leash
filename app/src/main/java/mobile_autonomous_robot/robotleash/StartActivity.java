package mobile_autonomous_robot.robotleash;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class StartActivity extends Activity {
    public final static String DEVICE_MESSAGE = "mobile_autonomous_robot.robotleash.MESSAGE";

    //Locally defined integer greater than one.
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter;
    DeviceListAdapter deviceListAdapter;
    ArrayList<BluetoothDevice> deviceArrayList = new ArrayList<>();

    ListView deviceList;

    Button scanButton;
    boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        deviceList = (ListView) findViewById(R.id.deviceListView);
        deviceListAdapter = new DeviceListAdapter(this);
        deviceList.setAdapter(deviceListAdapter);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice)deviceList.getItemAtPosition(position);
                onListItemClickHandler(view, device);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            // Device does not support Bluetooth.
            Toast.makeText(this, "Bluetooth is not available.",
                    Toast.LENGTH_LONG).show();
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        queryPairedDevices();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scanButtonClickHandler();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        // TODO: Clear list
        deviceListAdapter.deviceArrayList.clear();
        queryPairedDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                deviceListAdapter.deviceArrayList.add(device);
                Log.i("TAG", "Found new device");
                deviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    private void queryPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                deviceListAdapter.deviceArrayList.add(device);
                Log.i("TAG", "Found paired device");
                deviceListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void scanButtonClickHandler()
    {
        Log.i("TAG", "Scan Button Clicked");

        if(mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();
        }
        scanning = true;
        mBluetoothAdapter.startDiscovery();

        return;
    }

    public void onListItemClickHandler(View view, BluetoothDevice device) {
        // Do something when a list item is clicked
        //Toast.makeText(getApplicationContext(),
        //        "You selected:" + device.getName(), Toast.LENGTH_SHORT).show();
        startControlActivity(view, device);

    }


    public void startControlActivity(View view, BluetoothDevice device) {
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra(DEVICE_MESSAGE,device);
        startActivity(intent);
    }
}
