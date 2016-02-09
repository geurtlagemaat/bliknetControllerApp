package com.oriana.bliknetController;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.app.ActionBar.LayoutParams;
import android.widget.Switch;
import java.io.IOException;
import java.util.List;

/**
 * Created by geurt on 29-1-2016.
 */
public class setalertsAct extends Activity {
    private List<AlertSettings> lstAlerts;
    private BliknetApp applBliknet = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setalerts_act);
        setTitle("Alert Settings");

        applBliknet = (BliknetApp) getApplicationContext();
        final LinearLayout lm = (LinearLayout) findViewById(R.id.linLayout);

        LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
                );
        try {
            lstAlerts = applBliknet.getAppPersistence().getAlertSettings();
            for (int i=0; i < lstAlerts.size(); i++) {
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.HORIZONTAL);

                final Switch swAlert = new Switch(this);
                swAlert.setId(lstAlerts.get(i).getID());
                swAlert.setLayoutParams(paramsLayout);
                swAlert.setText(lstAlerts.get(i).getAlertLabel());
                swAlert.setChecked(lstAlerts.get(i).getAlertArmed());

                ll.addView(swAlert);
                lm.addView(ll);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }  // onCreate

    @Override
    protected void onDestroy(){
        setResult(Activity.RESULT_OK);
        for (int i=0; i < lstAlerts.size(); i++) {
            Switch myArmedSwich = (Switch) findViewById(lstAlerts.get(i).getID());
            lstAlerts.get(i).setAlertArmed(myArmedSwich.isChecked());
        }
        try {
            applBliknet.getAppPersistence().setAlertSettings(lstAlerts);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
