package plugin.TextAdventureApp.contoller;

import jakarta.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import plugin.TextAdventureApp.data.SceneData;
import plugin.TextAdventureApp.service.SceneService;

@Controller
public class AdvController {

  private final SceneService sceneService;

  public AdvController(SceneService sceneService) {
    this.sceneService = sceneService;
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping({"/", "/home"})
  public String home(HttpSession session) {
    // プレイヤーのアイテムをリセット
    //resetGameSession(session);
    session.removeAttribute("playerItems");
    session.setAttribute("playerItems", new HashSet<String>());
    return "home";
  }


  @GetMapping("/start")
  public String start(Model model, HttpSession session) {
    //resetGameSession(session);
    SceneData scene = sceneService.getScene("start");

    // debug
    Set<String> items = (Set<String>) session.getAttribute("playerItems");
    System.out.println("現在のアイテム: " + items); // デバッグ表示


    model.addAttribute("scene", scene);
    return "game";
  }

  @PostMapping("/choice")
  public String choice(@RequestParam String selected,
                       @RequestParam String currentScene,
                       @RequestParam(required=false) String previousScene,
                       Model model,
                       HttpSession session) {

    @SuppressWarnings("unchecked")
    Set<String> playerItems = (Set<String>) session.getAttribute("playerItems");
    if (playerItems == null) {
      playerItems = new HashSet<>();
      session.setAttribute("playerItems", playerItems);
    }
    SceneData next = sceneService.getNextScene(currentScene, selected, previousScene, playerItems);

    // GameOverを選んだ時だけhomeに戻す
    if ("GameOver".equals(selected)) {
      session.removeAttribute("playerItems");
      return "redirect:/home";
    }
    // nextSceneMap に home があればリダイレクト
    //if (next.getNextSceneMap() != null && next.getNextSceneMap().containsValue("home")) {
    //  sceneService.resetPlayerItems();
    //  return "redirect:/home";
    // }


    model.addAttribute("scene", next);
    return "game";
  }

  // ======================================
  // ▼ ゲストモード用の追加部分
  // ======================================

  @GetMapping("/guest/start")
  public String guestStart(Model model, HttpSession session) {
    // セッション初期化（ゲスト専用）
    session.removeAttribute("playerItems");
    session.setAttribute("playerItems", new HashSet<String>());
    session.setAttribute("guestMode", true);

    SceneData scene = sceneService.getScene("start");
    model.addAttribute("scene", scene);
    model.addAttribute("isGuest", true);  // Thymeleaf 側でゲスト表記など出したいとき用
    return "game";
  }

  @PostMapping("/guest/choice")
  public String guestChoice(@RequestParam String selected,
      @RequestParam String currentScene,
      @RequestParam(required=false) String previousScene,
      Model model,
      HttpSession session) {

    @SuppressWarnings("unchecked")
    Set<String> playerItems = (Set<String>) session.getAttribute("playerItems");
    if (playerItems == null) {
      playerItems = new HashSet<>();
      session.setAttribute("playerItems", playerItems);
    }

    SceneData next = sceneService.getNextScene(currentScene, selected, previousScene, playerItems);

    if ("GameOver".equals(selected)) {
      session.removeAttribute("playerItems");
      session.removeAttribute("guestMode");
      return "redirect:/home";
    }

    model.addAttribute("scene", next);
    model.addAttribute("isGuest", true);
    return "game";
  }


  private void resetGameSession(HttpSession session) {
    // 既存ログインセッションを維持しつつ、ゲームデータのみ初期化
    session.setAttribute("playerItems", new HashSet<String>());
    session.setAttribute("previousScene", null);
  }

}
