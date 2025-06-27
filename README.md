# yuru-archive
![YuruArchive Logo](assets/yuruArchieve_Logo.png)

## プロジェクト説明
🚀 ゆるアーカイブは、ある週末の夜、  
ふと「質問掲示板ってもっと気軽にできないかな」と思ったことから始まりました。

Spring Boot × PostgreSQLをベースに、  
まだ発展途中ながらも、ログイン・投稿・回答など基本機能を少しずつ組み立てています。

設計から実装まですべて一人でこつこつ進めています。  
「ゆるく、でもちゃんと残る」アーカイブを目指して。

実はこのプロジェクト名には、  
私が日頃楽しんでいるモバイルゲーム「ブルーアーカイブ」からの  
ささやかなインスピレーションも込められています。

**「日常に、小さな奇跡を。」**  
その言葉のように、  
このプロジェクトも、誰かの小さな気づきや発見に繋がればという気持ちで作っています。

🌱 よかったらそっと見守ってください。

## ✅ 開発完了：2025年6月26日
## 🎯 現在のフェーズ：**DevOps環境への移行準備中**
## ✅ DevOps開始日 : 2025年6月27日
詳細な開発環境と設定ドキュメントは [docs/README.md](./docs/README.md) をご覧ください。

🔧 Spring Boot 3.2.12へマイナーアップデート
- DoS対策（CVE-2024-22262等）を含む安定版
- 依存関係の安全性向上のため、3.2.1 → 3.2.12に更新
- build.gradleのpluginバージョンを修正

✅ 開発端末2台3.2.12動作確認完了(2025年6月27日)

📄 README構成をDevOps対応に整理
- メインREADMEはプロジェクト紹介＋DevOpsフェーズ記載に変更
- 詳細な開発環境ドキュメントをdoc/README.mdに移動
- CI/CD設計用のdevops.md作成予定

---------------------------------------------------------------------------------
## 📁 ドキュメント一覧

- 開発に関する詳細：[docs/README.md](./docs/README.md)
- CI/CD・運用設計：[docs/devops.md](./docs/devops.md)（作成予定・近日中に反映予定）


## License
This project is **NOT open source**.  
© 2025 John Dev – All rights reserved.  
Commercial use without prior written permission is strictly prohibited.