DELETE FROM users;

ALTER TABLE users AUTO_INCREMENT = 1;

-- password => "password00" for all users
INSERT INTO users (id, username, password, enabled)
VALUES (1, 'user1', '$2a$10$B0SVL8nAzks3ES5G8cFS1ulpS4OZLQyCvnjzi0ISWx9FrXVzKbkLK', true)
     , (2, 'user2', '$2a$10$JbGAmUhXF.KFaI3VrnL8ZOxzVNCOnMqonPlL82uHF7O2qwROSLUWK', true)
     , (3, 'user3', '$2a$10$/x0LDWs0tk6V90.VQ2ObdezEgBCDKuoLi1LWtIe49NKUrc7LqrWGq', true)
;

DELETE FROM articles;

ALTER TABLE articles AUTO_INCREMENT = 1;

INSERT INTO articles (title, body, user_id)
VALUES ('タイトルです1', '1本文です。', 1)
     , ('タイトルです2', '2本文です。', 1)
     , ('タイトルです3', '3本文です。', 2)
;

