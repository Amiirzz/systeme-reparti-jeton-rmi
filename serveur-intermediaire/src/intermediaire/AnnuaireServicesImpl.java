package intermediaire;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Route chaque demande de service vers le serveur final responsable,
 * puis relaie la reponse au client via une socket TCP.
 */
public class AnnuaireServicesImpl extends UnicastRemoteObject implements AnnuaireServices {

    private static final int REGISTRY_PORT = 1000;

    private final Map<String, ServerLocation> serviceToServer = new HashMap<>();

    private static class ServerLocation {
        final String ip;
        final int port;

        ServerLocation(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    protected AnnuaireServicesImpl() throws RemoteException {
        super();
        initServiceMap();
    }

    private void initServiceMap() {
        putRange(0, 3, "127.0.0.1", 2001);
        putRange(4, 6, "127.0.0.1", 2002);
        putRange(7, 9, "127.0.0.1", 2003);
        putRange(10, 12, "127.0.0.1", 2004);
        putRange(13, 15, "127.0.0.1", 2005);
    }

    private void putRange(int from, int to, String ip, int port) {
        for (int i = from; i <= to; i++) {
            serviceToServer.put("S" + i, new ServerLocation(ip, port));
        }
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            registry.rebind("ServeurInter", new AnnuaireServicesImpl());
            System.out.println("Serveur intermediaire pret sur le port " + REGISTRY_PORT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String information_service(String serviceName) throws RemoteException {
        return chercherService(serviceName);
    }

    @Override
    public String chercherService(String serviceName) throws RemoteException {
        ServerLocation location = serviceToServer.get(serviceName);
        if (location == null) {
            return "Service inconnu : " + serviceName;
        }
        return envoyerAuServeurFinal(location.ip, location.port, serviceName);
    }

    @Override
    public String envoyerAuServeurFinal(String adresse, int port, String service) {
        try (Socket socket = new Socket(adresse, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(service);
            return (String) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
