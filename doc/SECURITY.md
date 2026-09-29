# セキュリティ

## 認証

Spring Securityによるフォームログインを使用します。

認証が必要な主な操作:

- 質問作成
- 質問修正・削除
- 回答作成
- いいね
- 会員情報参照・編集

投稿の修正・削除ではController側でも投稿者を確認します。

## パスワード

`BCryptPasswordEncoder` を使用します。

## Markdown / HTML

ユーザー本文はCommonMarkでHTMLへ変換した後、jsoup `Safelist.basicWithImages()` でサニタイズします。

質問本文もリンクカード部分を除き必ず同じサニタイズ経路を通過し、生の投稿本文を直接 `th:utext` へ渡しません。

jsoupは1.23.2を使用します。

## リンクカード

外部URLは `http` / `https` の公開ホストのみ許可します。

以下は拒否します。

- loopback
- private / site-local
- link-local
- user-info付きURL
- 非HTTPスキーム
- 名前解決できないホスト

無効URLはクリック可能なフォールバックリンクとして出力しません。

## 添付ファイル

未使用だったlegacy `AttachController` は撤去しました。

現在の添付処理は `AttachService` に集約し、以下を実施します。

- 拡張子確認
- Content-Type確認
- 元ファイル名のbasename化
- UUID付きファイル名で保存
- 保存先パスのnormalize
- アップロード領域外パスの拒否
- IDベースの削除

## CSRF

既存互換性のため一部パスをCSRF除外しています。既存挙動を維持しながら、今後も不要な除外経路を段階的に見直します。

## 詳細

2026年9月の改善内容は [SECURITY_IMPROVEMENTS_2026-09-10.md](SECURITY_IMPROVEMENTS_2026-09-10.md) を参照してください。
