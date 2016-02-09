package com.oriana.bliknetController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.Switch;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by geurt on 25-1-2016.
 */
public class mainAct extends Activity {
    // private appPersistence persObject = null;
    static final int SET_ALERTS_REQUEST = 1;
    private BliknetApp applBliknet = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_act);

        // TODO error handling, opsplitsen
        try {
            appPersistence persObject = null;
            persObject = new appPersistence(this);
            persObject.createDataBase(false);
            persObject.openDataBase();
            applBliknet = (BliknetApp) getApplicationContext();
            applBliknet.setAppPersistence(persObject);
        } catch (IOException ioe) {
            throw new Error("Unable to access database");
        }

        String clientId = applBliknet.getMQTTClientID()+"-A";// UUID.randomUUID().toString();
        String uri = null;
        try {
            uri = applBliknet.getAppPersistence().getMQTTSettingStr(applBliknet.getAppPersistence().COLUMN_MQTTSET_HOST) + ":" +
                    applBliknet.getAppPersistence().getMQTTSettingInt(applBliknet.getAppPersistence().COLUMN_MQTTSET_PORT);
        } catch (IOException ioe) {
            throw new Error("Unable to access database");
        }

        // TODO wat doet die persistance, moet het een file based worden?
        MemoryPersistence memPer = new MemoryPersistence();
        final MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), uri, clientId, memPer);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                /* TextView txtTest = (TextView) findViewById(R.id.txtTest);
                txtTest.setText("connection lost: " + cause.getMessage()); */
                // Toast.makeText(this, "Service connected", Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "connectionLost: " + cause.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equalsIgnoreCase("smartmeter/power")) { ((EditText) findViewById(R.id.edtCurrentPower)).setText(new String(message.getPayload()));}
                if (topic.equalsIgnoreCase("pvdata/power")) {((EditText) findViewById(R.id.edtCurrentPowerGen)).setText(new String(message.getPayload()));}
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //empty
            }
        });

        /*MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(true);
            conOpt.setConnectionTimeout(60);
            conOpt.setKeepAliveInterval(60); */

        try {
            // TODO naar voorbeeld van MQTTService ASync client maken
            IMqttToken token = client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken mqttToken) {
                    try {
                        List<MQTTTopic> lstMQTTTopics = applBliknet.getAppPersistence().getMQTTTopics();
                        for (int i = 0; i < lstMQTTTopics.size(); i++) {
                            client.subscribe(lstMQTTTopics.get(i).getTopic(), lstMQTTTopics.get(i).getQAS());
                        }
                    } catch (IOException | MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    // TODO Auto-generated method stub
                    Log.e(this.getClass().getCanonicalName(), "Client connection failed: " + arg1.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Switch swService = (Switch) findViewById(R.id.swService);
        swService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Intent myService = new Intent(getBaseContext(), MQTTService.class);
                    myService.putExtra("explicitStart", true);
                    startService(myService);
                }else{
                    stopService(new Intent(getBaseContext(), MQTTService.class));
                    // not event from MQTTService class
                    applBliknet.setMqttServiceActive(false);
                }
            }
        });
        doUpdateMQTTService();
    }

    @Override
    public void onResume(){
        super.onResume();
        doUpdateMQTTService();
    }

    private void doUpdateMQTTService(){
        Switch swService = (Switch) findViewById(R.id.swService);
        swService.setChecked(applBliknet.getMqttServiceActive());
    }

    public void btnViewCameraVoorOnClick(View view){
        Intent intent = new Intent(this, cameraAct.class);
        intent.putExtra("cam", "Camera voor");
        this.startActivity(intent);
    }

    public void btnViewCameraAchterOnClick(View view){
        Intent intent = new Intent(this, cameraAct.class);
        intent.putExtra("cam", "Camera achter");
        this.startActivity(intent);
    }

    public void btnSetAlerts(View view){
        Intent intent = new Intent(this, setalertsAct.class);
        // this.startActivity(intent);
        // Toast.makeText(getBaseContext(), "after Set Alerts.", Toast.LENGTH_LONG).show();
        // startActivityForResults(myIntent, MY_REQUEST_CODE);

        this.startActivityForResult(intent, SET_ALERTS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* returning from activity's called from this main activity
           Check which request we're responding to */
        if (requestCode == SET_ALERTS_REQUEST) {
            if (applBliknet.getMqttServiceActive()) {  //  ( (resultCode == Activity.RESULT_OK) &&
                stopService(new Intent(getBaseContext(), MQTTService.class));
                doUpdateMQTTService();
                //
                Intent myService = new Intent(getBaseContext(), MQTTService.class);
                myService.putExtra("explicitStart", true);
                startService(myService);
                doUpdateMQTTService();
            }
        }
    }

    public void btnSetPrefences(View view){

    }

    /* public void btnSetServiceOnTouch(MotionEvent ev){
        Switch swService = (Switch) findViewById(R.id.swService);
        swService.performClick();
    } */

    /* public void btnSetServiceOnClick(View view){
        Switch swService = (Switch) findViewById(R.id.swService);
        if (swService.isChecked()){
            startService(new Intent(getBaseContext(), MQTTService.class));
        }
        else{
            stopService(new Intent(getBaseContext(), MQTTService.class));
        }
    } */
    /* public void btnTextOnClick(View view) {
        EditText edtText = (EditText) findViewById(R.id.edtText);
        String sMsg = edtText.getText().toString();
        TextView txtTest = (TextView) findViewById(R.id.txtTest);
        txtTest.setText(sMsg);
    } */
}