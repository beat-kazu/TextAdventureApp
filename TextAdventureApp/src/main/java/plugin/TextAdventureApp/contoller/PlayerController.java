package plugin.TextAdventureApp.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import plugin.TextAdventureApp.data.PlayerData;
import org.springframework.ui.Model;
import plugin.TextAdventureApp.data.SceneData;
import plugin.TextAdventureApp.repository.PlayerRepository;
import plugin.TextAdventureApp.service.PlayerService;

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
   * @return　register.htmlという画面を表示
   */
  @GetMapping("/register")
  public String showForm(Model model) {
    model.addAttribute("player", new PlayerData());
    return "register";  // register.html に対応
  }

  /**
   *　入力されたプレイヤー情報を登録処理を行うメソッド
   * @param player　プレイヤー情報
   * @param model　画面(ブラウザ)にデータを渡すためのオブジェクト
   * @return　login.html/register.html(エラーメッセージ)という画面を表示
   */
  @PostMapping("/register")
  public String register(@ModelAttribute("player") PlayerData player, Model model) {

    try {
      service.registerPlayer(player);
      model.addAttribute("message", "登録が完了しました！");
      return "login"; //"register_success";  // 成功画面（またはリダイレクト）
    } catch (IllegalArgumentException e) {
      model.addAttribute("message", e.getMessage()); // 重複エラー等
      return "register";
    } catch (DataIntegrityViolationException e) {
      model.addAttribute("message", "登録に失敗しました。");
      return "register";
    }

  }
}
