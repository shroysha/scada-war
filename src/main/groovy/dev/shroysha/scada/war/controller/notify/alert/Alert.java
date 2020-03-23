package dev.shroysha.scada.war.controller.notify.alert;

import java.util.Calendar;


public class Alert {

    private final int jobID;
    private final String message;

    private boolean acknowledged = false;
    private Calendar nextAlertTime;
    private int timesPaged = 0;

    public Alert(int jobID, String message) {
        super();

        this.jobID = jobID;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void acknowledge() {
        acknowledged = true;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }


    public boolean equals(Object o) {
        if (o instanceof Alert) {
            Alert a = (Alert) o;
            return a.jobID == jobID;
        }

        return false;
    }

    public String toString() {
        return "" + message;
    }

    public int getJobID() {
        return jobID;
    }

    void setNextAlertTime(Calendar instance) {
        this.nextAlertTime = instance;
    }

    boolean isReadyToAlert() {
        Calendar now = Calendar.getInstance();
        // if current time is past the time the page was supposed to send
        return nextAlertTime.getTimeInMillis()
                < now.getTimeInMillis();
    }

    public int getTimesPaged() {
        return timesPaged;
    }

    public void incrementTimesPaged() {
        timesPaged++;
    }
}
