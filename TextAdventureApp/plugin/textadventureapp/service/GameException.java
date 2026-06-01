package plugin.textadventureapp.service;

/**
 * ゲーム進行中に発生する業務例外を表す例外クラスです。
 * シーン遷移やゲーム状態処理などで発生した,エラー通知に使用します。
 */
public class GameException extends RuntimeException {

  /**
   * メッセージを指定してGameExceptionを生成します。
   * @param message エラーメッセージ
   */
  public GameException(String message) {
    super(message);
  }

  /**
   * メッセージと原因例外を指定してGameExceptionを生成します。
   * @param message エラーメッセージ
   * @param cause 発生原因となった例外
   */
  public GameException(String message, Throwable cause) {
    super(message, cause);
  }
}
