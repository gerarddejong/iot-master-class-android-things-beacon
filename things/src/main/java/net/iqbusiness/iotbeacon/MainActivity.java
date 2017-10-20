package net.iqbusiness.iotbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import static android.content.ContentValues.TAG;

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 *
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
public class MainActivity extends Activity {
    // GPIO Pin Name
    private static final String GPIO_PIN_RELAY1 = "BCM23";
    private static final String GPIO_PIN_RELAY2 = "BCM22";

    private Gpio relay1;
    private Gpio relay2;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "No default Bluetooth adapter. Device likely does not support bluetooth.");
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth Adapter is already enabled.");

            this.startBeacon();
        }
        else {
            Log.d(TAG, "Bluetooth adapter not enabled. Enabling.");
            mBluetoothAdapter.enable();
        }

        //this.switchRelays();

    }

    private void startBeacon() {
//        Beacon beacon = new Beacon.Builder()
//                .setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6")
//                .setId2("1")
//                .setId3("2")
//                .setManufacturer(0x4C00)
//                .setBeaconTypeCode(0x0215)
//                .setTxPower(-100)
//                .setDataFields(Arrays.asList(new Long[] {0l}))
//                .setServiceUuid(1)
//                .build();
//        BeaconParser beaconParser = new BeaconParser()
//                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
//        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
//
//        int result = BeaconTransmitter.checkTransmissionSupported(getApplicationContext());
//
//        beaconTransmitter.startAdvertising(beacon);

//        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
//        beaconManager.bind(this);

        Beacon beacon = new Beacon.Builder()
                .setId1("6fb0e0e9-2ae6-49d3-bba3-3cb7698c77e2")
                .setId2(Integer.toString(2))
                .setId3(Integer.toString(3))
                .setManufacturer(0x0000)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l}))
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon);
    }

    private void switchRelays() {
        // Attempt to access the GPIO
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            relay1 = manager.openGpio(GPIO_PIN_RELAY1);

            relay1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            relay1.setActiveType(Gpio.ACTIVE_LOW);

            relay2 = manager.openGpio(GPIO_PIN_RELAY2);

            relay2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            relay2.setActiveType(Gpio.ACTIVE_LOW);

            Log.w("Main Activity", "Off?");

            TimeUnit.SECONDS.sleep(1);
            relay1.setValue(true);
            relay2.setValue(false);
            Log.w("Main Activity", "On?");

            TimeUnit.SECONDS.sleep(1);
            relay1.setValue(false);
            relay2.setValue(true);
            Log.w("Main Activity", "Off?");

            TimeUnit.SECONDS.sleep(1);
            relay1.setValue(true);
            relay2.setValue(false);
            Log.w("Main Activity", "On?");


            TimeUnit.SECONDS.sleep(1);
            relay1.setValue(false);
            relay2.setValue(false);
            Log.w("Main Activity", "Off?");
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }
        catch (InterruptedException e) {
            Log.w(TAG, "TimeUnit Interrupted", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (relay1 != null) {
            try {
                relay1.close();
                relay1 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
        }

        if (relay2 != null) {
            try {
                relay2.close();
                relay2 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
        }
    }
}