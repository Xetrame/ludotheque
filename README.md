# Ludothèque : application Java / JavaFX

Prêt gratuit de jeux vidéo, BD, mangas et jeux de société.
Groupe 3 : Celisse Kyllian, Diarrassouba Noah, Gamon Thomas, Hego Maxence.

## Prérequis
- JDK 17 ou plus, Maven 3.8+
- MariaDB 10.6 ou plus

## Installer la base (une seule fois)
```bash
sudo apt install mariadb-server
sudo systemctl enable --now mariadb
sudo mariadb
```
```sql
CREATE DATABASE ludotheque CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ludo'@'localhost' IDENTIFIED BY 'choisissez_un_mot_de_passe';
GRANT ALL PRIVILEGES ON ludotheque.* TO 'ludo'@'localhost';
FLUSH PRIVILEGES;
```

## Configurer
```bash
cp bdd/db.properties.example bdd/db.properties
# mettez votre mot de passe dans bdd/db.properties (ignoré par Git)
```

## Recréer la base et tester
```bash
mvn compile exec:java
```
MainTestBDD exécute bdd/schema.sql (supprime et recrée les tables) puis bdd/donnees_test.sql,
vérifie 6 scénarios et affiche « Tout fonctionne. ».

## Organisation
| Élément | Rôle |
|---|---|
| bdd/schema.sql | Tables, clés, contraintes CHECK, index |
| bdd/donnees_test.sql | Adhérents, types et médias de test |
| bdd/volume_test.sql | 50 000 emprunts pour mesurer les index |
| bdd/optimisation.md | EXPLAIN avant / après |
| bdd/mld.md, bdd/mld.png, bdd/mcd.png | Modèles de données |
| ludotheque.bdd | ConnexionBD, InitBase, DaoException |
| ludotheque.modele | Classes métier (Media abstraite + 4 sous-classes, Emprunt) |
| ludotheque.dao | MediaDao (DAO complet), EmpruntDao |
| ludotheque.service | Transaction d'emprunt |
