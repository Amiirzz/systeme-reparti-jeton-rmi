package client;

import intermediaire.AnnuaireServices;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Random;

/**
 * Client participant a l'anneau a jeton. Chaque client possede sa propre
 * chaine de demandes de service et transmet le jeton au client suivant
 * une fois sa demande envoyee.
 */
public class Client {

    private static final int CLIENT_COUNT = 6;
    private static final int BASE_CLIENT_PORT = 1000;
    private static final int REGISTRY_PORT = 1000;
    private static final int TOKEN_WAIT_TIMEOUT_MS = 60000;
    private static final int TOKEN_RETRY_STEP_MS = 3000;

    private static final String[][] SERVICE_CHAINS = {
        {"S7", "S10", "S1", "S2", "S4", "S3", "S0", "S6", "S12", "S11", "S8", "S5", "S2", "S1", "S0", "S5", "S1", "S7", "S9", "S2"},
        {"S15", "S1", "S4", "S2", "S3", "S6", "S7", "S10", "S1", "S13", "S6", "S14", "S2", "S0", "S1", "S3", "S2", "S7", "S0", "S1"},
        {"S10", "S3", "S6", "S4", "S2", "S7", "S0", "S3", "S12", "S8", "S7", "S0", "S6", "S5", "S3", "S2", "S1", "S7", "S0", "S4"},
        {"S13", "S2", "S1", "S7", "S0", "S4", "S6", "S5", "S11", "S0", "S14", "S3", "S9", "S1", "S0", "S3", "S6", "S7", "S12", "S11"},
        {"S2", "S14", "S1", "S7", "S6", "S3", "S0", "S5", "S2", "S15", "S7", "S0", "S4", "S12", "S3", "S6", "S7", "S2", "S1", "S13"},
        {"S5", "S4", "S1", "S3", "S10", "S7", "S6", "S0", "S13", "S2", "S1", "S7", "S6", "S3", "S15", "S2", "S1", "S14", "S5", "S6"}
    };

    private final int id;
    private final int port;
    private final int nextPort;
    private final int retryTimeoutMs;
    private final int[] allClientPorts;
    private final Random random = new Random();

    private String[] chaineRef;
    private int token;

    public Client(int id) {
        if (id < 1 || id > CLIENT_COUNT) {
            throw new IllegalArgumentException("id doit etre compris entre 1 et " + CLIENT_COUNT);
        }
        this.id = id;
        this.chaineRef = SERVICE_CHAINS[id - 1];
        this.token = (id == 1) ? 1 : 0;
        this.port = BASE_CLIENT_PORT + id;
        this.nextPort = BASE_CLIENT_PORT + (id % CLIENT_COUNT) + 1;
        this.retryTimeoutMs = 10000 + (id - 1) * TOKEN_RETRY_STEP_MS;

        this.allClientPorts = new int[CLIENT_COUNT];
        for (int i = 0; i < CLIENT_COUNT; i++) {
            allClientPorts[i] = BASE_CLIENT_PORT + i + 1;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java client.Client <id 1-6>");
            return;
        }
        int id = Integer.parseInt(args[0]);
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", REGISTRY_PORT);
            AnnuaireServices directory = (AnnuaireServices) registry.lookup("ServeurInter");
            new Client(id).run(directory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run(AnnuaireServices directory) {
        try (DatagramSocket udpSocket = new DatagramSocket(port)) {
            while (chaineRef.length > 0) {
                if (token != 1) {
                    waitForToken(udpSocket);
                }
                sendServiceRequest(directory);
                if (token == 1) {
                    forwardToken(udpSocket);
                }
                token = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendServiceRequest(AnnuaireServices directory) {
        if (token != 1 || chaineRef.length == 0) {
            System.out.println("Client " + id + " ne possede pas le jeton");
            return;
        }
        try {
            String service = chaineRef[0];
            String resultat = directory.information_service(service);
            System.out.println("Client " + id + " - informations du service : " + resultat);
            chaineRef = Arrays.copyOfRange(chaineRef, 1, chaineRef.length);
            if (chaineRef.length == 0) {
                System.out.println("Client " + id + " a fini tous ses services");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void forwardToken(DatagramSocket udpSocket) {
        try {
            InetAddress nextClientAddress = InetAddress.getByName("localhost");
            byte[] data = String.valueOf(token).getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, nextClientAddress, nextPort);
            udpSocket.send(packet);
            System.out.println("Client " + id + " a transmis le jeton au port " + nextPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForToken(DatagramSocket udpSocket) {
        System.out.println("Client " + id + " attend le jeton...");
        try {
            udpSocket.setSoTimeout(TOKEN_WAIT_TIMEOUT_MS);
            do {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                udpSocket.receive(receivePacket);
                udpSocket.setSoTimeout(retryTimeoutMs);
                token = Integer.parseInt(new String(receivePacket.getData(), 0, receivePacket.getLength()));
            } while (token != 1);
            System.out.println("Client " + id + " a recu le jeton");
        } catch (SocketTimeoutException e) {
            System.out.println("Client " + id + " : delai depasse, election d'un nouveau porteur du jeton");
            electNewTokenHolder(udpSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
 // Choisit aleatoirement un nouveau porteur du jeton et previent tous les clients 
     
    private void electNewTokenHolder(DatagramSocket udpSocket) {
        int chosenPort = allClientPorts[random.nextInt(allClientPorts.length)];

        for (int clientPort : allClientPorts) {
            if (clientPort == port) {
                if (chosenPort == port) {
                    token = 1;
                    System.out.println("Client " + id + " s'est elu nouveau porteur du jeton");
                }
                continue;
            }
            try {
                InetAddress address = InetAddress.getByName("localhost");
                byte[] data = (clientPort == chosenPort ? "1" : "0").getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, clientPort);
                udpSocket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client " + id + " : nouvelle election terminee, porteur = port " + chosenPort);
    }
}
