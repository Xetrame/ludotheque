package ludotheque.modele;

public class Manga extends Media {

    public Manga(int id, String titre, String description, String genre, String auteurEditeur,
                         int annee, int ageMinimum, int nbExemplaires, int nbDisponibles) {
        super(id, titre, description, genre, auteurEditeur, annee, ageMinimum, nbExemplaires, nbDisponibles);
    }

    @Override
    public TypeMedia getType() { return TypeMedia.MANGA; }
}