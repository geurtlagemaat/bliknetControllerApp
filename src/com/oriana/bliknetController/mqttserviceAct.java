package com.oriana.bliknetController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by geurt on 1-2-2016.
 */
public class mqttserviceAct extends Activity {
    private BliknetApp applBliknet = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mqttservice_act);
        setTitle("Notification!");

        Intent intent = getIntent();
        ((TextView) findViewById(R.id.txtTopic)).setText(intent.getStringExtra("topic"));
        ((TextView) findViewById(R.id.txtMessage)).setText(intent.getStringExtra("message"));
    }

}
