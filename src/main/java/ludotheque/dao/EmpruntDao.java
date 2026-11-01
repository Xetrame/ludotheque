package ludotheque.dao;

import ludotheque.bdd.ConnexionBD;
import ludotheque.bdd.DaoException;
import ludotheque.modele.Emprunt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Version S2 : juste ce qu'il faut pour la transaction d'emprunt.
 * La version complète (implements Dao, retours, prolongation, retards...) viendra ensuite.
 */
public class EmpruntDao {

    /** Tous les emprunts, du plus récent au plus ancien. */
    public List<Emprunt> trouverTous() {
        String sql = "SELECT * FROM emprunt ORDER BY date_emprunt DESC, id DESC";
        List<Emprunt> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(mapper(rs));
            }
            return resultat;
        } catch (SQLException e) {
            throw new DaoException("Lecture des emprunts impossible", e);
        }
    }

    /** Variante transactionnelle : utilise la connexion de l'appelant et ne la ferme pas. */
    public int creer(Connection c, Emprunt e) {
        String sql = "INSERT INTO emprunt (id_utilisateur, id_media, date_emprunt, date_retour_prevue) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getIdUtilisateur());
            ps.setInt(2, e.getIdMedia());
            ps.setObject(3, e.getDateEmprunt());
            ps.setObject(4, e.getDateRetourPrevue());
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                int id = cles.getInt(1);
                e.setId(id);
                return id;
            }
        } catch (SQLException ex) {
            throw new DaoException("Création de l'emprunt impossible", ex);
        }
    }

    private static Emprunt mapper(ResultSet rs) throws SQLException {
        return new Emprunt(
                rs.getInt("id"),
                rs.getInt("id_utilisateur"),
                rs.getInt("id_media"),
                rs.getObject("date_emprunt", LocalDate.class),
                rs.getObject("date_retour_prevue", LocalDate.class),
                rs.getObject("date_retour_effective", LocalDate.class),   // null si non rendu
                rs.getBoolean("prolonge"),
                rs.getString("etat_retour"));
    }
}