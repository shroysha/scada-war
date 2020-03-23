package dev.shroysha.scada.war.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public abstract class ScadaUtilities {

    public static String getMainDirPath() {
        return System.getProperty("user.home") + "/.scada/";
    }

    public static String[] getDaysOfWeek() {
        return new String[]{
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        };
    }

    public static String timeFormat(double time) {
        int hours = (int) time;
        time -= hours;
        int minutes = (int) (time * 60.0);
        NumberFormat format = new DecimalFormat("00");
        String hoursText = format.format(hours);
        String minutesText = format.format(minutes);
        return "" + hoursText + ":" + minutesText;
    }
}
