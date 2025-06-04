/*
	-- default now()を入れてデータをInsertしたときに自動的に現在の時間が入れるようにしました。
*/

-- ユーザー情報を保存するテーブル (ユーザー1人の基本情報)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,  -- ユーザーID (主キー)
    username VARCHAR(255) NOT NULL UNIQUE, -- ユーザー名 (ユニーク制約あり)
    password VARCHAR(255) NOT NULL, -- パスワード (暗号化された文字列を保存)
    email VARCHAR(255) NOT NULL UNIQUE, -- メールアドレス (ユニーク制約あり)
    created_at TIMESTAMP DEFAULT now() -- 作成日時 (デフォルトで現在日時)
);

-- ユーザーの役割 (ロール) を保存するテーブル
-- users と user_roles は 1:N 関係 (ユーザー1人に複数のロールを設定可能)
CREATE TABLE user_roles (
    id SERIAL PRIMARY KEY,  -- ロールID (主キー)
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- ユーザーID (外部キー / ユーザー削除時はロールも削除)
    role VARCHAR(50) NOT NULL -- ロール名
);

-- 質問内容を保存するテーブル
-- users と questions は 1:N 関係 (ユーザー1人が複数の質問を投稿可能)
CREATE TABLE questions (
    id SERIAL PRIMARY KEY,  -- 質問ID (主キー)
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE SET NULL, -- ユーザーID (外部キー / ユーザー削除時はNULL)
    title VARCHAR(255) NOT NULL, -- 質問タイトル
    content TEXT NOT NULL, -- 質問内容
    created_at TIMESTAMP DEFAULT now() -- 作成日時
);

-- 回答内容を保存するテーブル
-- questions と answers は 1:N 関係 (質問1件に対して複数の回答が可能)
-- users と answers も 1:N 関係 (ユーザー1人が複数の回答を投稿可能)
CREATE TABLE answers (
    id SERIAL PRIMARY KEY,  -- 回答ID (主キー)
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE, -- 質問ID (外部キー / 質問削除時は回答も削除)
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE SET NULL, -- ユーザーID (外部キー / ユーザー削除時はNULL)
    content TEXT NOT NULL, -- 回答内容
    created_at TIMESTAMP DEFAULT now() -- 作成日時
);

-- コメントを保存するテーブル
-- users と comments は 1:N 関係 (ユーザー1人が複数のコメントを投稿可能)
-- questions と comments は 1:N 関係 (質問1件に対して複数のコメントが可能)
-- answers と comments も 1:N 関係 (回答1件に対して複数のコメントが可能)
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,  -- コメントID (主キー)
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- ユーザーID (外部キー / ユーザー削除時はコメントも削除)
    question_id INTEGER REFERENCES questions(id) ON DELETE CASCADE, -- 質問ID (外部キー)
    answer_id INTEGER REFERENCES answers(id) ON DELETE CASCADE, -- 回答ID (外部キー)
    content TEXT NOT NULL, -- コメント内容
    created_at TIMESTAMP DEFAULT now() -- 作成日時
);

-- コメント対象を制限する制約 (質問または回答のどちらかのみを指定する必要がある)
ALTER TABLE comments
ADD CONSTRAINT chk_question_or_answer
CHECK (
    (question_id IS NOT NULL AND answer_id IS NULL)
    OR (question_id IS NULL AND answer_id IS NOT NULL)
);


-- default now()を入れてデータをInsertしたときに自動的に現在の時間が入れるようにしました。
alter table users 
alter column created_at set default now();

alter table questions
alter column created_at set default now();

alter table answers 
alter column created_at set default now();

commit;

 
/*
　スーパーアカウントで、間違えて、作ったテーブルを削除する
 一度作ったアカウントが本当にスーパーアカウントかを確認したうえて、下のコードを実行してください。
 でもないと、復旧はできませんので、改めて再確認お願いします。
*/
DROP TABLE IF EXISTS QUESTIONS CASCADE;
DROP TABLE IF EXISTS ANSWERS CASCADE;
DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS COMMENTS CASCADE;
DROP TABLE IF EXISTS USER_ROLES CASCADE;

-- 先のテーブルが削除完了しましたら、コミットをしてください。
commit;



-- 添付ファイルを管理するデータベースです（ユーザーと紐付け）。
CREATE TABLE uploaded_file (
    id SERIAL PRIMARY KEY,                             -- 識別番号（自動増分）
    user_id INTEGER NOT NULL,                          -- ユーザーID（外部キー）
    file_name VARCHAR(255) NOT NULL,                   -- オリジナルファイル名
    folder_path VARCHAR(100),                          -- イメージ分類フォルダー（例: uploads/2025/05/09）
    created_at TIMESTAMP DEFAULT now()                 -- 登録日時

    CONSTRAINT fk_uploaded_file_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Answer テーブルにカーラムを追加します。
ALTER TABLE answers ADD COLUMN author_id INTEGER;
ALTER TABLE answers ADD COLUMN updated_at TIMESTAMP;

commit;

-- ちゃんと反映されていることを確認してください。
select * from answers;


-- 回答に対する投票情報を保持する中間テーブル
CREATE TABLE answers_voter (
    answer_id BIGINT NOT NULL, -- 回答ID（外部キー、answersテーブルを参照）
    voter_id BIGINT NOT NULL,  -- 投票ユーザーID（外部キー、site_userテーブルを参照）
    PRIMARY KEY (answer_id, voter_id), -- 複合主キー（重複投票を防ぐ）
    FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE, -- 回答削除時に関連投票も削除
    FOREIGN KEY (voter_id) REFERENCES site_user(id) ON DELETE CASCADE  -- ユーザー削除時に関連投票も削除
);
-- ちゃんと追加されていることを確認してください。
select * from answers_voter;


-- 重複や空のテーブルを削除する。
-- 使われていないことを確認したうえで、行ってください。
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS answer_voter;

-- FKの制約で削除できないときに使う方法
-- 特定のテーブルに対して定義された制約（主キー、外部キーなど）を一覧表示する
-- このクエリは 'question' テーブルに設定された制約情報を抽出するためのもの
-- 外部キー制約であれば、参照されているテーブル名も取得される

SELECT
    conname AS constraint_name,          -- 制約名（例: 外部キーや主キーの名前）こっちです。
    conrelid::regclass AS table_from,    -- 制約が定義されている元のテーブル名
    confrelid::regclass AS table_to      -- 外部キーが参照しているテーブル名（外部キーでない場合はNULL）
FROM
    pg_constraint
WHERE
    conrelid::regclass::text = 'question'; -- 対象テーブル名（ここでは 'question'）

-- FKを参照している制約名を削除します
ALTER TABLE question DROP CONSTRAINT IF EXISTS "fkoI558t304fpmksa6mgxrkyg3";

-- 削除出来なかったQueryに CASCADEを追加して、再実行します。
-- 後は、ちゃんと削除できたのかをselect文のqueryを作成し、確認します。
DROP TABLE IF EXISTS question CASCADE;    


-- 202505027 Answer Table DataType Change Query Add

/* Answer Table 制約条件を修正します。  */
-- 1. FK制約条件を削除します。
ALTER TABLE answers DROP CONSTRAINT answers_user_id_fkey;
-- 2. Columnのタイプを変更します。
ALTER TABLE answers ALTER COLUMN user_id TYPE BIGINT;
-- 3. FKキー制約条件をさらに追加します。(int4 > BIGINT)
-- SiteUser.id = Long と一致させるためであります。
ALTER TABLE answers
ADD CONSTRAINT answers_user_id_fkey
FOREIGN KEY (user_id) REFERENCES site_user(id);
