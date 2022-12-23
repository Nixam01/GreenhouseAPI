
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca Serwer
 * Autor: Filip Perz
 */
public class Server {
    private final GreenhouseSimulator simulator;
    private final List<GreenhouseClientHandler> clients;
    private ServerSocket tcpServerSocket;
    private MulticastSocket udpServerSocket;
    private InetSocketAddress group;

    public Server(int tcpPort, int udpPort) {
        simulator = new GreenhouseSimulatorImpl();
        clients = new ArrayList<>();

        try {
            tcpServerSocket = new ServerSocket(tcpPort);
            udpServerSocket = new MulticastSocket(udpPort);
            group = new InetSocketAddress("228.222.222.222", udpPort);
            udpServerSocket.joinGroup(group, NetworkInterface.getByName("localhost"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        createTcpServer().start();
        createUdpServer().start();

    }

    public static void main(String[] args) {
        new Server(9001, 10000);
    }

    public Thread createUdpServer() {
        return new Thread(() -> {
            while (true) {
                byte[] b = simulator.getData().getBytes();
                DatagramPacket out = new DatagramPacket(b, b.length, group);
                try {
                    udpServerSocket.send(out);
                    Thread.sleep(simulator.getDelay());
                } catch (InterruptedException | IOException ignored) {
                }

            }
        });
    }

    public Thread createTcpServer() {
        return new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = tcpServerSocket.accept();
                    clientSocket.getInetAddress();
                    addClient(clientSocket);
                } catch (IOException e) {
                    System.out.println("Error connecting client");
                }
            }
        });
    }

    private void addClient(Socket clientSocket) throws IOException {
        GreenhouseClientHandler client = new GreenhouseClientHandler(clientSocket, simulator, this);
        clients.add(client);
        client.init();
        new Thread(client).start();
        System.out.println("New client added, number of clients = " + clients.size());
    }

    synchronized void send(String msg) {
        for (GreenhouseClientHandler c : clients) {
            c.send(msg);
        }
    }


    public void removeClient(GreenhouseClientHandler greenhouseClientHandler) {
        clients.remove(greenhouseClientHandler);
        greenhouseClientHandler.close();
        System.out.println("Client disconnected");
    }

    public int getNumberOfClients() {
        return clients.size();
    }
}



