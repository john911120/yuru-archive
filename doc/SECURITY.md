# セキュリティ

## 認証

Spring Securityによるフォームログインを使用します。

認証が必要な主な操作:

- 質問作成
- 質問修正・削除
- 回答作成
- いいね

投稿の修正・削除ではController側でも投稿者を確認します。

## パスワード

`BCryptPasswordEncoder` を使用します。

## Markdown

CommonMarkでHTMLへ変換した後、jsoup `Safelist.basicWithImages()` でサニタイズします。

## リンクカード

外部URLは `http` / `https` のみ許可し、ループバック・プライベート・リンクローカルアドレスを拒否します。

## CSRF

既存互換性のため一部パスをCSRF除外しています。0.1では既存挙動を優先し、大幅なSecurity設計変更は行っていません。
