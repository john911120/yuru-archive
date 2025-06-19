### 💡 トラブルメモ: 「いいね」機能で地獄 250527
- Spring Securityのリダイレクト地獄
- fetch()のcredentials: "include" 抜けで403
- preventDefault()忘れて謎のページ遷移
- すべて解決し、平常通りに作動します。

## 🔒 未解決の課題：Answerの「いいね」数が反映されない問題(250527~250528)

### ✅ 症状
- 「いいね」ボタンをクリックすると、`answers_voter`テーブルには正常に反映される
- しかし、フロント画面上では数値が更新されない（常に1のまま）

### ✅ 試した対応策
1. `AnswerService.getAnswer()` → `findById()` による再取得に変更
2. `@Transactional` + `entityManager.flush()` を明示的に実行
3. `/voter-count` API によるAjax更新処理
4. `setTimeout()` を使ってトランザクションコミットを待つ
5. ログやDBのトレースをすべて確認済み

### 🔍 想定される原因
- JPAの永続コンテキストによるキャッシュの反映遅延
- トランザクションのコミットと取得のタイミングずれ
- `@ManyToMany` 関係の更新遅延
- Ajaxで取得するタイミングが早すぎて最新の値が取れない

---

📌 この課題は一時保留とします。  
🕰 日を改めて再検討・再挑戦する予定です。

### 💡 トラブルメモ: 「いいね」機能トラブル対応完了 250602
経緯
1. 回答に「おすすめ」ボタン機能を追加
✨ AnswerControllerに /vote/{id} エンドポイント実装
✨ サーバー側で重複チェック・本人投票の禁止処理
✨ Question詳細ページにボタン表示を追加

2. Ajaxによる非同期更新処理
⚡ Ajaxで「おすすめ」数をリアルタイム更新
⚡ JavaScriptでfetch APIを用いた非同期通信を実装
⚡ voter-count APIからいいね数を再取得
⚡ 成功時はバッジの数値を即時更新

3. エラーメッセージとJS処理修正
🐛 alertが動作しない不具合を修正
🐛 fetchレスポンスのtextパース順序を修正
🐛 エラー発生時にalertとconsole.errorを表示
🐛 href属性を除去し、data-uri＋preventDefaultで挙動安定化 	

4. テスト用コードと一時的な切り分け
🧪 テスト用コードを仮ブランチで分離
🧪 mainブランチに影響しないよう切替用コード作成
🧪 動作検証用にsetTimeoutでタイミング調整を実施

5. 最終整備およびREADME更新
📝 解決までのプロセスを記録
📝 コードスニペット、学び、動画記録のまとめを含む

📽️ **録画デモを見る[⚡ダウンロードのみ]**：  
👉 [トラブルシューティング動画を再生 (ローカル環境用)]：[download] (../assets/Vlog/Like_ButtonTroubleShooting.mp4)

### ♻️ Answerのいいね数が更新されない問題の修正（2025/06/03）

- @Query("SELECT size(a.voter)...") を用いてJPAのキャッシュ問題を回避
- voter-countの取得APIをAnswerControllerにて更新
- fetchによるAjax更新時の数値反映タイミング改善（反映遅延の根本対処）
- flush()の明示的な適用により即時反映を狙うが効果限定的
- DOM操作によるいいねボタン状態の反映ロジックは維持
- トラブルシューティングの結果、実装上の限界と判断し、現状仕様でFIXとする

### ⚠️ 仕様上の制限（2025/06/03 時点）
現在の「いいね」機能は、Ajaxを用いて数値の即時更新を試みていますが、
JPAの永続コンテキストやトランザクションのタイミングの都合により、
リアルタイムでの正確な反映には一定の遅延が発生する場合があります。


この問題に対して、以下のような対応を行いました：

⚠️ @Query("SELECT size(...)") によるキャッシュの回避

⚠️ EntityManager.flush() を活用した即時反映の試行

⚠️ Ajaxによる再取得・再描画の最適化

上記すべての施策を講じた上で、現時点では仕様上の限界と判断し、
本機能は 一時的にこの仕様でFIX いたしました。

📌 将来的にはRedisやLike用の非同期処理の導入など、さらなる改善を検討予定です。

💡 現状でも基本的な動作には支障がなく、十分なユーザビリティを確保しております。


## License

This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.