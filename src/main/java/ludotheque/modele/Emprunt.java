package ludotheque.modele;

import java.time.LocalDate;

public class Emprunt {
    private int id;
    private final int idUtilisateur;
    private final int idMedia;
    private final LocalDate dateEmprunt;
    private LocalDate dateRetourPrevue;
    private LocalDate dateRetourEffective;   // null tant que le média n'est pas rendu
    private boolean prolonge;
    private String etatRetour;

    public Emprunt(int id, int idUtilisateur, int idMedia, LocalDate dateEmprunt, LocalDate dateRetourPrevue,
                   LocalDate dateRetourEffective, boolean prolonge, String etatRetour) {
        this.id = id;
        this.idUtilisateur = idUtilisateur;
        this.idMedia = idMedia;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.dateRetourEffective = dateRetourEffective;
        this.prolonge = prolonge;
        this.etatRetour = etatRetour;
    }

    public boolean estRendu() {
        return dateRetourEffective != null;
    }

    /** Le retard se CALCULE, il n'est pas stocké en base. */
    public boolean estEnRetard(LocalDate aujourdhui) {
        return !estRendu() && aujourdhui.isAfter(dateRetourPrevue);
    }

    public boolean estEnRetard() {
        return estEnRetard(LocalDate.now());
    }

    /** Statut dérivé des dates : EN_COURS, EN_RETARD ou RENDU. */
    public StatutEmprunt getStatut() {
        if (estRendu()) {
            return StatutEmprunt.RENDU;
        }
        return estEnRetard() ? StatutEmprunt.EN_RETARD : StatutEmprunt.EN_COURS;
    }

    public void prolonger(int jours) {
        if (prolonge) {
            throw new IllegalStateException("Emprunt déjà prolongé une fois");
        }
        dateRetourPrevue = dateRetourPrevue.plusDays(jours);
        prolonge = true;
    }

    public void cloturer(String etat) {
        dateRetourEffective = LocalDate.now();
        etatRetour = etat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public int getIdMedia() { return idMedia; }
    public LocalDate getDateEmprunt() { return dateEmprunt; }
    public LocalDate getDateRetourPrevue() { return dateRetourPrevue; }
    public LocalDate getDateRetourEffective() { return dateRetourEffective; }
    public boolean isProlonge() { return prolonge; }
    public String getEtatRetour() { return etatRetour; }

    @Override
    public String toString() {
        return "Emprunt #" + id + " (média " + idMedia + ", retour prévu le " + dateRetourPrevue
                + (prolonge ? ", prolongé" : "") + ", " + getStatut() + ")";
    }
}