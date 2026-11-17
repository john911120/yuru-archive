# セキュリティ設計

現在のゆるアーカイブが、どの脅威をどこで防いでいるかをまとめた資料です。対応の経緯は [変更履歴](../CHANGELOG.md) と [過去の記録](../history/README.md) を参照してください。

## 1. 概要

| 分類 | 主な対策 | 担当 |
|---|---|---|
| 認証 | フォームログイン、BCrypt | `SecurityConfig`、`UserSecurityService` |
| 認可 | URL 単位のログイン要求、投稿者本人の確認 | `SecurityConfig`、各 Controller |
| 総当たり対策 | ID＋接続元、接続元単位のログイン試行制限 | `LoginAttemptService`、`LoginRateLimitFilter` |
| セッション | パスワード変更時に他のセッションを失効 | `UserSessionService` |
| CSRF | すべての状態変更を POST にし、CSRF 保護を有効化 | Spring Security の既定 |
| XSS | 本文の安全化、例外メッセージの固定化、CSP | `CommonUtil`、`LinkCardRenderService`、`GlobalExceptionHandler` |
| SSRF | リンクカードの URL を公開ホストに限定 | `ExternalOgService` |
| ファイル | 画像の検証、パスの正規化、所有者確認 | `FileValidator`、`AttachService` |
| 負荷・DoS | 画像サイズ上限、本文長上限、リンクカード数上限 | 各 Form、`FileValidator`、`LinkCardRenderService` |
| ログ | 入力値の改行・制御文字の除去 | `LogSanitizer` |

## 2. 認証・認可

- Spring Security のフォームログインを使用します（ログイン画面 `/user/login`）。
- パスワードは `BCryptPasswordEncoder` でハッシュ化して保存します。
- 会員登録・パスワード変更のパスワードは、どちらも8〜100文字です。
- ログインが必要な URL は `SecurityConfig` でまとめて指定しています（[URL 一覧](ARCHITECTURE.md#3-url-一覧)）。それ以外は誰でも閲覧できます。
- 質問・回答の修正・削除では、Controller で投稿者本人かどうかを確認し、本人でなければ 403 を返します。
- 権限は全員 `ROLE_USER` です。ユーザー名で権限を決める処理はありません。管理者と誤解されやすいユーザー名（`admin*`、`administrator`、`root` など）は登録できません。

> 管理機能を追加する場合は、`site_user` に権限のカラムを追加し、DB の値から権限を決める設計にします（DB スキーマの変更を伴うため未実施）。

## 3. ログイン試行制限

| 単位 | 条件 | 制限 |
|---|---|---|
| ユーザー名＋接続元 IP | 10分間に5回失敗 | 10分間ログイン不可 |
| 接続元 IP | 10分間に20回失敗（ユーザー名を問わず合計） | 10分間ログイン不可 |

- 制限中のリクエストは、パスワードを照合する前に `LoginRateLimitFilter` が `/user/login?blocked` へ移動させます。
- ログインに成功すると、そのユーザーの記録は解除されます。IP 単位の記録は、1回の成功では解除しません（パスワードスプレー対策）。
- 記録は Caffeine のメモリキャッシュ（最大10万件、20分で自動削除）で管理するため、アプリケーションを再起動するとリセットされます。
- 接続元 IP は `request.getRemoteAddr()` を使います。同じグローバル IP を共有する環境では、IP 単位の制限も共有されます。

## 4. セッション

- 同じアカウントで複数のブラウザから同時にログインできます。セッションは `SessionRegistry` で管理します。
- パスワードを変更すると、操作中のセッションは維持し、同じアカウントの他のセッションを失効させます。失効したセッションで操作すると `/user/login?expired` へ移動します。
- ログアウトは POST `/user/logout` のみで、セッションを破棄し `JSESSIONID` を削除します。

## 5. CSRF

- 状態を変更する操作（質問・回答の削除、いいね、ログアウトなど）はすべて POST です。
- CSRF 保護は全エンドポイントで有効です。除外している URL はありません（2026-11-03 に H2 Console の除外を撤去）。

## 6. XSS 対策

### 本文の表示

```text
利用者の本文 → CommonMark で HTML へ → jsoup（Safelist.basicWithImages）で安全化 → 画面へ
```

- 質問本文は `LinkCardRenderService` が、リンクカード部分とそれ以外に分けて処理します。リンクカード以外は必ず上の経路を通り、リンクカードはサーバー側のテンプレートで生成します。
- 回答本文は `CommonUtil.markdown()` で同じ経路を通します。
- 利用者の入力をそのまま `th:utext` に渡す経路はありません。
- 見出し（`h1`〜`h6`）や `script`、イベント属性（`onerror` など）、`iframe`、`style` は削除されます。

### エラー応答

- 例外の応答は `text/plain` で返し、利用者の入力値を含めません。型の不一致や必須パラメーター不足は「リクエストの内容が正しくありません。」の固定文言です（反射型 XSS 対策）。

### Content-Security-Policy

```text
default-src 'self';
script-src 'self' 'unsafe-inline';
style-src 'self' 'unsafe-inline';
img-src 'self' data: https:;
font-src 'self';
connect-src 'self' https://zipcloud.ibsnet.co.jp;
object-src 'none';
base-uri 'self';
form-action 'self';
frame-ancestors 'self'
```

- 画像の `https:` は、Markdown の外部画像とリンクカードのため、`connect-src` の ZipCloud は郵便番号検索のために許可しています。
- テンプレートにインラインの `<script>`・`onclick`・`style` 属性が残っているため、現在は `'unsafe-inline'` を許可しています。
- あわせて `X-Frame-Options: SAMEORIGIN` を返します。

## 7. リンクカード（SSRF 対策）

リンクカードのためにサーバーから外部へアクセスするので、内部ネットワークへのアクセスに悪用されないよう URL を制限します。

| 許可 | 拒否 |
|---|---|
| `http` / `https` で、名前解決したすべてのアドレスが公開ネットワーク上のホスト | `javascript:` などのスキーム、ユーザー情報付き URL（`user@host`）、ワイルドカード（`0.0.0.0`）、loopback、private / site-local、link-local（`169.254.169.254` などを含む）、名前解決できないホスト |

- 拒否した URL は、クリックできるリンクとしても出力しません。
- 外部 API（Microlink）の呼び出しは5秒でタイムアウトし、結果を10分間キャッシュします。
- 1投稿で表示するリンクカードは5件までです（1件ごとに外部呼び出しが発生するため）。

## 8. 添付ファイル

- 拡張子・Content-Type・ファイルシグネチャ・デコード可否・画像の大きさを確認します（詳細は [DB・添付ファイル](DATABASE_AND_FILES.md#受け付ける画像)）。
- 画像の大きさは、デコード前にヘッダーから確認します（Decompression bomb 対策）。
- 元のファイル名は basename に正規化し、保存先を正規化したうえでアップロード領域の外を指すパスを拒否します。
- 削除はファイル ID・質問 ID・会員 ID がすべて一致する場合だけ行います。
- 質問を削除すると、添付ファイルの実体も削除します（削除後の URL は 404）。
- `/upload/**` はログインなしで閲覧できます。非公開にしたい画像を扱う用途は想定していません。

## 9. 入力値の上限

| 対象 | 上限 |
|---|---|
| 質問タイトル / 本文 | 200文字 / 20,000文字 |
| 回答本文 | 10,000文字 |
| ページ番号 | 負の値は0として扱う |
| アップロード | 1リクエスト10MB、縦横8,000px、総ピクセル数4,000万 |

## 10. HTTP ステータス

| 状況 | ステータス |
|---|---|
| 不正な入力、型の不一致、受け付けないファイル | 400 Bad Request |
| 未ログインの Ajax（いいね） | 401 Unauthorized |
| ログイン済みだが権限がない | 403 Forbidden |
| データ・ページが存在しない | 404 Not Found |
| POST 専用 URL への GET など | 405 Method Not Allowed |
| 同じ回答への2回目のいいね | 409 Conflict |
| 想定外のエラー | 500 Internal Server Error |

## 11. 依存関係と実行環境

- Spring Boot・Tomcat・JDK などの脆弱性情報は、実際の利用条件と照らして影響を評価したうえで、修正版へ更新します。手順は [保守ガイド](MAINTENANCE.md) を参照してください。
- リンクカードの Microlink 呼び出しは HTTPS のため、JDK の TLS 実装（JSSE）と証明書ストアを使用します。JDK の TLS・証明書関連の修正は、本アプリケーションにも影響します。
- API キーなどの秘密情報はリポジトリに含めず、環境変数で渡します（例：`ANTHROPIC_API_KEY`）。

## 12. 残っている課題

| 課題 | 対応方針 |
|---|---|
| CSP で `'unsafe-inline'` を許可している | スクリプトを外部ファイル化し、nonce 方式へ移行する |
| 管理者権限の設計がない | 管理機能を追加する時点で、DB の権限カラムを設計する |
| ログイン試行の記録が再起動で消える | 現状の規模では許容。複数台構成にする場合は共有ストアを検討 |
| リンクカードの URL 判定が Java の標準判定（`isSiteLocalAddress` など）に依存している | IPv6 のユニークローカルアドレス（`fc00::/7`）や `100.64.0.0/10` は判定の対象外。IPv6 の内部ネットワークがある環境に置く場合は判定を追加する |

その他の課題は [保守ガイド](MAINTENANCE.md#既知の課題) にまとめています。
