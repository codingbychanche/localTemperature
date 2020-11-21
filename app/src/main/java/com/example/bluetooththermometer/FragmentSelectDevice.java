package com.example.bluetooththermometer;
/**
 * This lets the user select a bt- device from a list
 * of bonded devices.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Created by Berthold on 1/13/18.
 */

public class FragmentSelectDevice extends DialogFragment {

    getDataFromFragment gf;

    List<String> itemList = new ArrayList<>();

    public FragmentSelectDevice() {
        // Constructor must be empty....
    }

    public static FragmentSelectDevice newInstance(String titel) {
        FragmentSelectDevice frag = new FragmentSelectDevice();
        Bundle args = new Bundle();
        args.putString("titel", titel);
        frag.setArguments(args);
        return frag;
    }

    /**
     * THE INTERFACE
     * <p>
     * This is the interface used to pass data from the
     * this fragment to it's activity
     */
    public interface getDataFromFragment {
        void getDialogInput(String buttonPressed,String nameOfdeviceSelected, BluetoothDevice bluetoothDeviceSelected);
    }

    /**
     * get interface Object...
     * You may use any method defined in the interface through the
     * object 'gf'
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        gf = (getDataFromFragment) activity;
    }

    /**
     * Inflate fragment layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_device, container);
    }

    /**
     * This fills the layout with data.
     *
     * @param view
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView itemListView = (ListView) view.findViewById(R.id.list);
        List<String> bondedDevicesNames = new ArrayList<>();
        final List<BluetoothDevice> bondedBluetoothDevices=new ArrayList<>();

        BluetoothManager bm = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bm.getAdapter();

        final Set<BluetoothDevice> btBondedDevices = getBondedDevices();
        final BluetoothDevice deviceSelected;

        // Get names of bonded devices and write them into the list.
        // Get devices
        if (btBondedDevices.size() > 0) {
            for (BluetoothDevice dev : btBondedDevices) {
                bondedDevicesNames.add(dev.getName()+"\n"+"Adress:"+dev.getAddress());
                bondedBluetoothDevices.add(dev);
            }
        } else {
            bondedDevicesNames.add("No bonded devices.....");
        }

        // Show list of bonded devices
        ArrayAdapter<String> itemListAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, bondedDevicesNames);
        itemListView.setAdapter(itemListAdapter);

        // Click listener
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("FRAGMENT_ITEM", "" + position);
                String nameOfDeviceSelected=bondedBluetoothDevices.get(position).getName();
                BluetoothDevice bluetoothDeviceSelected=bondedBluetoothDevices.get(position);
                gf.getDialogInput("-",nameOfDeviceSelected,bluetoothDeviceSelected);
                dismiss();
            }
        };
        itemListView.setOnItemClickListener(listener);

        // When Cancel Button is pressed, finish!
        Button cancelButton = (Button) view.findViewById(R.id.close_fragment);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gf.getDialogInput("CANCEL","-",null);
                dismiss();
            }
        });
    }

    /**
     * Fills the list with data
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private Set<BluetoothDevice> getBondedDevices() {

        BluetoothManager bm = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter blueToothAdapter = bm.getAdapter();
        Set<BluetoothDevice> btBondedDevices = blueToothAdapter.getBondedDevices();

        btBondedDevices = blueToothAdapter.getBondedDevices();

        return btBondedDevices;
    }
}
