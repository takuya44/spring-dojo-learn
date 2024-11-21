# Spring 道場 Blog API

このドキュメントでは、Blog API のセットアップ方法と開発環境について説明します。他の開発者が簡単に環境を構築できるよう、手順を分かりやすくまとめました。

---

## **セットアップ**

以下の手順に従って、プロジェクトをセットアップしてください。

### **1. Docker コンテナの起動**

まず、必要な依存サービス（データベースなど）を Docker を使用して起動します。

```sh
docker compose up -d
```

- **コマンドの説明**
    - `docker compose up -d`: `docker-compose.yml` に記載された設定を元に必要なコンテナをバックグラウンドで起動します。

---

### **2. データベースの初期化**

Flyway を使用して、データベーススキーマを初期化およびマイグレーションします。

```sh
./gradlew flywayMigrate
```

- **コマンドの説明**
    - `./gradlew flywayMigrate`: Flyway を実行し、`src/main/resources/db/migration`
      にあるマイグレーションスクリプトを適用します。

---

### **3. アプリケーションの起動**

Spring Boot アプリケーションを起動します。

```sh
./gradlew bootRun
```

- **コマンドの説明**
    - `./gradlew bootRun`: アプリケーションを開発モードで起動します。
    - 起動後、API サーバーは `http://localhost:8080` でアクセス可能です。

---

## **開発環境**

開発を進めるための主要な情報は以下の通りです。

### **エンドポイントのベース URL**

```text
http://localhost:8080
```

### **主要なエンドポイント一覧**

| メソッド   | URL           | 説明         |
|--------|---------------|------------|
| GET    | `/posts`      | 全ての記事を取得   |
| POST   | `/posts`      | 新しい記事を作成   |
| GET    | `/posts/{id}` | 指定された記事を取得 |
| PUT    | `/posts/{id}` | 記事を更新      |
| DELETE | `/posts/{id}` | 記事を削除      |

---

## **データベース設定**

デフォルトのデータベース接続設定は以下の通りです。

- **データベース種別**: PostgreSQL
- **接続情報**:
    - ホスト: `localhost`
    - ポート: `3307`
    - データベース名: `apidb`
    - ユーザー名: `apiuser`
    - パスワード: `apipass`

### **データベース設定の変更**

`.env` ファイルをプロジェクトルートに作成し、以下のように記述することで接続設定を変更できます。

```env
DB_HOST=localhost
DB_PORT=3307
DB_NAME=apidb
DB_USER=apiuser
DB_PASSWORD=apipass
```

---

## **注意事項**

1. 必要なツールがインストールされていることを確認してください:
    - Docker
    - Gradle（プロジェクト内の `./gradlew` を利用可能）

2. `docker compose up` が失敗した場合は、`docker-compose.yml` ファイルを確認してください。

3. Flyway 実行時にエラーが発生した場合は、`./gradlew flywayRepair` を実行して状態をリセットしてください。

---

これでセットアップが完了します！問題が発生した場合は、`README.md` に記載されている手順を再度確認してください。