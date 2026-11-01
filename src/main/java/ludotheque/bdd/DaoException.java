package ludotheque.bdd;

/**
 * Exception non vérifiée qui enveloppe les SQLException.
 * Les couches hautes (services, contrôleurs JavaFX) n'ont pas à connaître java.sql.
 */
public class DaoException extends RuntimeException {
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}