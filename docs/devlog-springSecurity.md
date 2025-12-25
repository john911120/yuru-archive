## 🔐 認証ログ出力の改善 250526
- ユーザIDやパスワードに関する情報がログに出力されないように修正
- ユーザが存在しない場合のログメッセージを簡略化
- Spring Securityにおける安全なログ設計を反映

🔐 Spring Security 認証構成メモ
問題と対応（2025-05-26）
.userDetailsService(...), .passwordEncoder(...) の未設定 → 認証不可 → 対応完了
.loginProcessingUrl(...) 未指定 → POST送信先の認識失敗 → 明示的に設定
 ログ出力に ID/PW 情報 → 非表示化して簡略化対応
 
 
 ## License

This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.