# Annuaire de Services Distribue

Projet de systemes repartis en Java illustrant trois mecanismes combines :
RMI, sockets TCP/UDP, et un algorithme de jeton circulant entre plusieurs
clients. 

## Architecture

Trois modules independants.

### 1. `client`
Six clients (id 1 a 6) organises en anneau logique. Un seul jeton circule
entre eux via UDP. Le client qui detient le jeton envoie sa prochaine
demande de service au serveur intermediaire via RMI, puis transmet le
jeton au client suivant. Si un client ne recoit pas le jeton dans le delai
imparti, une nouvelle election a lieu et un porteur est choisi
aleatoirement parmi les six clients.

### 2. `serveur-intermediaire`
Serveur intermediaire expose en RMI (`AnnuaireServices` /
`AnnuaireServicesImpl`). Il recoit le nom d'un service (`S0` a `S15`),
determine quel serveur final en est responsable, puis lui transmet la
demande via une socket TCP et retourne la reponse au client.

### 3. `serveur-catalogue`
Serveur final sous forme d'EJB stateless (Jakarta EE), avec persistance
JPA. Il gere deux entites : `InfoServeur` (un serveur physique) et
`InfoService` (un service).

## Prerequis

- JDK 17 ou superieur pour `client` et `serveur-intermediaire`.
- Un serveur d'application Jakarta EE (WildFly, par exemple) et une base
  de donnees pour deployer `serveur-catalogue`, qui depend de `jakarta.ejb`
  et `jakarta.persistence`.

## Compilation et execution

### Serveur intermediaire

```
cd serveur-intermediaire/src
javac intermediaire/*.java
java intermediaire.AnnuaireServicesImpl
```

### Clients

Le client a besoin de l'interface `AnnuaireServices` du serveur
intermediaire sur son classpath.

```
cd client/src
javac -cp .:../../serveur-intermediaire/src client/Client.java
java -cp .:../../serveur-intermediaire/src client.Client 1
```

Repeter la derniere commande dans cinq autres terminaux avec les
identifiants 2 a 6 pour demarrer les six clients.

### Serveur final (serveur-catalogue)

Ce module est un module EJB : il doit etre empaquete en `.jar` (ou inclus
dans un `.ear`) et deploye sur un serveur d'application Jakarta EE
configure avec la source de donnees `java:/MySqlDS1` referencee dans
`persistence.xml`. Un simple `javac` ne suffit pas.

## Structure du depot

```
annuaire-services-distribue/
  client/
    src/client/Client.java
  serveur-intermediaire/
    src/intermediaire/AnnuaireServices.java
    src/intermediaire/AnnuaireServicesImpl.java
  serveur-catalogue/
    src/catalogue/CatalogueServeurs.java
    src/catalogue/CatalogueServeursBean.java
    src/catalogue/InfoServeur.java
    src/catalogue/InfoService.java
    src/META-INF/persistence.xml
```
