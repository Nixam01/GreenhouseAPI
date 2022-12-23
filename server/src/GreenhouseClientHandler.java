import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Klasa reprezentująca instancje klienta po stronie serwera
 * Autor: Filip Perz
 */
public class GreenhouseClientHandler implements Runnable {
    private final Server server;
    private Socket clientSocket;
    private GreenhouseSimulator simulator;
    private BufferedReader input;
    private PrintWriter output;

    public GreenhouseClientHandler(Socket clientSocket, GreenhouseSimulator simulator, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.simulator = simulator;
    }

    public void init() throws IOException {
        Reader reader = new InputStreamReader(clientSocket.getInputStream());
        output = new PrintWriter(clientSocket.getOutputStream(), true);
        input = new BufferedReader(reader);
    }

    public void close() {
        try {
            output.close();
            input.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing client: " + e);
        } finally {
            output = null;
            input = null;
            clientSocket = null;
        }
    }


    @Override
    public void run() {
        while (true) {
            String received = receive();
            StringTokenizer tokens = new StringTokenizer(received);
            String cmd = tokens.nextToken();
            switch (cmd) {
                case GreenhouseProtocol.LOGIN:
                    send(GreenhouseProtocol.LOGGEDIN);
                    break;
                case GreenhouseProtocol.HEATING:
                case GreenhouseProtocol.WATERING:
                case GreenhouseProtocol.LIGHT:
                    simulator.toggleFunction(cmd);
                    break;
                case GreenhouseProtocol.GETNUMBEROFCLIENTS:
                    send(Integer.toString(server.getNumberOfClients()));
                    break;
                case GreenhouseProtocol.LOGOUT:
                    send(GreenhouseProtocol.LOGOUT);
                case GreenhouseProtocol.STOPPED:
                    server.removeClient(this);
                case GreenhouseProtocol.DEFAULTCOMMAND:
                    return;
            }
        }
    }

    private String receive() {
        try {
            String s = input.readLine();
            System.out.println(s);
            return s;
        } catch (IOException e) {
            System.err.println("Error reading data");
        }
        return GreenhouseProtocol.DEFAULTCOMMAND;
    }

    void send(String cmd) {
        output.println(cmd);
    }

    public InetAddress getInetAddress() {
        return clientSocket.getInetAddress();
    }
}
