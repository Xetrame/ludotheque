package ludotheque.service;

/**
 * Exception métier : une règle de la section 6 du CDC interdit l'opération.
 * Le code (C-01 ... C-06) permet aux tests et à l'IHM de savoir laquelle,
 * le message est assez clair pour être affiché tel quel à l'adhérent.
 */
public class RegleMetierException extends RuntimeException {

    private final String code;

    public RegleMetierException(String code, String message) {
        super(code + " : " + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}