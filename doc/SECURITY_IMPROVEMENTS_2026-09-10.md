# 2026-09 セキュリティ改善記録

## 目的

2026年9月に実施したWebプロジェクト全体の脆弱性確認結果をもとに、`yuruArchive_Local` で確認された潜在的な攻撃経路と依存関係の世代不整合を整理・改善しました。

本対応では、既存DBスキーマと主要なQ&A機能を維持しながら、不要な公開エンドポイントの削除、HTML表示経路の安全化、依存ライブラリ更新を行っています。

## 1. legacy AttachController の撤去

### 改善前

未使用の `AttachController` に以下の旧エンドポイントが残っていました。

- `/attach/display`
- `/attach/remove`
- `/attach/upload`

特に `/attach/display` と `/attach/remove` は、リクエストから受け取った `fileName` をファイルシステムのパス生成に利用する旧構造でした。

現在の画面では、質問・回答の添付処理は `QuestionController` / `AnswerController` から `AttachService` を利用する経路へ移行済みであり、これらのエンドポイントは使用されていませんでした。

### 改善後

- `AttachController` を削除
- `AttachService.deleteFile(String fileName)` を削除
- `AttachService.getUploadPath()` を削除
- IDベースの `deleteFileById(Long fileId)` を継続利用

これにより、利用者が任意のファイル名を直接指定する旧ファイル取得・削除経路を撤去しました。

## 2. 添付ファイル処理の追加防御

legacy endpoint の削除に加えて、現在利用中の添付処理も防御を追加しました。

### アップロード

- ブラウザから受け取る元ファイル名をbasenameへ正規化
- `../` やディレクトリ部分を保存ファイル名として使用しない
- 保存先を `normalize()` したうえで、対象パスがアップロード用ディレクトリ内にあることを確認
- JPG / JPEG / PNG / GIF / WebP のみ許可
- Content-Type が画像であることを確認

### 削除

- DBに保存されたファイル情報からパスを再構築
- アップロードルートを基準に `normalize()`
- ルート外を参照するパスは拒否

## 3. Question本文のStored XSS対策

### 改善前

質問本文では `LinkCardRenderService` が元の投稿本文をほぼそのまま保持したHTML文字列を生成し、`question_detail.html` が `th:utext="${htmlBody}"` で表示していました。

リンクカード以外の部分にユーザー入力HTMLが含まれた場合、サニタイズ経路を迂回する可能性がある構造でした。

### 改善後

`LinkCardRenderService` の処理を以下へ変更しました。

```text
ユーザー投稿本文
      ↓
リンクカードshortcode単位に分割
      ↓
通常本文: CommonMark → jsoup Cleaner
      ↓
リンクカード: サーバー側テンプレートで生成
      ↓
安全化済みHTMLのみ question_detail.html へ渡す
```

これにより、生のユーザー投稿本文を直接 `th:utext` へ渡さない構造に変更しました。

回答本文についても従来どおり `CommonUtil.markdown()` を利用し、CommonMark変換後にjsoupでサニタイズします。

## 4. リンクカードURL検証

リンクカードは以下のURLのみ許可します。

- `http`
- `https`
- 公開ネットワーク上のホスト

以下は拒否します。

- `javascript:` などの非HTTPスキーム
- user-infoを含むURL
- loopbackアドレス
- site-local / privateアドレス
- link-localアドレス
- ホスト解決に失敗するURL

不正URLは通常リンクへのフォールバック処理へ流さず、画面上では無効なリンクとして扱います。

## 5. jsoup

### 変更

```text
1.17.2 → 1.23.2
```

従来コードは `Safelist.basicWithImages()` という組み込みSafelistを使用しており、2026年に公開されたcustom raw-text Safelist条件の問題には直接該当していませんでした。

ただし、依存関係の警告範囲を残さず、今後の保守性も考慮して修正版へ更新しました。

## 6. Spring AI

### 変更

```text
Spring AI BOM 1.1.8 → 2.0.1
```

本プロジェクトは Spring Boot 4.1.1 を使用しているため、Spring AIも2.0系へ揃えました。

現時点ではAI機能を実装していないため、以下の設定でChatModelの自動構成を停止します。

```properties
spring.ai.model.chat=none
```

将来AI機能を実装する場合は、別フェーズで明示的に有効化します。

## 7. MFE撤去確認

MFE機能の撤去後、実行構成から以下が存在しないことを確認しました。

- Vue / Viteプロジェクト
- `package.json`
- `package-lock.json`
- `src/main/resources/static/memo/**`
- `/memos/**` ルーティング
- `MemoResourceConfig`
- navbar上のMFE移動リンク

`docs/` や `assets/` に過去の開発記録・画面資料としてMFEという名称が残る場合がありますが、実行機能・依存関係・配信リソースではありません。

## 8. DB影響

今回の改善では以下を変更していません。

- PostgreSQLテーブル
- カラム
- 外部キー
- EntityのDBマッピング
- 既存データ

セキュリティ改善はWeb層、ファイル処理、HTML表示処理、依存関係を中心に実施しています。
