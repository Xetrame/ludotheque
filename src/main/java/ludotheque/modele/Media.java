package ludotheque.modele;

/**
 * Classe mère abstraite des quatre types de médias.
 * En base : stratégie "table unique" (une table media + id_type).
 */
public abstract class Media {
    private int id;
    private String titre;
    private String description;
    private String genre;
    private String auteurEditeur;
    private int annee;
    private int ageMinimum;
    private int nbExemplaires;
    private int nbDisponibles;

    protected Media(int id, String titre, String description, String genre, String auteurEditeur,
                    int annee, int ageMinimum, int nbExemplaires, int nbDisponibles) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.genre = genre;
        this.auteurEditeur = auteurEditeur;
        this.annee = annee;
        this.ageMinimum = ageMinimum;
        this.nbExemplaires = nbExemplaires;
        this.nbDisponibles = nbDisponibles;
    }

    /** Chaque sous-classe indique son type : sert au polymorphisme et à l'écriture en base. */
    public abstract TypeMedia getType();

    /** C-03 : empruntable seulement s'il reste au moins un exemplaire. */
    public boolean estDisponible() {
        return nbDisponibles > 0;
    }

    /** Côté objet uniquement : en base, c'est MediaDao.retirerExemplaire qui fait foi (UPDATE conditionnel). */
    public void retirerExemplaire() {
        if (nbDisponibles == 0) {
            throw new IllegalStateException("Aucun exemplaire disponible");
        }
        nbDisponibles--;
    }

    public void rendreExemplaire() {
        if (nbDisponibles == nbExemplaires) {
            throw new IllegalStateException("Tous les exemplaires sont déjà rendus");
        }
        nbDisponibles++;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getAuteurEditeur() { return auteurEditeur; }
    public void setAuteurEditeur(String auteurEditeur) { this.auteurEditeur = auteurEditeur; }
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    public int getAgeMinimum() { return ageMinimum; }
    public void setAgeMinimum(int ageMinimum) { this.ageMinimum = ageMinimum; }
    // Pas de setter sur le stock : il ne change QUE par MediaDao, dans une transaction
    public int getNbExemplaires() { return nbExemplaires; }
    public int getNbDisponibles() { return nbDisponibles; }

    @Override
    public String toString() {
        return "#" + id + " [" + getType().getNomAffiche() + "] " + titre
                + " (" + nbDisponibles + "/" + nbExemplaires + " dispo)";
    }
}