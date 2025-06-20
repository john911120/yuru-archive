### Ubuntu環境におけるLombok設定手順

このプロジェクトは、WindowsおよびUbuntuの両環境でLombokを使用するために、以下の設定を行いました。

### ✅ 実施内容

- GradleプロジェクトにLombok依存関係を追加
- Ubuntu上で`./gradlew build -x test`を実行して依存関係を取得
- `lombok.jar`を公式サイトよりダウンロードし、Spring Tool Suite（STS）に手動で適用
- Eclipse/STS上でアノテーションプロセッサーを有効化し、Lombokが正しく機能することを確認


## Ubuntu環境におけるLombok設定について

本プロジェクトは、WindowsおよびUbuntuの両環境に対応しています。Ubuntu環境においては、Lombokの導入と設定にいくつかの追加手順が必要となるため、以下にその内容を記載いたします。

### 🛠 実施内容の概要

- GradleベースのプロジェクトにLombokの依存関係を追加
- `./gradlew build -x test` によりテストをスキップしつつ依存関係を取得
- `lombok.jar` を用いて、Spring Tool Suite（STS）への手動設定を実施
- Annotation Processorを有効化し、Lombokアノテーションの認識を確認
- Windows環境でもGit Pull後、設定に問題がないことを確認済み

### 📦 `build.gradle` に追加した依存関係
```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'
}
```

### ⚙ Lombokの導入手順（Ubuntu）
1. 公式サイトより `lombok.jar` をダウンロード  
   → https://projectlombok.org/download
2. ターミナルで以下のコマンドを実行
   ```bash
   java -jar lombok.jar
   ```
3. GUIが起動したら、STSの実行ファイルを指定（例：`/home/vboxuser/sts-4.29.1.RELEASE/SpringToolSuite4`）
4. 「Install/Update」をクリックして設定を反映
5. STSを再起動し、Lombokのアノテーションが正しく機能するか確認

### ✅ 動作確認済み項目
- `@Getter`, `@RequiredArgsConstructor` などのアノテーションにエラーが表示されないこと
- Windows環境で `git pull` 後も同様に正常動作すること
- ビルド時にエラーが発生しないこと（Ubuntu/Windows共に）
---
本手順により、開発環境のOSに依存せず、Lombokを安定的に利用できる状態となりました。

## License

This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.