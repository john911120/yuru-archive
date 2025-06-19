## 250517 一部のファイルにダークモードとライトモード転換機能追加（テスト）

テストの目的に一部のファイルにダークモードとライトモード転換機能を追加しました。

- `question_list.html` にダークモード機能の動作確認を目的としたテスト実装を追加しました。
- テーマ切り替えボタンおよび `localStorage` を使用した状態保存処理を含みます。
- 他のページにはまだ適用されていません。

📝 **補足**：この実装は後に `layout.html` に統合され、全体適用されました（→ 2025/05/22 の更新を参照）。



## 250522 一部のファイルにダークモードとライトモード転換機能追加完了

### 🌙 Bootstrap 5.3 テーマ切替機能の導入

本プロジェクトでは、[Bootstrap 5.3](https://getbootstrap.jp/docs/5.3/) の `data-bs-theme` 属性を利用して、  
**ライトモードとダークモードの切替機能**を実装しています。

---

### 🔧 主な対応内容
- `<body data-bs-theme="light">` による初期テーマ設定
- `localStorage` を使用してユーザーのテーマ設定を永続化
- Bootstrap の CSS変数（例: `bg-body-tertiary`, `text-body`）により、テーマに応じて自動で配色が調整されます
- `navbar` はテーマごとに以下のように背景色を切り替えています：


```css
[data-bs-theme="light"] .navbar {
  background-color: #00bfff !important;
}
[data-bs-theme="dark"] .navbar {
  background-color: #222 !important;
}
```

🌗 テーマ切替ボタン
画面右上のボタンをクリックすることで、ユーザーが自由にテーマを切り替えることができます。
- `<button class="btn btn-outline-secondary btn-sm" onclick="toggleDarkMode()">🌓 テーマ切替</button>`

 
✅ 補足

この機能により、すべてのページにおいて一貫したユーザー体験を提供することができます。
なお、旧バージョンで一部ページに限定していたテスト用コードは、全体テンプレートに統合されたため削除しました。

ボータンの内容を少し修正しました。

## License

This project is **NOT open source**.  
All rights reserved by © 2025 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.
