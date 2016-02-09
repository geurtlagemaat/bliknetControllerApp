package com.oriana.bliknetController;

/**
 * Created by geurt on 30-1-2016.
 */
public class AlertSettings {
    private Integer ID;
    private String Label = null;
    private String Topic = null;
    private short AlertType = 1;
    private Boolean Armed = Boolean.FALSE;
    private Integer AlertPauzeFrom;
    private Integer AlertPauzeTo;
    private String LastEvent = null;

    public AlertSettings(Integer ID, String Label, String Topic, short AlertType, Boolean Armed, Integer AlertPauzeFrom,
                         Integer AlertPauzeTo, String LastEvent){
        this.ID = ID;
        this.Label = Label;
        this.Topic = Topic;
        this.AlertType = AlertType;
        this.Armed = Armed;
        this.AlertPauzeFrom = AlertPauzeFrom;
        this.AlertPauzeTo = AlertPauzeTo;
        this.LastEvent = LastEvent;
    }

    public AlertSettings(Integer ID, String Label, String Topic, short AlertType){
        this.ID = ID;
        this.Label = Label;
        this.Topic = Topic;
        this.AlertType = AlertType;
    }

    public Integer getID(){return ID;}

    public String getAlertLabel(){return Label;}
    public String getAlertTopic(){return Topic;}
    public Short getAlertType(){return AlertType;}
    public Boolean getAlertArmed(){return Armed;}
    public void setAlertArmed(Boolean Armed){this.Armed = Armed;}
    public Integer getAlertPauzeFrom(){return AlertPauzeFrom;}
    public void setAlertPauzeFrom(Integer AlertPauzeFrom){this.AlertPauzeFrom = AlertPauzeFrom;}
    public Integer getAlertPauzeTo(){return  AlertPauzeTo;}
    public void setAlertPauzeTo(Integer AlertPauzeTo){this.AlertPauzeTo = AlertPauzeTo;}
    public String getLastEvent(){return LastEvent;}
}
