package ludotheque.bdd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Point d'accès unique à la base MariaDB.
 * Les identifiants sont lus dans db.properties (hors du code et hors du dépôt Git).
 * Chaque appel à get() ouvre une NOUVELLE connexion : fermez-la avec try-with-resources.
 */
public final class ConnexionBD {

    private static final Properties PROPS = charger();

    private ConnexionBD() { }

    private static Properties charger() {
        Properties p = new Properties();
        try (InputStream in = ConnexionBD.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new DaoException("db.properties introuvable : copiez bdd/db.properties.example en bdd/db.properties", null);
            }
            p.load(in);
        } catch (IOException e) {
            throw new DaoException("Lecture de db.properties impossible", e);
        }
        return p;
    }

    public static Connection get() {
        String url = PROPS.getProperty("db.url");
        try {
            return DriverManager.getConnection(url, PROPS.getProperty("db.user"), PROPS.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DaoException("Connexion à la base impossible : " + url, e);
        }
    }
}