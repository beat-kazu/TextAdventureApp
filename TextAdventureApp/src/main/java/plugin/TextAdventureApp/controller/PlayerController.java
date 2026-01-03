package plugin.textadventureapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import plugin.textadventureapp.data.PlayerData;
import org.springframework.ui.Model;
import plugin.textadventureapp.service.PlayerService;

/**
 * ブラウザとのプレーヤー登録リクエスト(POST/GET)を処理するクラス
 */
@Controller
public class PlayerController {

  @Autowired
  private PlayerService service;

  /**
   * プレイヤー登録フォームをブラウザに表示する処理をするメソッド
   * @param model　画面(ブラウザ)にデータを渡すためのオブジェクト
   * @return　プレイヤー登録画面（register.html）
   */
  @GetMapping("/register")
  public String showForm(Model model) {
    model.addAttribute("player", new PlayerData());
    return "register";
  }

  /**
   *　入力されたプレイヤー情報を登録処理を行うメソッド
   * @param player　プレイヤー情報
   * @param model　画面(ブラウザ)にデータを渡すためのオブジェクト
   * @return　登録成功時はログイン画面(login.html)、失敗時は登録画面(register.html(エラーメッセージ))
   */
  @PostMapping("/register")
  public String register(@ModelAttribute("player") PlayerData player, Model model) {

    try {
      service.registerPlayer(player);
      model.addAttribute("message", "登録が完了しました！");
      return "login";
    } catch (IllegalArgumentException e) {
      model.addAttribute("message", e.getMessage());
      return "register";
    } catch (DataIntegrityViolationException e) {
      model.addAttribute("message", "登録に失敗しました。");
      return "register";
    }

  }
}
