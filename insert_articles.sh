#!/bin/zsh

# MySQL接続情報
DB_HOST="localhost"  # ローカルホストを指定
DB_PORT="3307"       # ホスト側のポートを指定
DB_USER="apiuser"
DB_PASS="apipass"
DB_NAME="apidb"

# MySQLコマンドのフルパスを指定
MYSQL_CMD="/opt/homebrew/bin/mysql"

# 環境変数を使用してパスワードのセキュリティ警告を回避
export MYSQL_PWD="$DB_PASS"

# SQLクエリを実行
$MYSQL_CMD -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" <<EOF
INSERT INTO articles (title, body) VALUES ("タイトルです4", "4本文です。");
INSERT INTO articles (title, body) VALUES ("タイトルです5", "5本文です。");
EOF
