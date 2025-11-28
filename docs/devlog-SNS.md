## 20251110 各種SNSのリンクプリビュー機能追加（開発中です。）
Microlink + Cloudinary + medium-zoom + Autolinker + セキュリティ対策を含み開発をしておりました。

**Develop Environment**  
- Java 17(after Upgrade for Java 21)  
- Spring Boot 3.5.x  
- Thymeleaf 3.x  
- Bootstrap 5.3.2  
- Microlink / Cloudinary API  


 **※ 開発の流れ**
 
 1. Webflux, caffeineを使って、キャッシュを柔軟に対応できるようにしました。
 2. OGP外部呼び出し+ セキュリティの検証 + Cloudinary JPGを強制的に変換する。
 3. コントローラーは、DB無しにURLのみを受信し、カード方式にレンダリングします。
 4. thymeleaf templateを作ります。 
 5. Global Security Header(CSP etc..)
  
📝 **補足内容**：
 	<dl>
		<dd> 1. Spring Security無しに簡単にフィルター適応もできるし、Securityを使っているなら、SecurityFilterChainからヘッダーを指定しても構わない。</dd>
		<dd> 2. 必要なドメインだけを開けておくことが原則であり、unpkgやjsDelivrを使わずに、自体的にホスティングをすれば、CSPをもっときつく減らせることが可能。</dd>
		<dd> 3. bootstrap + イメージ拡大機能 + 自動的にリンク化する</dd> 
	</dl>
📝 **期待できるところや使う方法**：

	ページへ移動:
	→ GET /cards/preview?url=https://example.org/some-article
	→ Microlinkが メタデータを受信し、イメージは Cloudinary fetch + f_jpgに JPG拡張子を固定します。	
	→ イメージをクリックしたら拡大されており, 説明の中で、URL/@handleは、自動的にリンク化されて、 “原文を開く”機能は、新しいタップで開かれます。

🕵️‍♀️ **  セキュリティ/devops Check Point **
	<dl>
		<dd> 1. Microlink 呼び出しはサバー側のみ(Key/quarter Security), 応答フィルダー長さ制限及びnullのロールバック</dd>
		<dd> 2. Cloudinary fetch domain allowlist設定をお勧め(アカウントセキュリティ設定) + 必要な時にはサインする形のURL.</dd>
		<dd> 3. network Timeout(5second), result**TTLcache(10minute)**で、コストとディレイを緩和できます。</dd>
		<dd> 4. テキストは、基本的にescape, Autolinkerに sanitizeHtml: trueと危険なスキームを遮ることできます。</dd>
		<dd> 5. CSP/Referrer/nosniff/HSTS(production)Applyed.</dd>
	</dl>

## 20251128 各種SNSのリンクプリビュー機能追加（開発完了です。）
 - 開発期間 : 総18日

## 📘 リンクカード機能の実装記録（開発ログ）

## 🚀 概要
本プロジェクトでは、**質問投稿内の URL を自動解析し、OGP(Open Graph Protocol) を基に「リンクカード」を生成する機能**を実装しました。  
Markdown の表現力を損なわず、ユーザー体験を向上させることを目的としています。

本機能により、以下のような記述が可能になります：

``[[linkcard url="https://example.com"]]``
 

---

## 🧩 機能仕様

### ✔ 1. OGP メタデータの取得
- 指定 URL へサーバー側からアクセスし、以下の OGP 情報を解析  
  - `og:title`  
  - `og:description`  
  - `og:image`  
  - `og:url`  

取得したデータは `OgMetadata` としてコントローラに渡されます。

---

### ✔ 2. テンプレートレンダリング
- `card.html` にてリンクカード用テンプレートを作成  
- `Thymeleaf TemplateEngine` を用いて HTML として描画  
- **DB の question.content はそのまま保持し、画面描画時のみリンクカードを反映**  
　（Markdown と干渉しない安全な構造設計）

---

### ✔ 3. フォールバック処理（Graceful Degradation）

外部サーバーが画像リンクをブロックしたり、CORS、404 を返すケースに備え、  
以下のフェイルセーフ処理を実装：

- 画像取得エラー → `<img>` タグを自動削除する  
- ダウンロードボタンも同時に非表示  
- タイトル / 説明 / URL は正常表示  
- UI の崩壊を完全防止

---

## 🎨 UI / レイアウト改善

### ✔ カード本体スタイル
```
<div th:fragment="linkCard(og)"
     class="card shadow link-card"
     style="width:100%; max-width:min(1060px, 90vw); margin-top:1.6rem; margin-bottom:1.4rem;">
```
 - PC・モバイル両対応
 - 最大幅を柔軟に調整（min(1060px, 90vw)）
 - 適切な余白で読みやすさ向上

## ✔ 画像処理
```
<img th:if="${og.image != null}" th:src="${og.image}"
     class="card-img-top zoomable"
     style="max-width:420px; width:100%; margin:0;"
     alt="preview"
     onerror="this.remove()">
```
 - 画像取得エラー時は 即時削除
 - CORS でも崩れないカード構成

## 🧪 question_detail.html への統合
リンクカードが存在する場合は htmlBody を優先的に描画：

```<!-- リンクカードを優先表示 -->
<div th:if="${htmlBody != null}"
     th:utext="${htmlBody}">
</div>

<!-- 無ければ通常の Markdown 出力 -->
<div th:if="${htmlBody == null}"
     th:utext="${@commonUtil.markdown(question.content)}">
</div>
```
Markdown の二重描画を防ぐため、旧ロジックは整理済み。

## ✔ 開発済みの画面
![ゆるアーカイブSNS画面](../assets/ゆるアーカイブSNS画面.jpg)
- 画像を提供するサーバー側で **404 エラー** が発生した場合、  
  プレビュー画像の `<img>` タグを削除し、**ダウンロードボタンも自動的に非表示** にします。
- 404 エラーが発生しなければ、プレビュー画像を表示し、  
  あわせて **JPG ダウンロードボタンも有効** な状態で表示されます。
 
## 🔄 バックアッププロジェクトについて（補足）
リンクカード開発中の破損リスクに備え、
バックアッププロジェクト側にも同等のリンクカード処理を実装済みです。

含まれている実装：
 - OGP 解析ロジック
 - TemplateEngine レンダリング
 - card.html のリンクカード fragment
 - question_detail 相当の統合ロジック（htmlBody 優先描画）

#🔹 万が一メインプロジェクトに不具合が発生した場合は、バックアップ側のコードを参考実装として利用できます。

# 🏁 完成度と総評
今回のリンクカード実装は、UI / 安定性 / データ整合性 を高レベルで満たした完成度の高い仕上がりになりました。

# 📌 達成したポイント
✔ URL → OGP → リンクカード自動生成

✔ Markdown を壊さない安全な仕組み

✔ 外部ホストの画像エラーに強い堅牢設計

✔ PC / モバイル両方で自然なレイアウト

✔ バックアッププロジェクトにも実装済み（参照性◎）

# 今後は、キャッシュ化・複数 URL 対応などへ発展できます。

 
## License
This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.