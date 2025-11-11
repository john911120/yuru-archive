## 20251110 DisCordや各種のSNSのリンクをつけて移動できるような機能追加（開発中です。）
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

## License
This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.