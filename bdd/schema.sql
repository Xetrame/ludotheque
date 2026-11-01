-- =============================================================
-- Schéma de la base Ludothèque (MariaDB 10.6+)
-- Conforme à la section 5 et aux règles C-01 à C-06 du cahier des charges.
-- Recrée toutes les tables : relancer ce script remet la base à zéro.
-- Attention : InitBase découpe sur les points-virgules,
-- n'en mettez jamais dans un commentaire ou une chaîne.
-- =============================================================

-- Suppression dans l'ordre inverse des dépendances (les tables filles d'abord)
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS emprunt;
DROP TABLE IF EXISTS media;
DROP TABLE IF EXISTS type_media;
DROP TABLE IF EXISTS utilisateur;

CREATE TABLE utilisateur (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    prenom            VARCHAR(100) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,          -- format sel:hash (PBKDF2), jamais le mot de passe
    telephone         VARCHAR(20),
    role              VARCHAR(10)  NOT NULL DEFAULT 'ADHERENT',
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,   -- C-04
    date_inscription  DATE         NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT chk_utilisateur_role CHECK (role IN ('ADHERENT', 'ADMIN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Libellés imposés par le CDC (section 5) : ce sont les noms de l'enum Java TypeMedia
CREATE TABLE type_media (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT chk_type_libelle CHECK (libelle IN ('JEU_VIDEO', 'JEU_SOCIETE', 'BANDE_DESSINEE', 'MANGA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Héritage : stratégie TABLE UNIQUE.
-- Une seule table media, id_type indique la sous-classe Java.
-- Les colonnes propres à une sous-classe sont NULL pour les autres types.
CREATE TABLE media (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    titre          VARCHAR(200) NOT NULL,
    description    TEXT,
    genre          VARCHAR(50),
    auteur_editeur VARCHAR(150),
    annee          SMALLINT,
    age_minimum    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    plateforme     VARCHAR(50),                         -- JeuVideo uniquement
    nb_joueurs     VARCHAR(20),                         -- JeuSociete uniquement (ex. 2-4)
    nb_exemplaires INT NOT NULL DEFAULT 1,
    nb_disponibles INT NOT NULL DEFAULT 1,              -- donnée dérivée assumée : voir mld.md
    id_type        INT NOT NULL,
    CONSTRAINT fk_media_type FOREIGN KEY (id_type) REFERENCES type_media (id),
    CONSTRAINT chk_media_exemplaires CHECK (nb_exemplaires >= 0),
    -- C-03 et contrainte de la section 5 : 0 <= nb_disponibles <= nb_exemplaires
    CONSTRAINT chk_media_disponibles CHECK (nb_disponibles >= 0 AND nb_disponibles <= nb_exemplaires),
    CONSTRAINT chk_media_age CHECK (age_minimum <= 18)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Pas de colonne statut : EN_COURS / EN_RETARD / RENDU se CALCULE
-- à partir de date_retour_effective et date_retour_prevue (voir Emprunt.getStatut).
CREATE TABLE emprunt (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur        INT  NOT NULL,
    id_media              INT  NOT NULL,
    date_emprunt          DATE NOT NULL,
    date_retour_prevue    DATE NOT NULL,
    date_retour_effective DATE,                         -- NULL tant que le média n'est pas rendu
    prolonge              BOOLEAN NOT NULL DEFAULT FALSE,   -- C-05
    etat_retour           VARCHAR(255),                 -- renseigné au retour (F-25)
    CONSTRAINT fk_emprunt_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id),
    CONSTRAINT fk_emprunt_media       FOREIGN KEY (id_media)       REFERENCES media (id),
    CONSTRAINT chk_emprunt_dates  CHECK (date_retour_prevue > date_emprunt),
    -- C-02 : 15 jours au plus, 15 + 7 une fois l'emprunt prolongé (C-05)
    CONSTRAINT chk_emprunt_duree  CHECK (date_retour_prevue <= date_emprunt + INTERVAL IF(prolonge, 22, 15) DAY),
    CONSTRAINT chk_emprunt_retour CHECK (date_retour_effective IS NULL OR date_retour_effective >= date_emprunt),
    CONSTRAINT chk_emprunt_etat   CHECK (etat_retour IS NULL OR date_retour_effective IS NOT NULL)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Table de la section 5 du CDC (clé id_reservation).
-- Le statut n'est PAS calculable (une annulation est un choix) : on le stocke.
CREATE TABLE reservation (
    id_reservation   INT AUTO_INCREMENT PRIMARY KEY,
    date_reservation DATE NOT NULL,
    id_utilisateur   INT  NOT NULL,
    id_media         INT  NOT NULL,
    statut           VARCHAR(12) NOT NULL DEFAULT 'EN_ATTENTE',
    CONSTRAINT fk_reservation_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id),
    CONSTRAINT fk_reservation_media       FOREIGN KEY (id_media)       REFERENCES media (id),
    CONSTRAINT chk_reservation_statut CHECK (statut IN ('EN_ATTENTE', 'DISPONIBLE', 'HONOREE', 'ANNULEE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- =============================================================
-- Index justifiés par EXPLAIN (voir bdd/optimisation.md)
-- InnoDB indexe déjà automatiquement chaque clé étrangère.
-- =============================================================

-- F-26 / F-27 : emprunts en cours et en retard
CREATE INDEX idx_emprunt_en_cours ON emprunt (date_retour_effective, date_retour_prevue);

-- F-11 : filtre du catalogue par genre
CREATE INDEX idx_media_genre ON media (genre);
