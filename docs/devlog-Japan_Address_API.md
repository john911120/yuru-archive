## 日本郵便番号APIによる住所自動入力機能を追加

日本のユーザー向けに、郵便番号を入力することで都道府県・市区町村・町名が自動的に補完される機能を実装しました。  
郵便番号APIとして [zipcloud](https://zipcloud.ibsnet.co.jp/doc/api) を利用しています。

### 🛠 実装内容の概要
- ユーザー登録画面に郵便番号フィールドを追加
- 7桁またはハイフン付き形式（例：1234567、123-4567）に対応
- API連携によって `address1`（都道府県）、`address2`（市区町村）、`address3`（町名）を自動入力
- 入力値の整合性を `@Pattern` アノテーションでサーバー側でも厳密にチェック
- DBにはハイフンを除いた正規化済みの郵便番号を保存

### 💡 ユーザー体験の向上
- 入力の手間を減らし、正確な住所データの取得を実現
- 無効な郵便番号に対しては、フロントエンドとバックエンドの両面で即時フィードバックを提供


## ✨ 250526追加機能及び改選事項
1. 詳細住所入力欄（addressDetail）の追加 > テスト完了
2. 認証ロジック自体は正しく実装されていましたが、Spring Security側で認識されない設定になっていたため、修正が必要でした。
3. SecurityConfig内で`.userDetailsService(...)`と`.passwordEncoder(...)`の設定が抜けていたため、認証処理に反映されていませんでした。
4. ログインフォームのPOST送信先URL(`/user/login`)に対し、`.loginProcessingUrl(...)`の明示的な指定がなかったため、認証フローが起動しませんでした。
5. 以上の原因を1つずつ特定し、設定を修正することで、ログイン認証のフローが正常に機能することを確認しました。


## License

This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.	