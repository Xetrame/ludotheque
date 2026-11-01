package ludotheque.modele;

/**
 * Les quatre types de médias. Le NOM de chaque constante (JEU_VIDEO, MANGA...) est EXACTEMENT
 * le libellé stocké dans la table type_media (section 5 du CDC) : MediaDao (la Factory)
 * fait TypeMedia.valueOf(libelle) pour instancier la bonne sous-classe.
 * Le nom affiché ("Jeu vidéo") ne sert qu'à l'interface.
 */
public enum TypeMedia {
    JEU_VIDEO("Jeu vidéo"),
    JEU_SOCIETE("Jeu de société"),
    BANDE_DESSINEE("BD"),
    MANGA("Manga");

    private final String nomAffiche;

    TypeMedia(String nomAffiche) {
        this.nomAffiche = nomAffiche;
    }

    public String getNomAffiche() {
        return nomAffiche;
    }
}