package plugin.textadventureapp.controller;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import plugin.textadventureapp.data.PlayerData;
import plugin.textadventureapp.data.SceneData;
import plugin.textadventureapp.service.FavoriteService;
import plugin.textadventureapp.service.FoodCategoryService;
import plugin.textadventureapp.service.PlayerService;
import plugin.textadventureapp.service.SaveDataService;
import plugin.textadventureapp.service.SceneService;

/**
 * テキストアドベンチャーゲームの画面遷移、プレイ進行を制御するクラス
 */
@Controller
public class AdvController {

  private final SceneService sceneService;
  private final SaveDataService saveService;
  private final PlayerService playerService;
  private final FoodCategoryService foodCategoryService;
  private final FavoriteService favoriteService;


  /**
   * AdvControllerのコンストラクタ。ゲーム進行・プレイヤー情報・セーブデータ管理に関する各 Service のインスタンスを受け取ります。
   * @param sceneService シーン遷移およびシーン情報取得を担当する Service
   * @param playerService プレイヤー情報およびイベントフラグ管理を担当する Service
   * @param saveService セーブデータの永続化を担当する Service
   * @param foodCategoryService 食べ物カテゴリ情報を扱う Service
   * @param favoriteService プレイヤーの好きな食べ物情報取得を担当する Service
   */
  public AdvController(SceneService sceneService,
                       PlayerService playerService,
                       SaveDataService saveService,
                       FoodCategoryService foodCategoryService,
                       FavoriteService favoriteService) {
    this.sceneService = sceneService;
    this.saveService = saveService;
    this.playerService = playerService;
    this.foodCategoryService = foodCategoryService;
    this.favoriteService = favoriteService;
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
   * ホーム画面を表示し、ゲーム開始前のセッション状態を初期化します。
   * @param session HTTPセッション(ゲーム進行中の一時状態を保持）
   * @param auth Spring Security による認証情報
   * @param model 画面表示用データを格納する Model
   * @param principal ログイン中ユーザーの識別情報
   * @return  ホーム画面(home.html）を返します。
   */
  @GetMapping({"/", "/home"})
  public String home(HttpSession session, Authentication auth, Model model, Principal principal) {
    // セッション初期化処理
    session.removeAttribute("playerItems");
    session.setAttribute("playerItems", new HashSet<String>());
    session.removeAttribute("guestMode");

    // ▼ ログイン状態を Thymeleaf へ渡す
    boolean isLoggedIn = (auth != null && auth.isAuthenticated());
    model.addAttribute("isLoggedIn", isLoggedIn);

    // 初期値を入れる
    boolean hasSave = false;

    // ログイン済み → セーブデータ存在チェック
    if (isLoggedIn) {
      String username = auth.getName();
      hasSave = saveService.findByUsername(username).isPresent();
    }
      model.addAttribute("hasSave", hasSave);


    return "home";
  }

  /**
   * ゲームを開始し、最初のシーンを表示します。
   * @param session　プレイヤーのアイテムなどを保持するセッション
   * @return　ゲーム画面(start.html）を返します。
   */
  @GetMapping("/start")
  public String start(HttpSession session) {

    // 明示的に「通常プレイ」
    session.setAttribute("guestMode", false);

    // アイテム初期化
    session.setAttribute("playerItems", new HashSet<String>());

    // 表示は /game に任せる
    return "redirect:/game?sceneId=start";
  }

  /**
   * プレイヤーの選択肢を処理し、次のシーンへ遷移します。
   * @param selected プレイヤーが選択した選択肢
   * @param currentScene 現在のシーンID
   * @param previousScene 直前のシーンID
   * @param itemsJson フロント側から渡されるアイテム情報（現在は未使用）
   * @param model ブラウザにデータを渡すためのオブジェクト
   * @param principal ログイン中ユーザーの識別情報
   * @param session プレイヤーのアイテムなどを保持するセッション
   * @return 次のシーン画面、またはGameOver時のホーム画面へのリダイレクト
   */
  @PostMapping("/choice")
  public String choice(@RequestParam (required = false) String selected,
                       @RequestParam String currentScene,
                       @RequestParam(required=false) String previousScene,
                       @RequestParam(required=false) String itemsJson,
                       Model model,
                       Principal principal,
                       HttpSession session) {

    boolean hasSave = false;
    if (principal != null) {
      hasSave = saveService.existsByUsername(principal.getName());
    }
    model.addAttribute("hasSave", hasSave);


    boolean guestMode = Boolean.TRUE.equals(session.getAttribute("guestMode"));

    // selected が null → 直接シーン表示に切り替える
    if (selected == null) {
      return "redirect:/game?sceneId=" + currentScene;
    }

    @SuppressWarnings("unchecked")
    Set<String> playerItems = (Set<String>) session.getAttribute("playerItems");
    if (playerItems == null) {
      playerItems = new HashSet<>();
      session.setAttribute("playerItems", playerItems);
    }

    //pendingReward をここで確定
    String pending = (String) session.getAttribute("pendingReward");
    if (pending != null) {
      playerItems.add(pending);
      session.removeAttribute("pendingReward");
    }


    String favorite = favoriteService.getFavorite(principal, guestMode);

    boolean foodEventUsed =
        Boolean.TRUE.equals(session.getAttribute("foodEventUsed"));

    SceneData next = sceneService.getNextScene(
        currentScene,
        selected,
        playerItems,
        foodEventUsed,
        favorite
    );

// foodEventUsed を立てる判断は Controller
    if (sceneService.shouldMarkFoodEventUsed(next.getId())) {
      //セッションに反映
      session.setAttribute("foodEventUsed", true);
      //ログインユーザーのみ DB に永続化
      if (principal != null && !guestMode) {
        playerService.markFoodEventUsed(principal.getName());
      }
    }

      if ("GameOver".equals(selected)) {
      session.removeAttribute("playerItems");
      session.setAttribute("guestMode", false);
      return "redirect:/home";
    }

    return "redirect:/game?sceneId=" + next.getId();
  }

  /**
   * 現在のゲーム状態をもとに、表示すべきシーンを決定し描画します。
   * @param sceneId 表示対象のシーンID（未指定時は start）
   * @param model 画面表示用データを格納する Model
   * @param session セッション（ゲーム進行中の一時状態を保持）
   * @param principal ログイン中ユーザーの識別情報
   * @return ゲーム画面（game.html）
   */
  @GetMapping("/game")
  public String game(
      @RequestParam(required=false) String sceneId,
      Model model,
      HttpSession session,
      Principal principal) {

    // ゲスト判定
    boolean isGuest = (principal == null) || Boolean.TRUE.equals(session.getAttribute("guestMode"));

    model.addAttribute("isGuest", isGuest);

    // item セッション処理
    @SuppressWarnings("unchecked")
    Set<String> items = (Set<String>) session.getAttribute("playerItems");
    if (items == null) {
      items = new HashSet<>();
      session.setAttribute("playerItems", items);
    }
    model.addAttribute("items", items);

    if (sceneId == null || sceneId.isEmpty()) {
      sceneId = "start";
    }

    boolean foodEventUsed = false;
    if (principal != null && !isGuest) {
      foodEventUsed = playerService.findByUsername(principal.getName())
          .map(playerService::getFlags)
          .map(flags -> Boolean.TRUE.equals(flags.get("foodEventUsed")))
          .orElse(false);
    }
    String favorite = favoriteService.getFavorite(principal, isGuest);

    if ("village".equals(sceneId)) {
      if (isGuest) {
        sceneId = "villageGuest";
      } else if (foodEventUsed) {
        sceneId = "villageAfterFood";
      } else {
        sceneId = "foodEvent";
      }
    }

    if ("end".equals(sceneId)) {
      if (isGuest) {
        sceneId = "end";
      } else if (foodEventUsed) {
        sceneId = "ending_food";
      } else {
        sceneId = "end";
      }
    }

    SceneData scene = sceneService.getScene(
        sceneId,
        items,
        foodEventUsed,
        favorite
    );

    // ゲストの判定
    Boolean guestMode = (Boolean) session.getAttribute("guestMode");

    String message = scene.getMessage();

    // ▼ 終了シーンだけプレイヤー名を差し替え
    if ("end".equals(scene.getId())) {
      String playerName = "あなた";

      // ゲストでない & Principal がある場合のみ反映
      if (principal != null && (guestMode == null || !guestMode)) {
        playerName = playerService.findByUsername(principal.getName())
            .map(PlayerData::getNickname)
            .filter(n -> n != null && !n.isBlank())
            .map(n -> n + "さん")
            .orElse("あなた");
      }
      message = message.replace("{player}", playerName);
    }

    String playerName = favoriteService.getPlayerNameForDisplay(principal, isGuest);

    sceneService.resolveMessage(scene, favorite, playerName);

    model.addAttribute("scene", scene);

    // 取得メッセージを表示したら pending に積む
    if (scene.getItemReward() != null) {
      session.setAttribute("pendingReward", scene.getItemReward());
    }

    boolean hasSave = false;

    if (principal != null) {
      hasSave = saveService.existsByUsername(principal.getName());
    }

    model.addAttribute("hasSave", hasSave);

    // ロード専用データは clear → game.html 側で JS が扱うため
    model.addAttribute("loadedItems", null);
    model.addAttribute("loadedSceneId", null);

    return "game";
  }

  /**
   * ゲストモード用のゲーム画面表示。
   * ゲストプレイ表記を表示、ゲストプレイ状態を引き渡し
   */
  @GetMapping("/guest/start")
  public String guestStart(HttpSession session) {
    session.setAttribute("guestMode", true);
    session.setAttribute("playerItems", new HashSet<String>());
    return "redirect:/game?sceneId=start";
  }

  /**
   * ゲストモード用のゲーム画面表示。
   * ゲストプレイ状態を引き渡し
   */
  @PostMapping("/guest/choice")
  public String guestChoice(
      @RequestParam(required = false) String selected,
      @RequestParam String currentScene,
      @RequestParam(required = false) String previousScene,
      Model model,
      Principal principal,
      HttpSession session) {

    session.setAttribute("guestMode", true);

    return choice(selected, currentScene, previousScene, null, model, principal,session);
  }


}
