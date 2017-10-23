package net.iqbusiness.iotbeacon;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private EddystoneAdvertiser advertiser;
    private ControlServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        advertiser = new EddystoneAdvertiser(getApplicationContext());
        server = new ControlServer();

        Thread thread = new Thread(server);
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        server.shutdownServer();
    }
}