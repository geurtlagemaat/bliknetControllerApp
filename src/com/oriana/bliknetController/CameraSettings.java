package com.oriana.bliknetController;

/**
 * Created by geurt on 30-1-2016.
 */
public class CameraSettings {

    private String URL = null;
    private String User = null;
    private String PW = null;
    private String Label = null;

    public CameraSettings(String Label, String URL, String User, String PW){
        this.Label = Label;
        this.URL = URL;
        this.User = User;
        this.PW = PW;
    }

    public CameraSettings(String Label, String URL){
        this.Label = Label;
        this.URL = URL;
    }

    public String getCameraLabel(){return Label;}

    public String getCameraURL(){return URL;}

    public String getCameraUser(){return User;}

    public String getCameraPW(){return PW;}
}
