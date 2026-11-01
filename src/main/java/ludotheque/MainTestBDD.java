package ludotheque;

import ludotheque.bdd.DaoException;
import ludotheque.bdd.InitBase;
import ludotheque.dao.EmpruntDao;
import ludotheque.dao.MediaDao;
import ludotheque.modele.Emprunt;
import ludotheque.modele.JeuSociete;
import ludotheque.modele.Media;
import ludotheque.modele.TypeMedia;
import ludotheque.service.EmpruntService;
import ludotheque.service.RegleMetierException;

import java.time.LocalDate;
import java.util.List;

/**
 * Livrable de la séance 2 : prouve que la base, le DAO et la transaction d'emprunt fonctionnent.
 * Chaque étape vérifie son résultat : au moindre écart, le programme s'arrête avec une erreur.
 */
public class MainTestBDD {

    /** Comptes insérés par donnees_test.sql (les vrais comptes viendront avec l'authentification). */
    private static final int ID_ALICE = 1;
    private static final int ID_BOB = 2;

    private static MediaDao medias;

    public static void main(String[] args) {
        // 0. Repartir d'une base propre à chaque lancement
        InitBase.executerScript("/schema.sql");
        InitBase.executerScript("/donnees_test.sql");
        System.out.println("1. Base recréée et remplie");

        medias = new MediaDao();
        EmpruntDao emprunts = new EmpruntDao();
        EmpruntService service = new EmpruntService(medias, emprunts);

        // ---------------------------------------------------------------
        System.out.println("\n2. Catalogue (héritage : Factory + polymorphisme)");
        List<Media> catalogue = medias.trouverTous();
        catalogue.forEach(m -> System.out.println("   " + m + "  -> " + m.getClass().getSimpleName()));
        verifier(catalogue.size() == 9, "9 médias attendus");

        // ---------------------------------------------------------------
        System.out.println("\n3. CRUD complet sur MediaDao");
        JeuSociete nouveau = new JeuSociete(0, "Les Aventuriers du Rail", "Trains", "Stratégie", "Days of Wonder",
                2004, 8, 3, 3, "2-5");
        int idNouveau = medias.creer(nouveau);
        System.out.println("   Créé : " + medias.trouver(idNouveau).orElseThrow());
        nouveau.setTitre("Les Aventuriers du Rail : Europe");
        medias.modifier(nouveau);
        verifier(medias.trouver(idNouveau).orElseThrow().getTitre().endsWith("Europe"), "modification lue en base");
        medias.supprimer(idNouveau);
        verifier(medias.trouver(idNouveau).isEmpty(), "média supprimé");
        System.out.println("   OK : créer, lire, modifier, supprimer");

        // ---------------------------------------------------------------
        System.out.println("\n4. Recherche et requêtes préparées (F-11)");
        List<Media> mangas = medias.rechercher(null, TypeMedia.MANGA, null, false);
        List<Media> aventure = medias.rechercher(null, null, "Aventure", false);
        System.out.println("   Mangas : " + mangas.size() + ", genre Aventure : " + aventure.size());
        verifier(mangas.size() == 2 && aventure.size() == 2, "résultats de recherche");
        // Tentative d'injection SQL : traitée comme du texte grâce au PreparedStatement
        verifier(medias.rechercher("' OR '1'='1", null, null, false).isEmpty(), "injection SQL neutralisée");
        System.out.println("   OK : l'injection ' OR '1'='1 ne renvoie rien");

        // ---------------------------------------------------------------
        System.out.println("\n5. TRANSACTION d'emprunt sur le dernier exemplaire de « Celeste »");
        int idCeleste = id("Celeste");
        Emprunt e1 = service.emprunter(ID_ALICE, idCeleste);
        System.out.println("   Alice : " + e1);
        verifier(stock(idCeleste) == 0, "stock à 0 après l'emprunt");
        try {
            service.emprunter(ID_BOB, idCeleste);
            verifier(false, "Bob n'aurait pas dû pouvoir emprunter");
        } catch (RegleMetierException ex) {
            System.out.println("   Bob   : refusé -> " + ex.getMessage());
        }
        verifier(emprunts.trouverTous().size() == 1, "un seul emprunt en base");

        // ---------------------------------------------------------------
        System.out.println("\n6. ROLLBACK prouvé : retour prévu à 20 jours, refusé par la base (C-02)");
        int idCatan = id("Catan");
        int stockAvant = stock(idCatan);
        try {
            // Le stock baisse (étape 1 du service), puis l'INSERT viole le CHECK chk_emprunt_duree
            service.emprunter(ID_BOB, idCatan, LocalDate.now().plusDays(20));
            verifier(false, "l'emprunt de 20 jours aurait dû échouer");
        } catch (DaoException ex) {
            System.out.println("   Échec attendu : " + ex.getCause().getMessage());
        }
        int stockApres = stock(idCatan);
        System.out.println("   Stock de Catan avant = " + stockAvant + ", après = " + stockApres);
        verifier(stockAvant == stockApres, "le rollback doit restaurer le stock");
        verifier(emprunts.trouverTous().size() == 1, "aucun emprunt fantôme");

        System.out.println("\nTout fonctionne.");
    }

    /** Id du premier média dont le titre contient ce texte. */
    private static int id(String titre) {
        return medias.rechercher(titre, null, null, false).get(0).getId();
    }

    /** Stock disponible relu en base. */
    private static int stock(int idMedia) {
        return medias.trouver(idMedia).orElseThrow().getNbDisponibles();
    }

    /** Mini-assertion : arrête le programme si une vérification échoue. */
    private static void verifier(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("ÉCHEC : " + message);
        }
    }
}