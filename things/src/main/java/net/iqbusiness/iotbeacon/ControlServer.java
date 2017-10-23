package net.iqbusiness.iotbeacon;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by gerarddejong on 2017/10/20.
 */

public class ControlServer implements Runnable {
    private static final String TAG = "ControlServer";

    // GPIO Pins
    private static final String GPIO_PIN_RELAY1 = "BCM23";
    private static final String GPIO_PIN_RELAY2 = "BCM22";
    private Gpio relay1;
    private Gpio relay2;

//    private static final String[] GPIO_PIN_NAMES = new String[]{"BCM23", "BCM22"};
//    private Gpio[] relays;
    private PeripheralManagerService manager;


    private static final int TCP_SERVER_PORT = 8080;

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

        manager = new PeripheralManagerService();

        try {
            relay1 = manager.openGpio(GPIO_PIN_RELAY1);
            relay1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            relay1.setActiveType(Gpio.ACTIVE_LOW);

            relay2 = manager.openGpio(GPIO_PIN_RELAY2);
            relay2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            relay2.setActiveType(Gpio.ACTIVE_LOW);
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }

        this.startServer();
    }

    private void startServer() {
        ServerSocket ss = null;
        try {
            Log.i(TAG, "Waiting for instructions ... ");
            ss = new ServerSocket(TCP_SERVER_PORT);
            //ss.setSoTimeout(10000);

            //accept connections
            while(true) {
                Socket s = ss.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                String outgoingMessage = relayStatusReport();
                writeMessage(out, outgoingMessage);
                Log.i(TAG, "Sent: " + outgoingMessage);

                String inputMessage = readMessage(in);
                Log.i(TAG, "Received: " + inputMessage);

                Gson gson = new Gson();
                try {
                    JsonObject jsonObject = gson.fromJson(inputMessage, JsonObject.class);
                    int relay = jsonObject.get("relay").getAsInt();

                    switch(relay) {
                        case 1 : toggleRelay1(); break;
                        case 2 : toggleRelay2(); break;
                    }

                    outgoingMessage = relayStatusReport();
                    writeMessage(out, outgoingMessage);
                    Log.i(TAG, "Sent: " + outgoingMessage);
                }
                catch (NullPointerException e) {
                    Log.w(TAG, "NullPointerException ", e);
                }

                in.close();
                out.close();
                s.close();
            }
        }
        catch (SocketException e) {
            Log.w(TAG, "Socket exception ", e);
        }
        catch (InterruptedIOException e) {
            Log.w(TAG, "Server socket timeout ", e);
        }
        catch (IOException e) {
            Log.w(TAG, "IOException ", e);
        }
        finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    Log.w(TAG, "Unable to close server socket ", e);
                }
            }
        }
    }

    private String relayStatusReport() {
        String report = "{";
        try {
            report += "\"relay1\":" + (relay1.getValue() ? "1" : "0");
            report += ",";
            report += "\"relay2\":" + (relay2.getValue() ? "1" : "0");
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO ", e);
        }
        report += "}" + System.getProperty("line.separator");
        return report;
    }

    private String readMessage(BufferedReader reader) {
        String inputMessage = "";
        try {
            String inputLine;
            while (!(inputLine = reader.readLine()).equals("")) {
                inputMessage += inputLine;
            }
        }
        catch (IOException e) {
            Log.w(TAG, "IOException ", e);
        }
        catch (NullPointerException e) {
            Log.w(TAG, "NullPointerException reading input ", e);
        }
        return inputMessage;
    }

    private void writeMessage(BufferedWriter writer, String message) {
        String inputMessage = "";
        try {
            writer.write(message);
            writer.flush();
        }
        catch (IOException e) {
            Log.w(TAG, "IOException ", e);
        }
    }

    private void toggleRelay1() {
        try {
            relay1.setValue(!relay1.getValue());

            Log.i(TAG, "Toggled relay 1 ...");
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO ", e);
        }
    }

    private void toggleRelay2() {
        try {
            relay2.setValue(!relay2.getValue());

            Log.i(TAG, "Toggled relay 2 ...");
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }
    }

    public void shutdownServer() {
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
