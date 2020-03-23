package dev.shroysha.scada.war.controller;

import dev.shroysha.scada.war.App;
import dev.shroysha.scada.war.view.ServerGUI;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScadaRunner {

    static final Logger log = Logger.getGlobal();

    static App server;

    static ServerGUI gui;

    public static void main(String[] args) {
        String[] verbose = {"v"};
        dispatch(verbose);

        server = new App();

        gui = new ServerGUI(server);
        gui.setVisible(true);
    }

    static void dispatch(String[] args) {
        for (String s : args) {
            s = s.replaceAll("-", "");
            char command = s.charAt(0);

            switch (command) {
                case 'v':
                    log.setLevel(Level.ALL);
                    try {
                        FileHandler fh = new FileHandler("log.xml");
                        log.addHandler(fh);
                        log.info("Hai");
                    } catch (IOException | SecurityException ex) {
                        Logger.getGlobal().info(ex.toString());
                    }
            }
        }
    }

    public static App getServer() {
        return server;
    }
}
