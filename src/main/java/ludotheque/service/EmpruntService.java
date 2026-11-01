package ludotheque.service;

import ludotheque.bdd.ConnexionBD;
import ludotheque.bdd.DaoException;
import ludotheque.dao.EmpruntDao;
import ludotheque.dao.MediaDao;
import ludotheque.modele.Emprunt;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Version S2 : la TRANSACTION D'EMPRUNT demandée au groupe 3.
 * Les autres règles du CDC (C-01, C-04, C-05, C-06), le retour et la réservation viendront ensuite.
 */
public class EmpruntService {

    /** C-02 : durée maximale d'un prêt, en jours. */
    public static final int DUREE_MAX_PRET_JOURS = 15;

    private final MediaDao medias;
    private final EmpruntDao emprunts;

    public EmpruntService(MediaDao medias, EmpruntDao emprunts) {
        this.medias = medias;
        this.emprunts = emprunts;
    }

    /** F-13 : l'adhérent emprunte lui-même, retour prévu dans 15 jours (C-02). */
    public Emprunt emprunter(int idUtilisateur, int idMedia) {
        return emprunter(idUtilisateur, idMedia, LocalDate.now().plusDays(DUREE_MAX_PRET_JOURS));
    }

    /**
     * F-24 : TRANSACTION D'EMPRUNT (l'administrateur peut choisir la date de retour).
     * Stock -1 puis création de l'emprunt : tout ou rien.
     * C-02 (15 jours maximum) est garantie par la contrainte chk_emprunt_duree de la base.
     */
    public Emprunt emprunter(int idUtilisateur, int idMedia, LocalDate dateRetourPrevue) {
        try (Connection c = ConnexionBD.get()) {
            c.setAutoCommit(false);
            try {
                // 1. C-03 : UPDATE conditionnel, échoue proprement s'il ne reste aucun exemplaire
                if (!medias.retirerExemplaire(c, idMedia)) {
                    throw new RegleMetierException("C-03", "plus aucun exemplaire disponible pour le média " + idMedia);
                }
                // 2. Création de l'emprunt. Si la base refuse (C-02, clé étrangère...),
                //    le stock déjà diminué à l'étape 1 est restauré par le rollback.
                Emprunt e = new Emprunt(0, idUtilisateur, idMedia, LocalDate.now(), dateRetourPrevue,
                        null, false, null);
                emprunts.creer(c, e);
                c.commit();
                return e;
            } catch (RuntimeException ex) {
                c.rollback();   // la base revient exactement à l'état d'avant
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DaoException("Emprunt impossible", e);
        }
    }
}