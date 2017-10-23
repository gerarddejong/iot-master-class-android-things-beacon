package net.iqbusiness.iotbeacon;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by gerarddejong on 2017/10/20.
 */

public class EddystoneAdvertiser {
    private static final String TAG = "EddystoneAdvertiser";

    private Context context;

    private BluetoothAdapter mBluetoothAdapter;
    private Beacon beacon;

    public EddystoneAdvertiser(Context _context) {
        this.context = _context;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "No default Bluetooth adapter. Device likely does not support bluetooth.");
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth Adapter is already enabled.");

            this.startBeaconAdvertising();
        }
        else
        {
            Log.d(TAG, "Bluetooth adapter not enabled. Enabling.");
            mBluetoothAdapter.enable();
        }
    }


    private void startBeaconAdvertising() {
        try {
            byte[] urlBytes = UrlBeaconUrlCompressor.compress("http://" + this.getWifiIp() + "/") ;
            Identifier encodedUrlIdentifier = Identifier.fromBytes(urlBytes, 0, urlBytes.length, false);
            ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
            identifiers.add(encodedUrlIdentifier);
            beacon = new Beacon.Builder()
                    .setIdentifiers(identifiers)
                    .setManufacturer(0x0118)
                    .setTxPower(-59)
                    .build();
            BeaconParser beaconParser = new BeaconParser()
                    .setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT);
            org.altbeacon.beacon.BeaconTransmitter beaconTransmitter = new org.altbeacon.beacon.BeaconTransmitter(context, beaconParser);
            beaconTransmitter.startAdvertising(beacon);
        } catch (MalformedURLException e) {
            Log.d(TAG, "That URL cannot be parsed");
        }
    }

    private String getWifiIp() {
        final WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            int ip = mWifiManager.getConnectionInfo().getIpAddress();
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                    + ((ip >> 24) & 0xFF);
        }
        return null;
    }
}
