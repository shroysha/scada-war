package dev.shroysha.scada.war.controller.notify.modem;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModemConnector {

    static final Logger log = Logger.getGlobal();
    final String setDefaults = "ATZ\r";
    final String voiceCommand = "AT+FCLASS=8\r";
    final String voiceInit = "ATVIP\r";
    final String hangup = "ATH0\r";
    private final Stack<ReadListener> readListeners = new Stack<>();
    String ip;
    Socket modem;
    OutputStreamWriter out;
    InputStreamReader in;
    String lastPin = "";
    boolean readyForRead;
    Thread listenThread;
    String port;

    public ModemConnector(String aIp, String aPort) throws IOException {
        ip = aIp;
        port = aPort;
        modem = new Socket(ip, Integer.parseInt(port));
        // Socket socket2 = new Socket();
        // socket2.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 5000);
        out = new OutputStreamWriter(modem.getOutputStream());
        in = new InputStreamReader(modem.getInputStream());
    }

    public void start() {
        listenThread = new Thread(new ModemListener());
        listenThread.start();

        if (init() == 1) {
            System.out.println("Modem Ready.");
        } else {
            System.out.println("Uh oh.");
        }
    }

    public int init() {
        try {
            out.write(hangup, 0, hangup.length());
            out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            out.write(setDefaults, 0, setDefaults.length());
            out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            out.write(voiceCommand, 0, voiceCommand.length());
            out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            out.write(voiceInit, 0, voiceInit.length());
            out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return 1;
        } catch (IOException ex) {
            return -2;
        }
    }

    public synchronized boolean hasRead() {
        return readyForRead;
    }

    public String read() {
        if (readyForRead) {
            String temp = lastPin;
            lastPin = "";
            readyForRead = false;
            return temp;
        } else {
            return null;
        }
    }

    public void notifyAllAckListeners(String pin) {
        for (ReadListener listener : readListeners) {
            listener.onRead(pin);
        }
    }

    public void addReadListener(ReadListener listener) {
        readListeners.add(listener);
    }

    public void removeReadListener(ReadListener listener) {
        readListeners.remove(listener);
    }

    private class ModemListener implements Runnable {


        public void run() {
            StringBuilder message = new StringBuilder();
            boolean ring = false;
            boolean zero = false;

            StopThread stopThread = null;

            //noinspection InfiniteLoopStatement
            while (true) {
                try {

                    char inc = (char) in.read();

                    if (inc == 'R') {
                        message = new StringBuilder();
                        ring = true;
                        stopThread = new StopThread();
                        stopThread.start();
                    }

                    if (ring && !zero) {
                        if (inc == '0') {
                            zero = true;
                        }
                    }

                    if (ring && zero) {
                        if (inc > '0' && inc <= '9') {
                            message.append(inc);
                        }
                        if (inc == '#' || inc == '*') {
                            lastPin = message.toString();
                            ring = false;
                            zero = false;
                            readyForRead = true;
                            stopThread.interrupt();
                            init();
                            log.log(Level.WARNING, "Hang up." + lastPin + readyForRead);

                            message = new StringBuilder();
                            if (lastPin.length() == 8) {
                                for (int i = 0; i < lastPin.length(); i += 2) { // take every other char
                                    message.append(lastPin.charAt(i));
                                }

                                lastPin = message.toString();
                            }

                            notifyAllAckListeners(lastPin);
                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class StopThread extends Thread {

        private static final int TWO_MINUTES = 2 * 60 * 1000;

        public StopThread() {
            super();
        }

        public void run() {
            try {
                Thread.sleep(TWO_MINUTES);

                listenThread.interrupt();

                init();

                listenThread = new Thread(new ModemListener());
                listenThread.start();
            } catch (InterruptedException ex) {
                Logger.getLogger(ModemConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
