package com.oriana.bliknetController;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by geurt on 1-2-2016.
 */
public class MQTTService extends Service {
    private static final String TAG = "MQTTService";
    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    private static boolean explicitStart = false;
    private short restartCount = 0;
    private Boolean isStarting = false;
    private Thread thread;
    private ConnectivityManager mConnMan;

    private volatile List<AlertSettings> lstAlerts;
    private volatile IMqttAsyncClient mqttClient;
    private String deviceId;
    private BliknetApp applBliknet = null;

    class MQTTBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            IMqttToken token;
            boolean hasConnectivity = false;
            boolean hasChanged = false;
            NetworkInfo infos[] = mConnMan.getAllNetworkInfo();

            for (int i = 0; i < infos.length; i++) {
                if (infos[i].getTypeName().equalsIgnoreCase("MOBILE")) {
                    if ((infos[i].isConnected() != hasMmobile)) {
                        hasChanged = true;
                        hasMmobile = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                } else if (infos[i].getTypeName().equalsIgnoreCase("WIFI")) {
                    if ((infos[i].isConnected() != hasWifi)) {
                        hasChanged = true;
                        hasWifi = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                }
            }

            hasConnectivity = hasMmobile || hasWifi;
            Log.v(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - " + (mqttClient == null || !mqttClient.isConnected()));
            if (hasConnectivity && (hasChanged || explicitStart) && (mqttClient == null || !mqttClient.isConnected())) {
                doConnect();
            } else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
                Log.d(TAG, "doDisconnect()");
                try {
                    token = mqttClient.disconnect();
                    token.waitForCompletion(1000);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        public class MQTTBinder extends Binder {
            public MQTTService getService() {
                return MQTTService.this;
            }
        }
    }

    @Override
    public void onCreate() {
        IntentFilter intentf = new IntentFilter();

        applBliknet = (BliknetApp) getApplicationContext();
        deviceId = applBliknet.getMQTTClientID()+"-S";
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new MQTTBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        android.os.Debug.waitForDebugger();
        super.onConfigurationChanged(newConfig);
    }

    /* private void setClientID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        deviceId = wInfo.getMacAddress();
        if (deviceId == null) {
            deviceId = MqttAsyncClient.generateClientId();
        }
    } */

    private void doConnect() {
        if (!isStarting) {
            this.isStarting = true;
            Log.d(TAG, "doConnect()");
            Toast.makeText(this, "Service DoConnect", Toast.LENGTH_LONG).show();
            IMqttToken token;
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            // options.setConnectionTimeout(30);
            // options.setKeepAliveInterval(60); // default heartbeat
             try {
                String uri = null;
                try {
                    uri = applBliknet.getAppPersistence().getMQTTSettingStr(applBliknet.getAppPersistence().COLUMN_MQTTSET_HOST) + ":" +
                            applBliknet.getAppPersistence().getMQTTSettingInt(applBliknet.getAppPersistence().COLUMN_MQTTSET_PORT);
                    mqttClient = new MqttAsyncClient(uri, deviceId, new MemoryPersistence());
                    token = mqttClient.connect(options);
                    Toast.makeText(this, "Service connected", Toast.LENGTH_LONG).show();

                    token.waitForCompletion(3500);
                    mqttClient.setCallback(new MqttEventCallback());
                    lstAlerts = applBliknet.getAppPersistence().getAlertSettings();
                    for (int i = 0; i < lstAlerts.size(); i++) {
                        if (lstAlerts.get(i).getAlertArmed()) {
                            token = mqttClient.subscribe(lstAlerts.get(i).getAlertTopic(), 2);
                            token.waitForCompletion(5000);
                            Toast.makeText(this, "Subscribed topic: " + lstAlerts.get(i).getAlertTopic(), Toast.LENGTH_LONG).show();
                        }
                    }
                    applBliknet.setMqttServiceActive(Boolean.TRUE);
                    restartCount = 0;
                    isStarting = false;
                } catch (IOException ioe) {
                    throw new Error("Unable to access database");
                }
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                switch (e.getReasonCode()) {
                    case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                    case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                    case MqttException.REASON_CODE_CONNECTION_LOST:
                    case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                        Log.v(TAG, "c" + e.getMessage());
                        e.printStackTrace();
                        break;
                    case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                        // Intent i = new Intent("RAISEALARM");
                        // i.putExtra("ALARM", e);
                        Log.e(TAG, "b" + e.getMessage());
                        break;
                    default:
                        Log.e(TAG, "a" + e.getMessage());
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand()");
        this.lstAlerts = null;
        this.explicitStart = intent.getBooleanExtra("explicitStart", false);
        if ((mqttClient != null) && (mqttClient.isConnected())) {
            try {
                applBliknet.setMqttServiceActive(false);
                mqttClient.disconnectForcibly(); // IMqttToken myToken = mqttClient.disconnect();
                // myToken.waitForCompletion(5000);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (mqttClient!=null) {
                mqttClient.disconnectForcibly();
                mqttClient = null;
            }
            lstAlerts = null;
            applBliknet.setMqttServiceActive(Boolean.FALSE);
            Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private class MqttEventCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable arg0) {
            applBliknet.setMqttServiceActive(Boolean.FALSE);
            explicitStart = true;
            if (restartCount < 4) {
                doConnect();
            } else {
                doNotify("system/mqttservicelost", applBliknet.now());
            }
            restartCount = +1;
            // applBliknet.setMqttServiceActive(Boolean.FALSE);
            // doNotify("SYSTEM", "SERVICE CONNECTION LOST at: "+ now());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        @SuppressLint("NewApi")
        public void messageArrived(final String topic, final MqttMessage msg) throws Exception {
            Log.i(TAG, "Message arrived from topic" + topic);
            Handler h = new Handler(getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    /* Intent launchA = new Intent(MQTTService.this, mqttserviceAct.class);
                    launchA.putExtra("message", msg.getPayload());

                    if(Build.VERSION.SDK_INT >= 11){
                        launchA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    }else {
        		        launchA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		    }
                    startActivity(launchA); */
                    // Toast.makeText(getApplicationContext(), "MQTT Message:\n" + new String(msg.getPayload()), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Topic: " + topic + ", message:\n" + new String(msg.getPayload()), Toast.LENGTH_SHORT).show();
                    doNotify(topic, new String(msg.getPayload()));
                }
            });
        }
    }

    public String getThread() {
        return Long.valueOf(thread.getId()).toString();
    }

    private void doNotify(String topic, String Message) {
        /* Intent launchA = new Intent(MQTTService.this, mqttserviceAct.class);
                    launchA.putExtra("message", msg.getPayload()); */
        Notification noti = null;
        if (topic.equalsIgnoreCase("system/mqttservicelost")){
            Intent intent = new Intent(this, mqttserviceAct.class);
            intent.putExtra("lostdatetime", Message);
            PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            noti = new Notification.Builder(this)
                    .setContentTitle("BliknetController")
                    .setContentText("MQTT conn. lost: "+Message).setSmallIcon(R.drawable.blik)
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.ic_cached_black_48dp, "Reconnect", pIntent).build();
            noti.defaults |= Notification.DEFAULT_SOUND;
            noti.defaults |= Notification.DEFAULT_VIBRATE;
        }
        else {
            if ( (topic.equalsIgnoreCase("buitencams/cam1lastevent")) || (topic.equalsIgnoreCase("buitencams/cam2lastevent")) ){
                Intent intent = new Intent(this, cameraAct.class);

                PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
                String sNotifyMsg = null;
                if (topic.equalsIgnoreCase("buitencams/cam1lastevent")){
                    sNotifyMsg = "Camera voor";
                } else {
                    sNotifyMsg = "Camera achter";
                }
                intent.putExtra("cam", sNotifyMsg);

                noti = new Notification.Builder(this)
                    .setContentTitle("BliknetController")
                    .setContentText(sNotifyMsg).setSmallIcon(R.drawable.blik)
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.ic_visibility_black_48dp, "Camera", pIntent).build();
            }
            else {
                Intent intent = new Intent(this, mqttserviceAct.class);
                intent.putExtra("message", Message);
                intent.putExtra("topic", topic);
                PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
                String sNotifyMsg = topic + ": " + Message;

                noti = new Notification.Builder(this)
                    .setContentTitle("BliknetController")
                    .setContentText(sNotifyMsg).setSmallIcon(R.drawable.blik)
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.ic_launcher, "Show", pIntent).build();
            }
        }
        noti.defaults |= Notification.DEFAULT_SOUND;
        noti.defaults |= Notification.DEFAULT_VIBRATE;
        //
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;  // hide the notification after its selected
        notificationManager.notify(0, noti);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");
        return null;
    }

    /* public static String now() {
        final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    } */

}
