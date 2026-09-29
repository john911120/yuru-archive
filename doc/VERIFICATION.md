# 検証記録

## 2026-09 改善後の静的確認

以下を確認しました。

- `src/main/java` に `MemoResourceConfig` が存在しない
- `src/main/resources/static/memo` が存在しない
- ルートに `package.json` / `package-lock.json` が存在しない
- 実行コード・テンプレート・ビルド設定に `/memos/**` の参照が存在しない
- navbarにMFE移動リンクが存在しない
- legacy `AttachController` が存在しない
- `AttachService.deleteFile(String)` / `getUploadPath()` が存在しない
- 質問本文の生データを直接 `th:utext` へ渡す経路を削除
- `LinkCardRenderService` が通常本文を `CommonUtil.markdown()` 経由でサニタイズすることを確認
- リンクカードの非HTTP URLを拒否する処理を追加
- Spring AI BOM 2.0.1を設定
- jsoup 1.23.2を設定
- `spring.ai.model.chat=none` により未使用AIモデルの自動構成を停止

## 追加テスト

`LinkCardRenderServiceTest` を追加し、以下をテスト対象としました。

- `<script>` を含む本文がサニタイズされること
- `onerror` などの危険属性が残らないこと
- サニタイズ済み本文とサーバー生成リンクカードを同時に表示できること
- `javascript:` リンクカードが実行可能HTMLにならないこと

既存の添付・質問・回答・会員情報のテストコードは維持しています。

## DB保護確認

今回の改善ではEntityのDBマッピング、テーブル、カラム、外部キー、既存PostgreSQLデータを変更していません。

## Gradleテスト

本作業環境では Gradle 8.14.5 本体を外部ネットワークから取得できないため、Gradle Wrapperによる実コンパイル・JUnit完走は実施できませんでした。

ローカル開発環境では以下を実行してください。

```bat
gradlew.bat clean test
gradlew.bat clean build
```

## ローカル回帰確認項目

1. アプリケーション起動
2. ログイン / ログアウト
3. 会員情報参照・編集
4. 質問一覧・検索
5. 質問登録
6. 画像付き質問登録
7. 質問修正・添付追加・添付削除
8. 質問削除
9. 回答登録・修正・削除
10. 回答へのいいね
11. Markdown表示
12. `<script>` 等を含む入力が実行されないこと
13. リンクカード表示
14. 不正スキームのリンクカードが拒否されること
15. ダーク / ライトテーマ切替
16. PC / モバイルでナビゲーション位置と一覧表示を確認
17. `/memos` がMFE画面として提供されないこと
18. 既存PostgreSQLデータの表示
