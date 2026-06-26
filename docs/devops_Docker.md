## 📡 Docker Setting[250722]
## 実行方法
 1. '.env'ファイルをルートディレクトリに生成する。
  `DB_NAME=yuru_archive_run`
  `DB_USER=postgres`
  `DB_PASSWORD=(あなたのパスワード)`
 2. 次のコマンドを入力します。
 `bash`
 `docker-compose up --build`

### ✅ GitHub にプッシュする手順
```
	git init  #（初回の場合）
	git add .
	git commit -m "Initial commit with Docker and .env"
	git branch -M main
	git remote add origin https://github.com/your-name/your-repo.git
	git push -u origin main
```

## 📡 Docker Java17 → Java 21 に環境再調整 [260626]
1. LocalのJava環境はJava21なのに、Localと一致していないことを確認、環境を再調整しました。

## License
This project is **NOT open source**.  
All rights reserved by © 2025~2026 John Dev.  
Commercial use is strictly prohibited unless prior written permission is obtained.