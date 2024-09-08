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
VALUES ('user1', '$2a$10$yov2wEmxbog1xF0pmeoMB.HMKkSoFWuT95xrDZs8K5XLDTkvWe9ua', true)
     , ('user2', '$2a$10$R98uxPGVBI4bmBBU5vYmC.M0ukEaIySVZUEjzjPI6vAnoeuma7juK', true)
     , ('user3', '$2a$10$2hIkgw9SXNZ4xUWUVrqzP.cWkAVkUfvYcqRerGqNPXQ4XUSABIEJm', true)
;