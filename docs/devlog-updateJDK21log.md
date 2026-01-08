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
  

## License

This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.