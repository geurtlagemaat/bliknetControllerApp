package com.oriana.bliknetController;

/**
 * Created by geurt on 27-1-2016.
 */
public class MQTTTopic {

    private String Topic;
    private int QOS;
    private Boolean Retain;

    public MQTTTopic(String Topic, int QOS, Boolean Retain){
        this.Topic = Topic;
        this.QOS = QOS;
        this.Retain = Retain;
    }

    public String getTopic(){return Topic;}

    public int getQAS(){return QOS;}

    public Boolean getRetain(){return Retain;}

}

