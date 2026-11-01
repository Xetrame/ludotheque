package ludotheque.modele;

public class JeuSociete extends Media {
    private String nbJoueurs;   // ex. "2-4"

    public JeuSociete(int id, String titre, String description, String genre, String auteurEditeur,
                      int annee, int ageMinimum, int nbExemplaires, int nbDisponibles, String nbJoueurs) {
        super(id, titre, description, genre, auteurEditeur, annee, ageMinimum, nbExemplaires, nbDisponibles);
        this.nbJoueurs = nbJoueurs;
    }

    public String getNbJoueurs() { return nbJoueurs; }
    public void setNbJoueurs(String nbJoueurs) { this.nbJoueurs = nbJoueurs; }

    @Override
    public TypeMedia getType() { return TypeMedia.JEU_SOCIETE; }

    @Override
    public String toString() { return super.toString() + ", " + nbJoueurs + " joueurs"; }
}