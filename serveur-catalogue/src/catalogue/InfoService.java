package catalogue;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Service")
public class InfoService implements Serializable {

    @Id
    private String nom;
    private int num;
    private String description;

    public InfoService() {
    }

    public InfoService(String nom, int num, String description) {
        this.nom = nom;
        this.num = num;
        this.description = description;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "[ numero du service = " + num + ", nom du service = " + nom + ", description = " + description + " ]";
    }
}
