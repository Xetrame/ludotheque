package ludotheque.dao;

import ludotheque.bdd.ConnexionBD;
import ludotheque.bdd.DaoException;
import ludotheque.modele.BandeDessinee;
import ludotheque.modele.JeuSociete;
import ludotheque.modele.JeuVideo;
import ludotheque.modele.Manga;
import ludotheque.modele.Media;
import ludotheque.modele.TypeMedia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO complet de la table media (CRUD + recherche + variantes transactionnelles). */
public class MediaDao implements Dao<Media> {

    /** Jointure sur type_media pour récupérer le libellé qui choisit la sous-classe. */
    private static final String SELECT_BASE =
            "SELECT m.*, t.libelle FROM media m JOIN type_media t ON t.id = m.id_type ";

    @Override
    public Optional<Media> trouver(int id) {
        String sql = SELECT_BASE + "WHERE m.id = ?";
        try (Connection c = ConnexionBD.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Lecture du média " + id + " impossible", e);
        }
    }

    @Override
    public List<Media> trouverTous() {
        return rechercher(null, null, null, false);
    }

    /**
     * F-11 : recherche par titre + filtres facultatifs (null = pas de filtre).
     * Le SQL est construit dynamiquement MAIS uniquement avec des "?" : aucune valeur concaténée.
     */
    public List<Media> rechercher(String titre, TypeMedia type, String genre, boolean disponiblesSeulement) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (titre != null && !titre.isBlank()) {
            sql.append(" AND m.titre LIKE ?");
            params.add("%" + titre + "%");
        }
        if (type != null) {
            sql.append(" AND t.libelle = ?");
            params.add(type.name());
        }
        if (genre != null && !genre.isBlank()) {
            sql.append(" AND m.genre = ?");
            params.add(genre);
        }
        if (disponiblesSeulement) {
            sql.append(" AND m.nb_disponibles > 0");
        }
        sql.append(" ORDER BY m.titre");

        List<Media> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(mapper(rs));
                }
            }
            return resultat;
        } catch (SQLException e) {
            throw new DaoException("Recherche de médias impossible", e);
        }
    }

    @Override
    public int creer(Media m) {
        String sql = "INSERT INTO media (titre, description, genre, auteur_editeur, annee, age_minimum, "
                + "plateforme, nb_joueurs, nb_exemplaires, nb_disponibles, id_type) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM type_media WHERE libelle = ?))";
        try (Connection c = ConnexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplirColonnes(ps, m);
            ps.setInt(9, m.getNbExemplaires());
            ps.setInt(10, m.getNbExemplaires());       // à la création, tout est disponible
            ps.setString(11, m.getType().name());       // libellé = nom de l'enum (CDC section 5)
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                int id = cles.getInt(1);
                m.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new DaoException("Création du média impossible", e);
        }
    }

    /** F-22 : modifie la fiche. Le stock n'est PAS touché ici : il ne change que par les transactions. */
    @Override
    public void modifier(Media m) {
        String sql = "UPDATE media SET titre = ?, description = ?, genre = ?, auteur_editeur = ?, annee = ?, "
                + "age_minimum = ?, plateforme = ?, nb_joueurs = ? WHERE id = ?";
        try (Connection c = ConnexionBD.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            remplirColonnes(ps, m);
            ps.setInt(9, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Modification du média " + m.getId() + " impossible", e);
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM media WHERE id = ?";
        try (Connection c = ConnexionBD.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Clé étrangère : un média qui a des emprunts ne peut pas être supprimé
            throw new DaoException("Suppression du média impossible (il a des emprunts ?)", e);
        }
    }

    // ---- Variantes TRANSACTIONNELLES : la connexion vient du service, on ne la ferme pas ----

    /**
     * C-03, UPDATE conditionnel : ne réussit que s'il reste un exemplaire.
     * Si deux adhérents prennent le dernier exemplaire en même temps, un seul obtient 1 ligne modifiée.
     */
    public boolean retirerExemplaire(Connection c, int idMedia) {
        String sql = "UPDATE media SET nb_disponibles = nb_disponibles - 1 WHERE id = ? AND nb_disponibles > 0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idMedia);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Mise à jour du stock impossible", e);
        }
    }

    public void rendreExemplaire(Connection c, int idMedia) {
        String sql = "UPDATE media SET nb_disponibles = nb_disponibles + 1 WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idMedia);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Mise à jour du stock impossible", e);
        }
    }

    // ---- Outils privés ----

    /** Colonnes 1 à 8 (la fiche), communes à INSERT et UPDATE. */
    private static void remplirColonnes(PreparedStatement ps, Media m) throws SQLException {
        ps.setString(1, m.getTitre());
        ps.setString(2, m.getDescription());
        ps.setString(3, m.getGenre());
        ps.setString(4, m.getAuteurEditeur());
        ps.setInt(5, m.getAnnee());
        ps.setInt(6, m.getAgeMinimum());
        // Colonnes propres à une sous-classe : NULL pour les autres types
        if (m instanceof JeuVideo jv) {
            ps.setString(7, jv.getPlateforme());
        } else {
            ps.setNull(7, Types.VARCHAR);
        }
        if (m instanceof JeuSociete js) {
            ps.setString(8, js.getNbJoueurs());
        } else {
            ps.setNull(8, Types.VARCHAR);
        }
    }

    /** FACTORY : instancie la bonne sous-classe selon le libellé de type_media (JEU_VIDEO, MANGA...). */
    private static Media mapper(ResultSet rs) throws SQLException {
        TypeMedia type = TypeMedia.valueOf(rs.getString("libelle"));   // "MANGA" -> TypeMedia.MANGA
        int id = rs.getInt("id");
        String titre = rs.getString("titre");
        String description = rs.getString("description");
        String genre = rs.getString("genre");
        String auteur = rs.getString("auteur_editeur");
        int annee = rs.getInt("annee");
        int age = rs.getInt("age_minimum");
        int total = rs.getInt("nb_exemplaires");
        int dispo = rs.getInt("nb_disponibles");
        return switch (type) {
            case JEU_VIDEO -> new JeuVideo(id, titre, description, genre, auteur, annee, age, total, dispo,
                    rs.getString("plateforme"));
            case JEU_SOCIETE -> new JeuSociete(id, titre, description, genre, auteur, annee, age, total, dispo,
                    rs.getString("nb_joueurs"));
            case BANDE_DESSINEE -> new BandeDessinee(id, titre, description, genre, auteur, annee, age, total, dispo);
            case MANGA -> new Manga(id, titre, description, genre, auteur, annee, age, total, dispo);
        };
    }
}