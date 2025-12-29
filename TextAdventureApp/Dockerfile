# ▼ 1. ビルド用（Gradle）
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle キャッシュを効かせるため、先に設定ファイルだけコピー
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# 依存関係だけ先にダウンロード（キャッシュされる）
RUN ./gradlew dependencies --no-daemon || true

# 全ソースをコピーしてビルド
COPY . .

RUN ./gradlew build -x test --no-daemon


# ▼ 2. 実行用
FROM eclipse-temurin:21-jdk

WORKDIR /app

# ビルドで生成した jar をコピー
COPY --from=builder /app/build/libs/*.jar app.jar

# このコンテナが使用するポート
EXPOSE 8080

# 実行
ENTRYPOINT ["java", "-jar", "app.jar"]
