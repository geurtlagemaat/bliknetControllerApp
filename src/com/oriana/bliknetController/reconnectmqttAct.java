package com.oriana.bliknetController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by geurt on 3-2-2016.
 */
public class reconnectmqttAct extends Activity {
    private BliknetApp applBliknet = null;

    // reconnectmqtt_act
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reconnectmqtt_act);
        setTitle("Connection Lost");
        applBliknet = (BliknetApp) getApplicationContext();

        Intent intent = getIntent();

        TextView txtLostDateTime = (TextView) findViewById(R.id.txtLostDateTime);
        txtLostDateTime.setText(intent.getStringExtra("lostdatetime"));
    }

    public void btnReconnectMQTTServiceOnClick(View view){
        stopService(new Intent(getBaseContext(), MQTTService.class));
        Intent myService = new Intent(getBaseContext(), MQTTService.class);
        myService.putExtra("explicitStart", true);
        startService(myService);
    }



}
