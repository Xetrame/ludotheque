package ludotheque.dao;

import java.util.List;
import java.util.Optional;

/** Contrat commun : un DAO par table, tout le SQL de l'application vit dans les DAO. */
public interface Dao<T> {

    Optional<T> trouver(int id);

    List<T> trouverTous();

    /** Insère l'objet et retourne l'identifiant généré par la base. */
    int creer(T objet);

    void modifier(T objet);

    void supprimer(int id);
}