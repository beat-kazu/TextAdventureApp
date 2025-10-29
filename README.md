## はじめに
* 本リポジトリはJava学習者のかず(Xアカウント：@kaz_jef_endber)が作った  
Webアプリゲーム「テキストアドベンチャーゲーム」に関するものです。  
* ご利用に関するトラブル等の責任は一切負いかねます。

## コンセプト
* webブラウザ上であそぶテキストアドベンチャーゲームです。
* Java学習に際し、SplingBoot(Spling Security)を活用したWebアプリを形にしたいという事で、
* 作成してみました。Webアプリでただのtodoリストみたいな者は面白くないなという事で
* 簡単なゲームにしました。
* とはいえ、ただの一本道ゲームなので大したことはありませんが、形にしたいという事を第一に進めました。

## アプリ概要
* 本アプリは、ユーザに

## 環境
* 設計言語　　　　　　：Oracle JDK　Java21  
* 作業環境　　　　　　：Windows 11(24H2)
* ＭｙＳＱＬ　　　　　：mysql バージョン	8.0.42
* Spring Boot　　　　：バージョン	3.5.6

## データベース構成
* データベース内構造
<img width="1297" height="370" alt="image" src="https://github.com/user-attachments/assets/1c14ce19-b270-42e6-b97e-66ecb6c4e17b" />
* 実際の表示例
<img width="1664" height="438" alt="image" src="https://github.com/user-attachments/assets/7b144df7-4522-4d28-8320-300a7a325588" />

## 事前準備
* データベースの準備として、以下のファイルに記載の password をご自身のパスワードに変更してください
  + src/main/resources/application.properties
   ```　 spring.datasource.password=password　```
   
## url
* ログイン登録URL:
* http://localhost:8080/register

* アプリホーム画面URT:
* http://localhost:8080/home

## アドベンチャーゲームの流れ
* 

## 制約

## 苦労した点
* ゲームを進めるとアイテムを入手できるのですが、そのアイテムの所持未所持でルートが
かわるのですが、それがうまくいかなくて解決に関して少し妥協してなんとか意図した通りに動きました。

## 入れてみたかった機能
* あるルートを進むとゴールが変わる(ルートが変わる)という仕組み
* アイテムをDBと連携させる
* セーブ機能(ゲームの再開)
* 複数のエンディング
* などなど

## おわりに
* Java学習者のアウトプットして、リポジトリ公開させていただきました
* 感想・コメント等あればXアカウントまでご連絡くださると幸いです


