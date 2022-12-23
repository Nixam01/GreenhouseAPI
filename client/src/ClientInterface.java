 /**Program wykonany przez zespół w składzie:
 Filip Perz
 Marcin Latawiec
 Jan Ignasiak
 Anna Satkowska

 Oświadczamy, że praca została wykonana przez nas samodzielnie, bez pomocy innych osób

 Działanie programu:
  - utworzenie nowego klienta
  - logowanie do serwera, dokonuje się ono poprzez podanie adresu ip serwera
    (w przypadku wciśnięcia entera, przypisywana jest wartość domyślna - localhost)
    w terminalu Server powinna pojawić się informacja o dołączeniu nowego klienta
  - wybór dostęnych opcji przez klienta:
    1. sprawdzenie ilu klientów jest podłączonych
    2. wyświetlenie danych z symulatora
    (w przypadku chęci przerwania wyświetlania należy wcisnąć dowolny przycisk)
    3. modyfikacja wybranej danej
    4.  zakończenie połączenia i wyjście z programu
*/

import java.util.Scanner;

// klasa programu
// Anna Satkowska
public class ClientInterface {

    static final Scanner scanner = new Scanner(System.in);
    private Client client;
    private boolean readData;

    public ClientInterface() throws InterruptedException {

        System.out.println("\nWitamy w programie do sterowania szklarnią!\n");

        readData = false;
        //tworzenie klienta
        String choice = "";
        while (!validate(choice, 1, 2)) {
            System.out.println("""
                    Wybierz opcję:
                    [1] Zaloguj się do serwera
                    [2] Wyjdź z programu
                    """);

            choice = scanner.nextLine();
        }

        switch (choice) {
            case "1" -> loginController();
            case "2" -> System.exit(0);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                client.logout();
            }
        }));

        do {

            System.out.println("""
                    Wybierz, co chcesz zrobić:
                    [1] sprawdź ilu klientów jest podłączonych
                    [2] wyświetl dane z symulatora
                    [3] dokonaj modyfikacji wybranej danej
                    [4] zakończ połączenie i wyjdź z programu 
                    """);

            choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    int number = client.getNumberOfConnectedClients();
                    System.out.println("Liczba połączonych użytkowników: " + number);
                }
                case "2" -> {
                    readSimulation();
                }
                case "3" -> simulatorController();
                case "4" -> {
                    client.logout();
                    System.exit(0);
                }
                default -> System.out.println("Wybrana opcja nie istnieje");
            }

        } while (true);
    }

    public static void main(String[] args) throws InterruptedException {
        new ClientInterface();

    }

    private static boolean validate(String option, int bottomRange, int upperRange) {
        try {
            int opt = Integer.parseInt(option);
            if (opt >= bottomRange && opt <= upperRange)
                return true;
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    private void readSimulation() throws InterruptedException {
        readData = true;
        Thread readSim = new Thread(() -> {
            while (getReadData()) {
                System.out.println(client.getData());
            }
        });

        readSim.start();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
            readData = false;
            Thread.sleep(1000);
        }

    }

    synchronized private boolean getReadData() {
        return readData;
    }

    private void simulatorController() {
        System.out.println("""
                Wybierz daną do modyfikacji
                [1] ogrzewanie
                [2] podlewanie
                [3] oświetlenie""");
        String choice2 = scanner.nextLine();
        switch (choice2) {
            case "1" -> client.handleCommand(GreenhouseProtocol.HEATING);
            case "2" -> client.handleCommand(GreenhouseProtocol.WATERING);
            case "3" -> client.handleCommand(GreenhouseProtocol.LIGHT);
            default -> System.out.println("Wybrana opcja nie istnieje");
        }

    }

    private void loginController() {
        System.out.println("Podaj adres ip serwera (wartość domyślna: localhost)");
        String ipAddress = scanner.nextLine();
        if (ipAddress.isEmpty()) {
            try {
                client = new Client("localhost", 9001);
                client.login();
            } catch (Exception exception) {
                System.err.println("Unable to create client");
            }
        }

    }
}
