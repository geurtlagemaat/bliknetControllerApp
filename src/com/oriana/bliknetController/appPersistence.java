/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * <p/>
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package com.oriana.bliknetController;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>Persistence</code> deals with interacting with the database to persist
 * various objects and settings.
 */
public class appPersistence extends SQLiteOpenHelper implements BaseColumns {

    /**
     * The version of the database
     **/
    public static final int DATABASE_VERSION = 1;
    /**
     * The name of the database file
     **/
    private static String DB_PATH = "/data/data/com.oriana.bliknetController/databases/";
    public static final String DATABASE_NAME = "bliknetcontroller.db";

    /** Tables **/
    /**
     * Table MQTTSettings
     **/
    public static final String TABLE_MQTTSettings = "appMQTTSettings";
    public static final String COLUMN_MQTTSET_HOST = "mqttHOST";
    public static final String COLUMN_MQTTSET_PORT = "mqttPORT";
    public static final String COLUMN_MQTTSET_USER = "mqttUSER";
    public static final String COLUMN_MQTTSET_PW = "mqttPW";
    public static final String COLUMN_MQTTSET_TIMEOUT = "mqttTIMEOUT";
    public static final String COLUMN_MQTTSET_CLEANSESSION = "mqttCLEANSESSION";
    /**
     * Table MQTTTopics
     **/
    public static final String TABLE_MQTTTOPICS = "appMQTTTopics";
    public static final String COLUMN_MQTTTOPICS_topic = "topic";
    public static final String COLUMN_MQTTTOPICS_qos = "qos";
    public static final String COLUMN_MQTTTOPICS_retained = "retained";

    /**
     * Table Cameras
     **/
    public static final String TABLE_Camera = "appCameras";
    public static final String COLUMN_CAMERAS_label = "label";
    public static final String COLUMN_CAMERAS_url = "url";
    public static final String COLUMN_CAMERAS_user = "user";
    public static final String COLUMN_CAMERAS_pw = "pw";

    /**
     * Table AlertSettings
     **/
    public static final String TABLE_Alerts = "appAlerts";
    public static final String COLUMN_ALERTS_id = "_id"; // alertID
    public static final String COLUMN_ALERTS_label = "label";
    public static final String COLUMN_ALERTS_topic = "topic";
    public static final String COLUMN_ALERTS_alerttype = "alerttype";
    public static final String COLUMN_ALERTS_armed = "armed";
    public static final String COLUMN_ALERTS_lastevent = "lastevent";
    public static final String COLUMN_ALERTS_alertpauzefrom = "alertpauzefrom";
    public static final String COLUMN_ALERTS_alertpauzeto = "alertpauzeto";

    //last will
    /**
     * Table column for last will topic
     **/
    public static final String COLUMN_TOPIC = "topic";
    /**
     * Table column for the last will message payload
     **/
    public static final String COLUMN_MESSAGE = "message";
    /**
     * Table column for the last will message qos
     **/
    public static final String COLUMN_QOS = "qos";
    /**
     * Table column for the retained state of the message
     **/
    public static final String COLUMN_RETAINED = "retained";

    //sql lite data types
    /**
     * Text type for SQLite
     **/
    private static final String TEXT_TYPE = " TEXT";
    /**
     * Int type for SQLite
     **/
    private static final String INT_TYPE = " INTEGER";
    /**
     * Comma separator
     **/
    private static final String COMMA_SEP = ",";

    /**
     * Create table MQTTSettings
     **/
    private static final String SQL_CREATE_MQTTSettings =
            "CREATE TABLE " + TABLE_MQTTSettings + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_MQTTSET_HOST + TEXT_TYPE + COMMA_SEP +
                    COLUMN_MQTTSET_PORT + INT_TYPE + COMMA_SEP +
                    COLUMN_MQTTSET_USER + TEXT_TYPE + COMMA_SEP +
                    COLUMN_MQTTSET_PW + TEXT_TYPE + COMMA_SEP +
                    COLUMN_MQTTSET_TIMEOUT + INT_TYPE + COMMA_SEP +
                    COLUMN_MQTTSET_CLEANSESSION + INT_TYPE + " );";

    /**
     * Delete table MQTTTOPICS
     **/
    private static final String SQL_DELETE_MQTTSettings =
            "DROP TABLE IF EXISTS " + TABLE_MQTTSettings;

    /**
     * Create table MQTTTOPICS
     **/
    private static final String SQL_CREATE_MQTTTOPICS =
            "CREATE TABLE " + TABLE_MQTTTOPICS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_MQTTTOPICS_topic + TEXT_TYPE + COMMA_SEP +
                    COLUMN_MQTTTOPICS_qos + INT_TYPE + COMMA_SEP +
                    COLUMN_MQTTTOPICS_retained + INT_TYPE + " );";

    /**
     * Delete table MQTTTOPICS
     **/
    private static final String SQL_DELETE_MQTTTOPICS =
            "DROP TABLE IF EXISTS " + TABLE_MQTTTOPICS;

    private final Context myContext;
    private SQLiteDatabase myDataBase;

    /**
     * Creates the persistence object passing it a context
     *
     * @param context Context that the application is running in
     */
    public appPersistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /*
     * (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onDowngrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    public void createDataBase(Boolean AlwaysCopy) throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist && !AlwaysCopy) {
            // vet, niets meer aan doen
        } else {
            // maak er een aan
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database" + e.getMessage());
            }
        }
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);  // OPEN_READONLY
    }

    public String getMQTTSettingStr(String key) throws IOException {
        if (myDataBase != null) {
            String[] connectionColumns = {key};
            Cursor c = myDataBase.query(TABLE_MQTTSettings, connectionColumns, null, null, null, null, COLUMN_MQTTSET_HOST);
            String sResult = null;
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) { //move to the next item throw persistence exception, if it fails
                    throw new Error("Failed read from " + TABLE_MQTTSettings + " count: " + c.getCount() + "loop iteration: " + i);
                }
                sResult = c.getString(c.getColumnIndexOrThrow(key));
            }
            c.close();
            return sResult;
        } else {
            throw new Error("Error database mot open!");
        }
    }

    public Integer getMQTTSettingInt(String key) throws IOException {
        if (myDataBase != null) {
            String[] connectionColumns = {key};
            Cursor c = myDataBase.query(TABLE_MQTTSettings, connectionColumns, null, null, null, null, COLUMN_MQTTSET_HOST);
            int iResult = -1;
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) { //move to the next item throw persistence exception, if it fails
                    throw new Error("Failed read from " + TABLE_MQTTSettings + " count: " + c.getCount() + "loop iteration: " + i);
                }
                iResult = c.getInt(c.getColumnIndexOrThrow(key));
            }
            c.close();
            return iResult;
        } else {
            throw new Error("Error database mot open!");
        }
    }

    public List<MQTTTopic> getMQTTTopics() throws IOException {
        if (myDataBase != null) {
            String[] connectionColumns = {COLUMN_MQTTTOPICS_topic, COLUMN_MQTTTOPICS_qos, COLUMN_MQTTTOPICS_retained};
            Cursor c = myDataBase.query(TABLE_MQTTTOPICS, connectionColumns, null, null, null, null, COLUMN_MQTTTOPICS_topic);
            ArrayList<MQTTTopic> list = new ArrayList<MQTTTopic>(c.getCount());
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) { //move to the next item throw persistence exception, if it fails
                    throw new Error("Failed read from " + TABLE_MQTTTOPICS + " count: " + c.getCount() + "loop iteration: " + i);
                }
                //get data from cursor
                String sTopic = c.getString(c.getColumnIndexOrThrow(COLUMN_MQTTTOPICS_topic));
                int iQOS = c.getInt(c.getColumnIndexOrThrow(COLUMN_MQTTTOPICS_qos));
                Boolean bRetain = c.getInt(c.getColumnIndexOrThrow(COLUMN_MQTTTOPICS_retained)) == 1 ? true : false;
                MQTTTopic myMQTTTopic = new MQTTTopic(sTopic, iQOS, bRetain);
                list.add(myMQTTTopic);
            }
            c.close();
            return list;
        } else {
            throw new Error("Error database not open!");
        }
    }

    public CameraSettings getCameraSettings(String Label) {
        if (myDataBase != null) {
            String[] connectionColumns = {COLUMN_CAMERAS_label, COLUMN_CAMERAS_url, COLUMN_CAMERAS_user, COLUMN_CAMERAS_pw};
            Cursor c = myDataBase.query(TABLE_Camera, connectionColumns, COLUMN_CAMERAS_label + "='" + Label + "'", null, null, null, COLUMN_CAMERAS_label);
            CameraSettings myCamSettings = null;
            try {
                for (int i = 0; i < c.getCount(); i++) {
                    if (!c.moveToNext()) { //move to the next item throw persistence exception, if it fails
                        throw new Error("Failed read from " + TABLE_MQTTTOPICS + " count: " + c.getCount() + "loop iteration: " + i);
                    }
                    try {
                        myCamSettings = new CameraSettings(
                                c.getString(c.getColumnIndexOrThrow(COLUMN_CAMERAS_label)),
                                c.getString(c.getColumnIndexOrThrow(COLUMN_CAMERAS_url)),
                                c.getString(c.getColumnIndexOrThrow(COLUMN_CAMERAS_user)),
                                c.getString(c.getColumnIndexOrThrow(COLUMN_CAMERAS_pw))
                        );
                    } catch (Exception e) {
                        throw new Error("Error reading camera settings with label: " + Label + ". Error: " + e.getMessage());
                    }
                }
            } finally {
                c.close();
            }
            return myCamSettings;
        } else {
            throw new Error("Error database not open!");
        }
    }

    public List<AlertSettings> getAlertSettings() throws IOException {
        if (myDataBase != null) {
            String[] connectionColumns = {COLUMN_ALERTS_id, COLUMN_ALERTS_label, COLUMN_ALERTS_topic, COLUMN_ALERTS_alerttype, COLUMN_ALERTS_armed,
                                          COLUMN_ALERTS_lastevent, COLUMN_ALERTS_alertpauzefrom, COLUMN_ALERTS_alertpauzeto};
            Cursor c = myDataBase.query(TABLE_Alerts, connectionColumns, null, null, null, null, COLUMN_ALERTS_label);
            ArrayList<AlertSettings> list = new ArrayList<AlertSettings>(c.getCount());
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) {
                    throw new Error("Failed read from " + TABLE_Alerts + " count: " + c.getCount() + "loop iteration: " + i);
                }
                //get data from cursor
                String sTopic = c.getString(c.getColumnIndexOrThrow(COLUMN_ALERTS_topic));
                AlertSettings myAlertSettings = new AlertSettings(
                        c.getInt(c.getColumnIndexOrThrow(COLUMN_ALERTS_id)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_ALERTS_label)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_ALERTS_topic)),
                        c.getShort(c.getColumnIndexOrThrow(COLUMN_ALERTS_alerttype)),
                        c.getInt(c.getColumnIndexOrThrow(COLUMN_ALERTS_armed)) == 1 ? true : false,
                        c.getInt(c.getColumnIndexOrThrow(COLUMN_ALERTS_alertpauzefrom)),
                        c.getInt(c.getColumnIndexOrThrow(COLUMN_ALERTS_alertpauzeto)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_ALERTS_lastevent))
                );
                list.add(myAlertSettings);
            }
            c.close();
            return list;
        } else {
            throw new Error("Error database not open!");
        }
    }

    public void setAlertSettings(List<AlertSettings> lstAlertSettings) throws IOException {
        if (myDataBase != null) {
            for (int i=0; i < lstAlertSettings.size(); i++){
                AlertSettings mySettings = lstAlertSettings.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_ALERTS_armed, mySettings.getAlertArmed()); // (column name, new row value)
                contentValues.put(COLUMN_ALERTS_alertpauzefrom, mySettings.getAlertPauzeFrom());
                contentValues.put(COLUMN_ALERTS_alertpauzeto, mySettings.getAlertPauzeTo());
                String selection = COLUMN_ALERTS_id + " LIKE ?"; //  + lstAlertSettings.get(i).getID(); // where ID column = rowId (that is, selectionArgs)
                String[] selectionArgs = { Integer.toString(mySettings.getID()) };  // String.valueOf(rowId)

                long id = myDataBase.update(TABLE_Alerts, contentValues, selection, selectionArgs);
            }
            // myDataBase.close();
            // openDataBase();
            Log.e(this.getClass().getCanonicalName(), "Alertsettings update done!");
        } else {
            throw new Error("Error database not open!");
        }
    }

    // general utilitys
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException {
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /* SQLiteDatabase db = getWritableDatabase();
    SQLiteDatabase db = getReadableDatabase();
    db.execSQL(SQL_CREATE_ENTRIES);
    String username = c.getString(c.getColumnIndexOrThrow(COLUMN_USER_NAME));
    boolean cleanSession = c.getInt(c.getColumnIndexOrThrow(COLUMN_CLEAN_SESSION)) == 1 ? true : false;

    long newRowId = db.insert(TABLE_CONNECTIONS, null, values);

    db.close(); //close the db then deal with the result of the query

    Cursor c = db.query(TABLE_CONNECTIONS, connectionColumns, null, null, null, null, sort);
    ArrayList<Connection> list = new ArrayList<Connection>(c.getCount());
    for (int i = 0; i < c.getCount(); i++) { */
}
