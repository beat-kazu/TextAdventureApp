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


@Controller
public class PlayerController {

  @Autowired
  private PlayerService service;

  // 登録画面の表示
  @GetMapping("/register")
  public String showForm(Model model) {
    model.addAttribute("player", new PlayerData());
    return "register";  // register.html に対応
  }

  // 登録処理
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
