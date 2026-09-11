package catalogue;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "serveurs")
public class InfoServeur implements Serializable {

    @Id
    private int port;

    @Column(name = "adresse_IP")
    private String ip;

    @Column(name = "num")
    private int num;

    public InfoServeur() {
    }

    public InfoServeur(int port, String ip, int num) {
        this.port = port;
        this.ip = ip;
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "Serveur " + num + " - adresse IP : " + ip + " - port : " + port;
    }
}
