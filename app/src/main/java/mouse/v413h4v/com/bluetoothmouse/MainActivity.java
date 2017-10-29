package mouse.v413h4v.com.bluetoothmouse;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    BluetoothAdapter btAdapter;

    ArrayAdapter<String> listAdapter;
    Button discoverButton;
    ListView deviceList;
    ArrayList<String> discoveredDevices;
    ArrayList<BluetoothDevice> deviceToBePaired;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"Inside BroadcastReceiver.");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                Log.d(TAG,"Inside BroadcastReceiver.");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

               // String deviceName = device.getName();
                // String deviceHardwareAddress = device.getAddress(); // MAC address

                String listItem = device.getName() + " ( " + device.getAddress() + " )";
                Log.d(TAG,"Discovered Device: "+listItem);
                discoveredDevices.add(listItem);
                deviceToBePaired.add(device);

                listAdapter.notifyDataSetChanged();
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                Log.d(TAG,"Inside BroadcastReceiver: BOND_STATE_CHANGED");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG,"Paired with: "+device.getName());
                    Toast.makeText(getApplicationContext(), "Paired with: " + device.getName(), Toast.LENGTH_SHORT).show();

                    if(btAdapter.isDiscovering()){
                        btAdapter.cancelDiscovery();
                    }

                    Intent newIntent = new Intent(getApplicationContext(),TouchpadInterface.class);
                    newIntent.putExtra("EXTRA_MAC_ADDR",device.getAddress());
                    startActivity(newIntent);
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
    }

    private void init() {
        Log.d(TAG,"init(): Inside init()");
        discoverButton = (Button) findViewById(R.id.btDiscover);
        deviceList = (ListView) findViewById(R.id.discoverList);
        discoveredDevices = new ArrayList<>();
        deviceToBePaired = new ArrayList<>();
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,discoveredDevices);
        deviceList.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices(view);
            }
        });

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedDevice = (String)deviceList.getItemAtPosition(i);
                BluetoothDevice device = deviceToBePaired.get(i);

                Toast.makeText(getApplicationContext(), "Selected device: " + selectedDevice, Toast.LENGTH_SHORT).show();

                IntentFilter bondIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(mReceiver,bondIntent);

                try{
                    if(device.getBondState() == BluetoothDevice.BOND_NONE){
                        Method method = device.getClass().getMethod("createBond",(Class[]) null);
                        method.invoke(device, (Object[])null);
                    }

//                    Toast.makeText(getApplicationContext(), "Selected device: " + selectedDevice, Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }



            }
        });

        enableBT();
    }

    private void discoverDevices(View view) {
        checkBTPermissions();
        if(btAdapter.isDiscovering()){
            Log.d(TAG, "discoverDevices(): Canceling current discovery..");
            btAdapter.cancelDiscovery();

            Log.d(TAG, "discoverDevices(): starting new discovery..");
            btAdapter.startDiscovery();
            IntentFilter btDiscoveryIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, btDiscoveryIntent);
            Log.d(TAG,"Intent fired");

        }else{
            Log.d(TAG, "discoverDevices(): Starting discovery..");
            btAdapter.startDiscovery();
            IntentFilter btDiscoveryIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, btDiscoveryIntent);
            Log.d(TAG,"Intent fired");
        }

    }

    private void enableBT() {
        if(btAdapter != null){
            if(!btAdapter.isEnabled()){
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);

                Intent discoverBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverBTIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
                startActivity(discoverBTIntent);

            }else{
                Toast.makeText(this, "Good. Bluetooth is already enabled.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "This device does not support bluetooth.", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"This device does not support Bluetooth");
        }
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
