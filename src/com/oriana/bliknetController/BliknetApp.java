package com.oriana.bliknetController;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by geurt on 30-1-2016.
 */
public class BliknetApp extends Application {
    private appPersistence appPersistence = null;
    private Boolean MqttServiceActive = false;

    public void setAppPersistence(appPersistence appPers){this.appPersistence = appPers;}
    public appPersistence getAppPersistence(){ return this.appPersistence;}

    public void setMqttServiceActive(Boolean State){this.MqttServiceActive = State;}
    public Boolean getMqttServiceActive(){return this.MqttServiceActive;}

    public String now() {
        final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public String getMQTTClientID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String sDeviceId = wInfo.getMacAddress();
        if (sDeviceId == null) {
            sDeviceId = MqttAsyncClient.generateClientId();
        }
        return sDeviceId;
    }
}
