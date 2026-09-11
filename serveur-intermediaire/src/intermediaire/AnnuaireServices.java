package intermediaire;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI exposee par le serveur intermediaire.
 */
public interface AnnuaireServices extends Remote {

    String information_service(String serviceName) throws RemoteException;

    String chercherService(String serviceName) throws RemoteException;

    String envoyerAuServeurFinal(String adresse, int port, String service) throws RemoteException;
}
