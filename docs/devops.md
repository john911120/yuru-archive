## 📡 外部API

- 使用API：日本住所検索API（郵便番号API）
- 利用目的：ユーザー登録時の住所補完機能


🔧 Spring Boot 3.2.12へマイナーアップデート (250627)
- DoS対策（CVE-2024-22262等）を含む安定版
- 依存関係の安全性向上のため、3.2.1 → 3.2.12に更新
- build.gradleのpluginバージョンを修正


## 🇯🇵 DevOps記録（コメント数表示機能の追加)
📅 日付: 2025年7月3日（木）


🖼 実際の画面: <br>
![PC表示例](../assets/コメントの数機能追加.png)

🛠 作業内容:
- 投稿一覧画面に「コメント数（回答数）」を表示する機能を追加（Plan A方式）
- AnswerRepository に `countByQuestion` メソッドを追加
- QuestionController で質問ごとのコメント数をカウントし、Map形式でテンプレートへ渡すように修正
- question_list.html にコメント数表示を追加
- Thymeleaf上での SpELエラー（null参照）を回避するため、nullチェック条件を追加

🕒 作業時間:
- 想定: 約2時間  
- 実績: 約40分（前日に処理方針を事前に設計したため、スムーズに対応）

🐛 発生した問題と対応:
- テンプレート側で `answerCountMap[question.id]` に null が渡されたことにより、SpELパースエラーが発生
- 原因をログから特定し、Controllerで `model.addAttribute()` が不足していたことを確認
- nullチェック付きの条件式で安全に回避完了

📌 今後の対応検討:
- 質問数が多くなった場合、JPQL＋DTOによるPlan B（最適化構成）への移行も視野に入れる
- 上部固定の「お知らせ投稿」機能の追加要望に備えた設計も検討中

## License
This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.