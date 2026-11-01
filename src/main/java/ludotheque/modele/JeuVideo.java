package ludotheque.modele;

public class JeuVideo extends Media {
    private String plateforme;

    public JeuVideo(int id, String titre, String description, String genre, String auteurEditeur,
                    int annee, int ageMinimum, int nbExemplaires, int nbDisponibles, String plateforme) {
        super(id, titre, description, genre, auteurEditeur, annee, ageMinimum, nbExemplaires, nbDisponibles);
        this.plateforme = plateforme;
    }

    public String getPlateforme() { return plateforme; }
    public void setPlateforme(String plateforme) { this.plateforme = plateforme; }

    @Override
    public TypeMedia getType() { return TypeMedia.JEU_VIDEO; }

    @Override
    public String toString() { return super.toString() + " sur " + plateforme; }
}