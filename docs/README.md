# yuru-archive
![YuruArchive Logo](../assets/yuruArchieve_Logo.png)

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

## 📈 開発記録 / Development Log

- 総コミット数: **97回**
- 開発期間: **2025年5月1日 ～ 6月26日**（約2ヶ月/ 56日）
- 機能追加・バグ修正・UI改善を含め、毎日こつこつ積み重ねました。
- 設計から実装・修正・完成まで、全て一人で対応。

> ⛰️ まるで山を一歩ずつ登るような感覚でした。
> 最後には、頂上からの景色が見えました。


## 🖥️ メイン画面（PC・モバイル）
# PCバージョン
![PC](assets/ゆるアーカイブPC版メイン画面.png) 


# モバイルバージョン
![Flex](assets/ゆるアーカイブflexibleメイン画面.png)

📍一部のスマートフォン・タブレット機種によって異なって見えるかもしれないので、ご了承ください。

## 🔧 主な機能一覧

| 🛠 機能 | 💡 概要 |
|--------|--------|
| 質問・回答投稿機能 | 登録済ユーザによるQ&A作成、編集、削除 |
| 添付ファイル | 画像アップロード＋自動サムネイル生成 |
| タグ・カテゴリ機能 | 質問の分類・管理 |
| 投票（いいね）機能 | 回答に対するAjaxベースの評価機能 |
| ダークモード | Bootstrap 5.3 のテーマ切替に対応 |
| 日本住所API | 郵便番号による自動入力（ZipCloud） |

## 技術スタック
- Java 17.0.6 → 21.0.2 → 21.0.11 / Spring Boot 3.5.6 → 3.5.9 → 3.5.16（3.5.xバージョンUpdate Patch）
- PostgreSQL 16 / H2 Database（テスト用）
- Thymeleaf / Bootstrap 5.3
- Docker / Docker Compose
- Thumbnailator（画像サムネイル）
- 郵便番号API：[ZipCloud](https://zipcloud.ibsnet.co.jp/doc/api)


## 特徴
- 質問・回答の投稿、編集、削除機能
- カテゴリ別の質問管理
- タグ機能
- 日本向け郵便番号APIによる住所自動入力
- 電話番号日本仕様のバリデーション対応
- Dockerを使用したローカル環境構築

## 📂 ファイルアップロード構成
com.yuru.archive.attach
├── controller
├── dto
├── entity
├── repository
└── service

✅ 詳細仕様（アップロード・削除・DB保存・GitHub URL対応など）  
📖 → README下部に詳細あり、または別ファイル参照

## 🔍 開発メモ・技術ノート（詳細）
開発環境の構築やトラブル対応の記録については、以下の文書をご参照ください。

| カテゴリ | ドキュメント |
|----------|--------------|
| 2026年JDK17→JDK21 | [updateJDK21log.md](devlog-updateJDK21log.md) |
| 2025年〆メッセージ | [closing_2025.md](closing_2025.md) |
| SNSのリンクプリビュー機能 | [devlog-SNS.md](devlog-SNS.md) |
| 🚀**DevOps アップグレード履歴** | [devops_upgrade.md](devops_upgrade.md) |
| Lombok設定（Ubuntu） | [devlog-lombok.md](devlog-lombok.md) |
| PostgreSQLリモート接続 | [devlog-postgres-remote.md](devlog-postgres-remote.md) |
| 添付ファイルの構成と仕様 | [devlog-fileupload.md](devlog-fileupload.md) |
| ダークモード切替の実装 | [devlog-darkmode.md](devlog-darkmode.md) |
|「いいね」機能の不具合対応 | [devlog-like-troubleshoot.md](devlog-like-troubleshoot.md) |
| 日本住所API構成と仕様 | [devlog-Japan Address API.md](devlog-Japan_Address_API.md) |
| spring-security Auth | [devlog-springSecurity.md](devlog-springSecurity.md) |
| Database設計 | [devlog-database.md](devlog-database.md) |
| 時間帯に合わせてログインする時に挨拶出力機能 | [devlog-greeting_Japan.md](devlog-greeting_Japan.md) |
| MFE統合ステップ1 | [MFE_Integration_Step1.md](MFE_Integration_Step1.md) |
| MFE統合ステップ1_Plus | [MFE_Integration_Step1_Plus.md](MFE_Integration_Step1_Plus.md) |
| 🚨 開発メモ：バグと修正履歴 | [devlog-troubleshootetc.md](devlog-troubleshootetc.md) |


## License
This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.