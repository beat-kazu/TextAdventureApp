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
import lombok.extern.slf4j.Slf4j;

/**
 * プレイヤー登録画面の表示とユーザー登録処理を行うControllerクラスです。
 */
@Slf4j
@Controller
public class PlayerController {

  @Autowired
  private PlayerService service;

  /**
   * プレイヤー登録画面を表示します。
   *
   * 登録フォーム用のPlayerDataを生成し、画面へ渡します。
   * @param model　画面表示用データを格納するModel
   * @return　プレイヤー登録画面（register.html）
   */
  @GetMapping("/register")
  public String showForm(Model model) {
    model.addAttribute("player", new PlayerData());
    return "register";
  }

  /**
   *　入力されたプレイヤー情報を登録します。
   * 登録成功時はログイン画面へ遷移し、登録失敗時はエラーメッセージを設定して
   * 登録画面を再表示します。
   * @param player　登録対象のプレイヤー情報
   * @param model　画面表示用データを格納するModel
   * @return　登録成功時はログイン画面（login.html）、登録失敗時は登録画面（register.html）
   */
  @PostMapping("/register")
  public String register(@ModelAttribute("player") PlayerData player, Model model) {

    log.info("Controller: register start");
    try {
      service.registerPlayer(player);
      model.addAttribute("message", "登録が完了しました！");
      log.info("Controller: register after service");
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
