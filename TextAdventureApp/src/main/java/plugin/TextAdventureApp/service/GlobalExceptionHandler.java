package plugin.textadventureapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;

/**
 * アプリ全体で発生した例外を処理するクラスです。
 * 例外発生時はエラーログを出力し、エラー画面へ遷移します。
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * ゲーム進行中に発生したGameExceptionを処理します。
   * エラーメッセージをModelへ設定し、エラー画面を表示します。
   * @param ex 発生したGameException
   * @param model 画面表示用データを格納するModel
   * @return エラー画面（error.html）
   */
  @ExceptionHandler(GameException.class)
  public String handleGameException(GameException ex, Model model) {
    log.error("GameException occurred", ex);

    model.addAttribute("errorMessage", ex.getMessage());
    return "error"; // error.html
  }

  /**
   * 想定外の例外を処理します。
   * 汎用的なエラーメッセージをModelへ設定し、エラー画面を表示します。
   * @param ex 発生した例外
   * @param model 画面表示用データを格納するModel
   * @return エラー画面（error.html）
   */
  @ExceptionHandler(Exception.class)
  public String handleException(Exception ex, Model model) {
    log.error("Unexpected error", ex);

    model.addAttribute("errorMessage", "予期しないエラーが発生しました");
    return "error";
  }
}

