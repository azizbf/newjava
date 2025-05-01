DROP TABLE IF EXISTS reponsequiz;

CREATE TABLE reponsequiz (
    id_rep INT(11) PRIMARY KEY AUTO_INCREMENT,
    id_question INT(11),
    reponse_choisie VARCHAR(255),
    date_reponse DATETIME,
    points INT(11),
    id_utilisateur INT(11),
    FOREIGN KEY (id_utilisateur) REFERENCES user(id) ON DELETE CASCADE
);

ALTER TABLE reponsequiz
ADD CONSTRAINT fk_reponsequiz_user
FOREIGN KEY (id_utilisateur) REFERENCES user(id) ON DELETE CASCADE; 