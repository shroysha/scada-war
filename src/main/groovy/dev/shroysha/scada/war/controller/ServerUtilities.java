package dev.shroysha.scada.war.controller;


public abstract class ServerUtilities {

    public static String getBaseDirectory() {
        return System.getProperty("user.home") + "/.scada/";
    }
}
