## 260102 JDK17からJDK21にアップグレード

## Java バージョンアップについて

本プロジェクトは、ローカル環境にて **Java 17 から Java 21 へのバージョンアップ**を行い、
ビルドおよび主要な機能が問題なく動作することを確認しました。

一方で、Spring Security において
deprecated かつ marked for removal とされている一部の API が使用されていることを確認しており、
将来的なバージョンアップ時に影響が出る可能性があります。

現時点では動作に問題はありませんが、
公式ドキュメントおよび推奨されている移行方法を確認のうえ、
順次修正対応を行う予定です。

## 260108 細かいところを修正しました。
 Java 17 から Java 21 へバージョンアップを行いながら、発生された細かい問題を解決しました。
 初日は、不要になったコードを整理と@Builder.Defaultの適用
 serialVersionUID宣言等々の小問題解決をしました。
 残っていることは少しずつ解決する予定です。
 
## 260108 Spring Boot 4 バージョンが正式化されました。 
 Spring Boot 4 バージョン正式化と共に現在維持されているこのゆるアーカイブも少しのアップグレードを考えなければならない局面に入りました。
 
 Spring Boot 4から求めている仕様はこのような形になっております。
 - **Java17でも使えますが、基本的にサポートしてくれるのは、Java21が基本になっております。**
 
   → 数日前にJavaをアップグレードさせたので、心配することはありません。
 - **gradleをつかっているなら、 8.15+ から 9.xバージョンが必要です。**
 
   → gradleは、安定化されたときに少しずつアップグレードします。  
 - mavenを利用する方は、https://docs.spring.io/spring-boot/tutorial/first-application/index.html **こちらの公式ホームページをご確認ください。**
 - ServerとServletは、自動的についてくるので、特に触ることはございません。
 
## 260109 細かいところを修正とリンクカード（Microlink API）の安定化対応をしました。

### Spring Security 設定の見直し
Spring Security 6.x への対応に伴い、`SecurityConfig.java` にて
非推奨（deprecated）となった API を排除し、設定構成を見直しました。

- AntPathRequestMatcher の使用を廃止
- 推奨されている新しい RequestMatcher 方式に統一
- AuthenticationManager 設定を明示的に構築し、fluent chain を排除

※ 本修正は挙動変更を目的としたものではなく、
既存の認証・認可ロジックはそのまま維持しています。

---

### リンクカード（Microlink API）の安定化対応
リンクカード機能では外部サービス Microlink API を利用していますが、
一部の URL において突発的な 400 / timeout が発生するケースが確認されました。

本プロジェクトでは以下の方針を採用しています。

- 外部 API の失敗は想定内の事象とする
- リンクカードは UI 補助機能（オプション機能）として扱う
- 外部 API 失敗時でも画面全体は必ず描画する

#### 対応内容
- Microlink 呼び出し形式を正規化（`https://api.microlink.io/?url=...`）
- 例外発生時は通常リンク表示へフォールバック
- Service / Controller の両方で例外を捕捉し、画面破壊を防止

この設計により、外部依存による不安定要素を吸収し、
安定した画面表示を優先する構成としています。
  

## License

This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.