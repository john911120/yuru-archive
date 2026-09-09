# 検証記録

## 実施結果

0.1の1次リファクタリング後、以下の静的検証を実施しました。

- `Question` / `Answer` / `SiteUser` / `UploadedFile` のSHA-256を原本と比較し、完全一致を確認
- ローカルDB接続、HikariCP、`ddl-auto=none`、アップロード先設定が原本と同一であることを確認
- ControllerからRepositoryへの直接依存が残っていないことを確認
- 質問登録時の旧 `uploadFilesFromDTOs` 経路が削除され、添付保存が `AttachService` に統合されていることを確認
- `QuestionRepository` のID型が `Long` であることを確認
- `System.out.println` / `printStackTrace` / Multipart一時デバッグログをメインJavaソースから除去したことを確認
- `src/main/resources/static` 配下のJavaScript 5ファイルについて `node --check` がすべて成功
- Markdownのローカルリンクを検査し、リンク切れ0件を確認
- `.git` / `.gradle` / `build` / `bin` / `node_modules` が配布対象に含まれていないことを確認
- Javaソースを `javac -proc:none` で走査し、外部依存クラス不足以外の構文エラーが検出されないことを確認

## DB保護確認

以下のEntityファイルは原本とバイト単位で変更していません。

- `question/Question.java`
- `answer/Answer.java`
- `user/SiteUser.java`
- `attach/entity/UploadedFile.java`

そのため、0.1リファクタリングではテーブル・カラム・外部キーのマッピング変更はありません。

## Gradleテスト

以下を実行しました。

```bash
./gradlew clean test --no-daemon
```

本検証環境では `services.gradle.org` の名前解決ができず、Gradle 8.14.5本体の取得段階で停止しました。

```text
java.net.UnknownHostException: services.gradle.org
```

したがって、この環境ではSpring依存関係を利用した実コンパイル・JUnit・アプリケーション起動まで完走できていません。

## ローカルSTSでの回帰確認項目

最終的な回帰確認は、Gradle 8.14.5が利用可能なローカル環境で以下を実施してください。

1. `gradlew.bat clean test`
2. `gradlew.bat clean processResources --no-daemon`
3. アプリケーション起動
4. ログイン / ログアウト
5. 質問一覧・検索
6. 質問登録
7. 画像付き質問登録（重複ファイルが生成されないことも確認）
8. 質問修正・添付追加・添付削除
9. 質問削除
10. 回答登録・修正・削除
11. 回答へのいいね（重複・自己いいねも確認）
12. Markdown表示
13. リンクカード表示と外部API失敗時フォールバック
14. MFEメモ画面 `/memos/`（Vue SPA表示、SPAルーティング、localStorage保存）
15. 既存PostgreSQLデータの表示

DBスキーマの自動更新は行わず、既存データを利用して確認します。

## MFE Last Flight確認項目

MFEを含む最終コミットでは、次の接続点が現行仕様として存在することを確認対象とします。

- Vue側 `base: '/memos/'`
- `src/main/resources/static/memo/` にVueビルド成果物を配置
- `MemoResourceConfig` による `/memos/**` と `/memos/assets/**` の配信
- `navbar.html` から `/memos/` への遷移
- メモデータがSpring / PostgreSQLではなくブラウザ `localStorage` に保存されること

次フェーズのMFE分離作業では、これらを「撤去・変更対象一覧」として利用します。
本節は**Javaプロジェクト内部にMFEが存在する最後の回帰確認記録**です。
