package mobile_autonomous_robot.robotleash;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Vegard on 09.04.2016.
 */


public class DeviceListAdapter extends BaseAdapter {

    /**
     * Indicates the remote device is not bonded (paired).
     * <p>There is no shared link key with the remote device, so communication
     * (if it is allowed at all) will be unauthenticated and unencrypted.
     */
    public static final int BOND_NONE = 10;
    /**
     * Indicates bonding (pairing) is in progress with the remote device.
     */
    public static final int BOND_BONDING = 11;
    /**
     * Indicates the remote device is bonded (paired).
     * <p>A shared link keys exists locally for the remote device, so
     * communication can be authenticated and encrypted.
     * <p><i>Being bonded (paired) with a remote device does not necessarily
     * mean the device is currently connected. It just means that the pending
     * procedure was completed at some earlier time, and the link key is still
     * stored locally, ready to use on the next connection.
     * </i>
     */
    public static final int BOND_BONDED = 12;

    ArrayList<BluetoothDevice> deviceArrayList;
    Context context;
    DeviceListAdapter(Context c){
        context = c;
        deviceArrayList = new ArrayList<BluetoothDevice>();
    }

    @Override
    public int getCount() {

        return deviceArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View row = convertView;
        ViewHolder holder = null;

        // Optimization of the listView. Find layout objects and store in holder.
        //Thanks to "slidenerd" on youtube.
        if(row == null) { //First call.
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.device_list_row, parent, false);
            holder = new ViewHolder(row);

            row.setTag(holder);
        }else{ // Recycling the view. Access the previously stored holder.
            holder = (ViewHolder)row.getTag();
        }

        BluetoothDevice device = deviceArrayList.get(position);
        holder.nameText.setText(device.getName());
        //TODO: holder.proximityText.setText(device.get);
        holder.pairingText.setText(getBondState(device));
        holder.addressText.setText(device.getAddress());

        return row;
    }

    private String getBondState(BluetoothDevice device){
        int bondState = device.getBondState();
        if(bondState == BOND_BONDED)
        {
            return "PAIRED";
        }else if (bondState == BOND_BONDING){
            return "PAIRING";
        }
        return "NOT PAIRED";
    }

    static class ViewHolder{
        TextView nameText;
        TextView proximityText;
        TextView pairingText;
        TextView addressText;

        ViewHolder(View v){
            nameText = (TextView) v.findViewById(R.id.name_textView);
            proximityText = (TextView) v.findViewById(R.id.proximity_textView);
            pairingText = (TextView) v.findViewById(R.id.pairing_textView);
            addressText = (TextView) v.findViewById(R.id.mac_textView);
        }
    }
}


