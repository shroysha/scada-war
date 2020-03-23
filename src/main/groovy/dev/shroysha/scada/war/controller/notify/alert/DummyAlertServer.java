package dev.shroysha.scada.war.controller.notify.alert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DummyAlertServer {

    public static void main(String[] args) {

        System.out.println("YO");

        // ALERT
        new Thread(
                () -> {
                    try {
                        ServerSocket ss = new ServerSocket(7655);
                        Socket socket = ss.accept();
                        OutputStream os = socket.getOutputStream();
                        InputStream is = socket.getInputStream();
                        Scanner scanner = new Scanner(System.in);
                        while (true) {
                            String line = scanner.nextLine();
                            os.write(line.getBytes());
                            System.out.println(readBuffer(is));
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DummyAlertServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                })
                .start();
    /*
            new Thread(new Runnable() {
    //PAGING

                public void run() {
                    try {
                        ServerSocket ss = new ServerSocket(7655);
                        Socket socket = ss.accept();
                        InputStream is = socket.getInputStream();
                        OutputStream os = socket.getOutputStream();
                        os.write("ID=".getBytes());
                        boolean first = true;
                        while(true) {
                            String buffer = readBuffer(is);
                            System.out.println(buffer);
                            if(!first)
                                os.write("OK".getBytes());
                            first = false;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DummyAlertServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();*/
    }

    private static String readBuffer(InputStream is) throws IOException {
        StringBuilder buffer = new StringBuilder();
        do {
            int read = is.read();
            if (read == -1) {
                throw new IOException("The connection was broken");
            }
            buffer.append((char) read);

        } while (is.available() > 0);
        return buffer.toString();
    }
}
