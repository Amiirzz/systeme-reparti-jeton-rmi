package catalogue;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Stateless
public class CatalogueServeursBean implements CatalogueServeurs {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void ajouterServ(InfoServeur serveur) {
        em.persist(serveur);
    }

    @Override
    public InfoServeur rechercherServeur(int port) {
        return em.find(InfoServeur.class, port);
    }

    @Override
    @Transactional
    public void ajouterService(InfoService service) {
        em.persist(service);
    }

    @Override
    @Transactional
    public InfoService rechercherService(String nom) {
        return em.find(InfoService.class, nom);
    }
}
