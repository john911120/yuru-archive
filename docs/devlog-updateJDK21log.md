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


## License

This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.