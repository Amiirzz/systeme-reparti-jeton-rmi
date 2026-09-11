package catalogue;

import jakarta.ejb.Remote;

@Remote
public interface CatalogueServeurs {

    void ajouterServ(InfoServeur serveur);

    InfoServeur rechercherServeur(int port);

    void ajouterService(InfoService service);

    InfoService rechercherService(String nom);
}
