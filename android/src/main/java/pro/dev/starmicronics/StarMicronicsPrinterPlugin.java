package pro.dev.starmicronics;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.PluginCall;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CapacitorPlugin(
        name = "StarMicronicsPrinter",
        permissions = {
                @Permission(strings = { Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT }, alias = "bluetooth")
        }
)
public class StarMicronicsPrinterPlugin extends Plugin {
    private StarIOPort port;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> discoveredDevices = new HashSet<>();
    private PluginCall scanCall;

    @Override
    public void load() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    protected void handleOnStop() {
        super.handleOnStop();
        getContext().unregisterReceiver(receiver);
    }

    @PluginMethod
    public void scanDevices(PluginCall call) {
        if (bluetoothAdapter == null) {
            call.reject("Bluetooth is not supported on this device");
            return;
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            call.reject("Bluetooth or location permission is not granted");
            return;
        }

        scanCall = call;
        discoveredDevices.clear();
        bluetoothAdapter.startDiscovery();

        // Stop discovery after a certain time period (e.g., 10 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bluetoothAdapter.cancelDiscovery();
            List<JSObject> devices = new ArrayList<>();
            for (BluetoothDevice device : discoveredDevices) {
                JSObject deviceInfo = new JSObject();
                deviceInfo.put("name", device.getName());
                deviceInfo.put("macAddress", device.getAddress());
                devices.add(deviceInfo);
            }
            JSObject ret = new JSObject();
            ret.put("devices", new JSArray(devices));
            if (scanCall != null) {
                scanCall.resolve(ret);
                scanCall = null;
            }
        }, 30000);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (scanCall != null) {
                    List<JSObject> devices = new ArrayList<>();
                    for (BluetoothDevice device : discoveredDevices) {
                        JSObject deviceInfo = new JSObject();
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            scanCall.reject("Bluetooth connect permission not granted");
                            return;
                        }
                        deviceInfo.put("name", device.getName());
                        deviceInfo.put("macAddress", device.getAddress());
                        devices.add(deviceInfo);
                    }
                    JSObject ret = new JSObject();
                    ret.put("devices", new JSArray(devices));
                    scanCall.resolve(ret);
                    scanCall = null;
                }
            }
        }
    };

    @PluginMethod
    public void connectPrinter(PluginCall call) {
        String macAddress = call.getString("macAddress");
        if (macAddress == null) {
            call.reject("MAC address is required");
            return;
        }

        try {
            StarIOPort port = StarIOPort.getPort("BT:" + macAddress, "", 10000, getContext());
            call.resolve();
        } catch (StarIOPortException e) {
            call.reject("Failed to connect to printer", e);
        }
    }
}
