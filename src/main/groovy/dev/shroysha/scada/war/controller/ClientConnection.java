package dev.shroysha.scada.war.controller;

import dev.shroysha.scada.ejb.ScadaSite;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientConnection {

    private final Socket socket;
    private final String ip;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int connectionAttempts;

    public ClientConnection(Socket aSocket) throws IOException {
        socket = aSocket;
        ip = socket.getInetAddress().getHostAddress();
        socket.setSoTimeout(5000);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        connectionAttempts = 0;
    }

    public String readString() throws IOException, ClassNotFoundException {
        Object temp = in.readObject();
        if (temp instanceof String) {
            return (String) temp;
        } else {
            return "Error in Stream, not a String.";
        }
    }

    public void connectionProblem() {
        connectionAttempts++;
    }

    public boolean connectionDown() {
        return connectionAttempts > 10;
    }

    public boolean shutDownConnection() {
        try {
            in.close();
            out.close();
            socket.close();
            return true;
        } catch (IOException e) {
            System.out.println("Problem closing socket.");
        }

        return false;
    }

    public void printSite(ScadaSite site) throws IOException {
        out.writeObject(site);
    }

    public void printString(String message) throws IOException {
        out.writeObject(message);
    }

    public void resetOutStream() throws IOException {
        out.reset();
    }

    public boolean isClosed() {
        System.out.println(socket.isClosed());
        return socket.isClosed();
    }

    public String getIP() {
        return ip;
    }

    public Socket getSocket() {
        return socket;
    }
}
