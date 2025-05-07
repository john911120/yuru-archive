-- Users Table Dummy Table
insert into users (username, password, email ) values
('テスト01', 'password1!@', 'test01@sample.com'),
('テスト02', 'password12!@', 'test02@sample.com'),
('テスト03', 'password123!@#', 'test03@sample.com');

-- user_roles Table Dummy Table
insert into user_roles (user_id, role) values
(1, 'admin'),
(2, 'editor'),
(3, 'user');

-- Questions Table Dummy Table
INSERT INTO questions (user_id, title, content) VALUES
(1, 'ユウカの体重の件について', '本当に100Kgですか?'),
(2, 'エ駄死の基準について', 'どこまでがエ駄死の基準になるの?'),
(3, 'ブラックマーケットの武器について', '品質があまりにもよくないって本当？');


-- Answers Table Dummy Table
INSERT INTO answers (question_id, user_id, content) VALUES
(1, 2, '本当ですよ。ｗｗｗ'),
(2, 1, 'とにかく駄目なものはため！'),
(3, 3, '品質は大体いいものですよ');


-- comments Table Dummy Table
INSERT INTO comments (user_id, answer_id, content) VALUES
(2, 1, 'モモイまたお前だなぁ待ってろよ。'),
(3, 3, 'お前あの店のバイトだなぁ？')
(3, 2, '理不尽すぎだろう。');
