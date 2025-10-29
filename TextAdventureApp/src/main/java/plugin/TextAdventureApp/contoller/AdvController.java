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

/**
 * テキストアドベンチャーゲームの画面遷移、プレイ進行を制御するクラス
 */
@Controller
public class AdvController {

  private final SceneService sceneService;

  /**
   * AdvControllerのコンストラクタ。sceneServiceのインスタンスを受け取っています。
   * @param sceneService　sceneService
   */
  public AdvController(SceneService sceneService) {
    this.sceneService = sceneService;
  }

  /**
   * ログイン画面を表示します。
   * @return　ログインページ（login.html）を返します。
   */
  @GetMapping("/login")
  public String login() {
    return "login";
  }

  /**
   * ホーム画面を表示し、プレイヤーのアイテムデータを初期化します。
   * @param session HTTPセッション
   * @return ホーム画面(home.html）を返します。
   */
  @GetMapping({"/", "/home"})
  public String home(HttpSession session) {
    // プレイヤーのアイテムをリセット
    //resetGameSession(session);
    session.removeAttribute("playerItems");
    session.setAttribute("playerItems", new HashSet<String>());
    return "home";
  }

  /**
   * ゲームを開始し、最初のシーンを表示します。
   * @param model ブラウザにデータを渡すためのオブジェクト
   * @param session　プレイヤーのアイテムなどを保持するセッション
   * @return　ゲーム画面(start.html）を返します。
   */
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

  /**
   * プレイヤーの選択肢を処理し、次のシーンへ遷移します。
   * @param selected 現在の選択肢
   * @param currentScene 現在のシーンID
   * @param previousScene 直前のシーンID
   * @param model ブラウザにデータを渡すためのオブジェクト
   * @param session プレイヤーのアイテムなどを保持するセッション
   * @return 次のシーン画面、またはGameOver時のホーム画面へのリダイレクト
   */
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

  // ================================================
  // ▼ ゲストモード用の追加部分　ベースは通常時と同じ為docは略
  // ================================================

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

  /**
   * ゲーム進行情報をリセットします（ログインセッションは維持）。
   * @param session 現在のHTTPセッション
   */
  private void resetGameSession(HttpSession session) {
    // 既存ログインセッションを維持しつつ、ゲームデータのみ初期化
    session.setAttribute("playerItems", new HashSet<String>());
    session.setAttribute("previousScene", null);
  }

}
