DELETE FROM articles;

ALTER TABLE articles AUTO_INCREMENT = 1;

INSERT INTO articles (title, body)
VALUES ('タイトルです1', '1本文です。')
     , ('タイトルです2', '2本文です。')
     , ('タイトルです3', '3本文です。')
;

DELETE FROM users;

-- password => "password" for all users
INSERT INTO users (username, password, enabled)
VALUES ('user1', '$2a$10$esd2Yc/.cTe1trzUcoKvOOlQk8tT/Het0xohjlCXBw0TG/E6HOGs.', true)
     , ('user2', '$2a$10$t8qWABspN9FjYSjjs5pln.UlErwsZFg8UWwHtUlL.P.DRJ5rbR0p.', true)
     , ('user3', '$2a$10$KNCI.EkZliHUbhxXBzg1F.YruuNRg0AbNmMUsZP./Qm24m5eF.6IO', true)
;