-- 2025年10月22日 FK & Index 更新
/*
FK列は検証時に自動的に参照検索が走るため、インデックスが無いと性能が低下します。
PostgreSQLはFK作成時に自動でインデックスを作成しないため、必要に応じて手動作成が必須です。
MySQLはFK作成時に自動でインデックスが付与されます。
*/

-- ========================================================
-- Transaction ① : 質問テーブル 外部キー・インデックス再構成
-- ========================================================
BEGIN;
SET lock_timeout = '5s';
SET statement_timeout = '30s';

ALTER TABLE public.questions
  DROP CONSTRAINT IF EXISTS questions_user_id_fkey;

ALTER TABLE public.questions
  ADD CONSTRAINT questions_user_id_fkey
  FOREIGN KEY (user_id) REFERENCES public.site_user(id)
  ON DELETE RESTRICT;

-- 性能インデックス（存在しなければ作成）
CREATE INDEX IF NOT EXISTS idx_questions_user_id ON public.questions(user_id);

COMMIT;

-- ========================================================
-- Transaction ② : 添付ファイル 外部キー・インデックス再構成
-- ========================================================
BEGIN;
SET lock_timeout = '5s';
SET statement_timeout = '30s';

ALTER TABLE public.uploaded_file
  DROP CONSTRAINT IF EXISTS fk_uploaded_file_user;

ALTER TABLE public.uploaded_file
  ADD CONSTRAINT fk_uploaded_file_user
  FOREIGN KEY (user_id) REFERENCES public.site_user(id)
  ON DELETE RESTRICT;

COMMIT;

-- 性能インデックス（存在しなければ作成）
CREATE INDEX IF NOT EXISTS idx_uploaded_file_user_id  ON public.uploaded_file(user_id);
CREATE INDEX IF NOT EXISTS idx_uploaded_file_question_id ON public.uploaded_file(question_id);

-- 失敗した場合は ROLLBACK を実行してください。

/*
【運用向けメモ（選択）】
- FKはロック影響を抑えるため以下のようにNOT VALID→VALIDATEで分割可能：
  ALTER TABLE public.questions
    ADD CONSTRAINT questions_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES public.site_user(id)
    ON DELETE RESTRICT
    NOT VALID;
  ALTER TABLE public.questions VALIDATE CONSTRAINT questions_user_id_fkey;

- インデックスはトラフィックがある場合 CONCURRENTLY 推奨（トランザクション外で実行）：
  CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_questions_user_id ON public.questions(user_id);

【簡易ロールバック例】
-- 外部キー取り消し
-- ALTER TABLE public.questions      DROP CONSTRAINT IF EXISTS questions_user_id_fkey;
-- ALTER TABLE public.uploaded_file DROP CONSTRAINT IF EXISTS fk_uploaded_file_user;
-- インデックス削除
-- DROP INDEX IF EXISTS public.idx_questions_user_id;
-- DROP INDEX IF EXISTS public.idx_uploaded_file_user_id;
-- DROP INDEX IF EXISTS public.idx_uploaded_file_question_id;
*/
