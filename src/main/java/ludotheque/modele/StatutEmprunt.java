package ludotheque.modele;

/** Statut CALCULÉ par Emprunt.getStatut() : il n'est pas stocké en base. */
public enum StatutEmprunt {
    EN_COURS,
    EN_RETARD,
    RENDU
}