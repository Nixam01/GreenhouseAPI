

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.StringTokenizer;

/**
 * Klasa reprezentująca klienta
 * Autor: Marcin Latawiec
 */
public class Client {
    MulticastSocket ms;
    boolean loggedin;
    private Socket socketTCP;
    private Socket socketUDP;
    private BufferedReader input;
    private PrintWriter output;

    public Client() throws IOException {
    }
    public Client(String host, int portTCP) throws Exception {
        ms = new MulticastSocket(10000);
        boolean loggedin = false;
        Socket socketTCP = new Socket(host, portTCP);
        input = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
        output = new PrintWriter(socketTCP.getOutputStream(), true);
        String groupAddress = "228.222.222.222";
        InetSocketAddress group = new InetSocketAddress(groupAddress, 10000);
        ms.joinGroup(group, NetworkInterface.getByName("localhost"));
    }

    public static void main(String[] args) throws Exception {
        Client cl = new Client("localhost", 9001);
    }

    public String getData() {
        byte[] b = new byte[200];
        DatagramPacket in = new DatagramPacket(b, b.length);
        try {
            ms.receive(in);
        } catch (IOException ignored) {
        }
        return new String(in.getData(), 0, in.getLength());
    }

    public boolean login() {
        send(GreenhouseProtocol.LOGIN);
        return loggedin = true;
    }

    public boolean logout() {
        send(GreenhouseProtocol.LOGOUT);
        return loggedin = false;
    }

    public int getNumberOfConnectedClients() {
        send(GreenhouseProtocol.GETNUMBEROFCLIENTS);
        try {
            input.readLine();
            return Integer.parseInt(input.readLine());
        } catch (IOException e) {
            return 0;
        }
    }

    public void send(String command) {
        if (output != null)
            output.println(command);
    }

    String receive() throws IOException {
        return input.readLine();
    }

    public boolean handleCommand(String protocolSentence) {
        StringTokenizer st = new StringTokenizer(protocolSentence);
        String command = st.nextToken();
        switch (command) {
            case GreenhouseProtocol.HEATING:
                send(GreenhouseProtocol.HEATING);
                break;
            case GreenhouseProtocol.LIGHT:
                send(GreenhouseProtocol.LIGHT);
                break;
            case GreenhouseProtocol.WATERING:
                send(GreenhouseProtocol.WATERING);
                break;
            case GreenhouseProtocol.GETNUMBEROFCLIENTS:
                send(GreenhouseProtocol.GETNUMBEROFCLIENTS);
                break;
            case GreenhouseProtocol.LOGOUT:
                return false;
        }
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        logout();
    }
}
