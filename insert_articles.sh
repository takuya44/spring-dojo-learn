#!/bin/bash

# MySQL接続情報
DB_HOST="localhost"
DB_PORT="3307"  # ホスト側のポートを指定
DB_USER="apiuser"
DB_PASS="apipass"
DB_NAME="apidb"

# SQLクエリを実行
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" <<EOF
INSERT INTO articles (title, body) VALUES ("タイトルです4", "1本文です。");
INSERT INTO articles (title, body) VALUES ("タイトルです5", "2本文です。");
EOF
