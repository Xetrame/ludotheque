-- Données de test.
-- Deux adhérents pour tester l'emprunt. Leur mot de passe n'est pas un vrai hash :
-- ils ne peuvent pas se connecter. Les vrais comptes (mot de passe haché en Java)
-- viendront avec l'authentification, à la séance suivante.
INSERT INTO utilisateur (id, nom, prenom, email, mot_de_passe_hash, role) VALUES
    (1, 'Durand', 'Alice', 'alice.test@ludo.fr', 'pas-de-mot-de-passe', 'ADHERENT'),
    (2, 'Petit', 'Bob', 'bob.test@ludo.fr', 'pas-de-mot-de-passe', 'ADHERENT');

INSERT INTO type_media (id, libelle) VALUES
    (1, 'JEU_VIDEO'),
    (2, 'BANDE_DESSINEE'),
    (3, 'MANGA'),
    (4, 'JEU_SOCIETE');

INSERT INTO media (titre, description, genre, auteur_editeur, annee, age_minimum,
                   plateforme, nb_joueurs, nb_exemplaires, nb_disponibles, id_type) VALUES
    ('Zelda : Tears of the Kingdom', 'Aventure en monde ouvert', 'Aventure', 'Nintendo', 2023, 12, 'Switch', NULL, 2, 2, 1),
    ('Celeste', 'Plateformes exigeant', 'Plateforme', 'Maddy Makes Games', 2018, 7, 'PC', NULL, 1, 1, 1),
    ('Mario Kart 8 Deluxe', 'Course entre amis', 'Course', 'Nintendo', 2017, 3, 'Switch', NULL, 3, 3, 1),
    ('Astérix le Gaulois', 'Le premier album', 'Humour', 'Goscinny et Uderzo', 1961, 0, NULL, NULL, 2, 2, 2),
    ('Blacksad', 'Polar animalier', 'Policier', 'Canales et Guarnido', 2000, 14, NULL, NULL, 1, 1, 2),
    ('One Piece tome 1', 'Début de l''aventure de Luffy', 'Aventure', 'Eiichiro Oda', 1997, 10, NULL, NULL, 3, 3, 3),
    ('Death Note tome 1', 'Thriller surnaturel', 'Thriller', 'Ohba et Obata', 2003, 14, NULL, NULL, 1, 1, 3),
    ('Catan', 'Commerce et colonisation', 'Stratégie', 'Kosmos', 1995, 10, NULL, '3-4', 2, 2, 4),
    ('Dixit', 'Jeu d''ambiance et d''imagination', 'Ambiance', 'Libellud', 2008, 8, NULL, '3-6', 1, 1, 4);
