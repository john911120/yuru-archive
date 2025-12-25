## postgreSQL DatabaseServer remote setting method

✅ PostgreSQL用 5432ポートのWindowsファイアウォール開放手順（日本語版）<br>
1️⃣ コントロールパネルを開く
「コントロールパネル」を開き、「Windows Defender ファイアウォール」を選択します。<br>
2️⃣ 詳細設定を開く
左側メニューから「詳細設定」をクリックして、「Windows Defender ファイアウォールと詳細セキュリティ」を開きます。<br>
3️⃣ 受信の規則（インバウンドルール）を作成する
左側の「受信の規則」を右クリックし、「新しい規則」を選択します。<br>
4️⃣ ポートを選択する
「ポート」を選択して「次へ」をクリックします。<br>
5️⃣ TCPと特定のポート（5432）を指定する
「TCP」を選択し、「特定のローカルポート」に「5432」と入力します。<br>
「次へ」をクリックします。<br>
6️⃣ 接続を許可する
「接続を許可する」を選択し、「次へ」をクリックします。<br>
7️⃣ プロファイルを選択する
「ドメイン」、「プライベート」、「パブリック」のすべてをチェックしたまま「次へ」をクリックします。<br>
8️⃣ 名前を付ける
規則名を入力します。（例：「PostgreSQL Remote Allow」）
「完了」をクリックしてルールを作成します。<br>

これで、PostgreSQLのデフォルトポートである5432ポートが開放され、
他のPCからの接続が許可されるようになります。

## postgreSQL DatabaseServer remote 構築 感想

常にサーバーを稼働させる環境ではないため、現時点では接続方法のみを把握するにとどめています。  
オフィスや常設サーバーなど、PCを常に稼働させることが可能な環境であれば、  
この方法を用いて他の端末からもデータベースにアクセスする構成が実現可能であることを確認しました。  
今回の設定を通じて、PostgreSQLをネットワーク経由で利用する方法について理解を深めることができました。

## License

This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.