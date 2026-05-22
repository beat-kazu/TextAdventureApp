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
import lombok.extern.slf4j.Slf4j;

/**
 * テキストアドベンチャーゲームの画面遷移、プレイ進行を制御するControllerクラスです。
 * シーン遷移、セッション管理、ゲストプレイ制御、ゲーム状態の初期化などを行います。
 */
@Slf4j
@Controller
public class AdvController {

  private final SceneService sceneService;
  private final SaveDataService saveService;
  private final PlayerService playerService;
  private final FoodCategoryService foodCategoryService;
  private final FavoriteService favoriteService;


  /**
   * AdvControllerで使用する各Serviceを受け取ります。
   * @param sceneService シーン遷移およびシーン情報取得を管理するService
   * @param playerService プレイヤー情報を管理するService
   * @param saveService セーブデータを管理するService
   * @param foodCategoryService 食べ物カテゴリ情報を扱うService
   * @param favoriteService 好きな食べ物情報を扱うService
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
   * @return　ログイン画面（login.html）
   */
  @GetMapping("/login")
  public String login() {
    return "login";
  }


  /**
   * ホーム画面を表示します。
   * ゲーム開始前のセッション初期化、ログイン状態判定、
   * セーブデータ有無の確認を行い、画面表示用データをModelへ設定します。
   * @param session ゲーム進行中の状態を保持するHTTPセッション
   * @param auth Spring Security による認証情報
   * @param model 画面表示用データを格納する Model
   * @param principal ログイン中ユーザーの識別情報
   * @return  ホーム画面（home.html）
   */
  @GetMapping({"/", "/home"})
  public String home(HttpSession session, Authentication auth, Model model, Principal principal) {
    String username = (principal != null) ? principal.getName() : null;
    String logUser = (username != null) ? username : "anonymous";


    log.info("STEP1: controller start");
    log.info("=== LOGIN SUCCESS === loginUser={}", logUser );
    log.info("SESSION before init: items={}, pending={}",
        session.getAttribute("playerItems"),
        session.getAttribute("pendingReward"));

    // セッション初期化処理
    session.removeAttribute("playerItems");
    session.setAttribute("playerItems", new HashSet<String>());
    session.removeAttribute("guestMode");
    session.removeAttribute("foodEventUsed");

    log.info("SESSION after init: items={}",
        session.getAttribute("playerItems"));

    // ▼ ログイン状態を Thymeleaf へ渡す
    boolean isLoggedIn = (auth != null && auth.isAuthenticated());
    model.addAttribute("isLoggedIn", isLoggedIn);

    // 初期値を入れる
    boolean hasSave = false;

    // ログイン済み → セーブデータ存在チェック
    if (username != null) {
      try{
        hasSave = saveService.findByUsername(username).isPresent();
      }catch (Exception e){
        log.error("SaveData check failed: username={}", username, e);
        hasSave = false;
      }
    }
      model.addAttribute("hasSave", hasSave);


    return "home";
  }

  /**
   * 通常プレイを開始します。
   * ゲーム用セッション情報を初期化し、最初のシーンへリダイレクトします。
   * @param session　ゲーム状態を保持するHTTPセッション
   * @return　最初のゲーム画面へのリダイレクト
   */
  @GetMapping("/start")
  public String start(HttpSession session) {

    // 明示的に「通常プレイ」
    session.setAttribute("guestMode", false);

    // アイテム初期化
    session.setAttribute("playerItems", new HashSet<String>());

    // フラグ初期化
    session.setAttribute("foodEventUsed", false);

    // 表示は /game に任せる
    return "redirect:/game?sceneId=start";
  }

  /**
   * ププレイヤーの選択内容を処理し、次のシーンへの遷移を行います。
   * アイテム取得処理、イベントフラグ更新、ゲーム終了判定なども行います。
   *
   * @param selected プレイヤーが選択した選択肢
   * @param currentScene 現在のシーンID
   * @param previousScene 直前のシーンID
   * @param itemsJson フロント側から渡されるアイテム情報（現在は未使用）
   * @param model 画面表示用データを格納するModel
   * @param principal ログイン中ユーザーの識別情報
   * @param session ゲーム状態を保持するHTTPセッション
   * @return 次のゲーム画面、またはホーム画面へのリダイレクト
   */
  @PostMapping("/choice")
  public String choice(@RequestParam (required = false) String selected,
                       @RequestParam String currentScene,
                       @RequestParam(required=false) String previousScene,
                       @RequestParam(required=false) String itemsJson,
                       Model model,
                       Principal principal,
                       HttpSession session) {

    log.info("REQUEST: scene={}, selected={}, sessionItems={}",
        currentScene, selected, session.getAttribute("playerItems"));

    log.info("SESSION STATE(before): items={}, pending={}",
        session.getAttribute("playerItems"),
        session.getAttribute("pendingReward"));


    boolean hasSave = false;
    if (principal != null) {
      hasSave = saveService.existsByUsername(principal.getName());
    }
    model.addAttribute("hasSave", hasSave);


    boolean guestMode = Boolean.TRUE.equals(session.getAttribute("guestMode"));

    log.info("FLAG(before): guestMode={}", guestMode);

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
      log.info("ITEM ACQUIRED: {}", pending);
      playerItems.add(pending);
      session.removeAttribute("pendingReward");

      log.info("SESSION STATE(after): items={}, pending={}",
          playerItems,
          session.getAttribute("pendingReward"));

    }


    String favorite = null;
    try {
      favorite = favoriteService.getFavorite(principal, guestMode);
    } catch (Exception e) {
      log.error("Favorite取得失敗", e);
    }

    boolean foodEventUsed =
        Boolean.TRUE.equals(session.getAttribute("foodEventUsed"));

    log.info("FLAG(before): foodEventUsed={}", foodEventUsed);


    SceneData next;
    try {
      next = sceneService.getNextScene(
          currentScene,
          selected,
          playerItems,
          foodEventUsed,
          favorite
      );
    } catch (Exception e) {
      log.error("Scene遷移失敗: current={}, selected={}", currentScene, selected, e);
      return "redirect:/home";
    }

// foodEventUsed を立てる判断は Controller
    if (sceneService.shouldMarkFoodEventUsed(next.getId())) {

      log.info("FLAG UPDATE: foodEventUsed -> true (scene={})", next.getId());
      //セッションに反映
      session.setAttribute("foodEventUsed", true);
    }

      if ("GameOver".equals(selected)) {
      session.removeAttribute("playerItems");
      session.setAttribute("guestMode", false);
      return "redirect:/home";
    }

    return "redirect:/game?sceneId=" + next.getId();
  }

  /**
   * 現在のゲーム状態をもとに、表示対象のシーンを決定して画面表示を行います。
   *
   * セッション内のアイテム情報、イベントフラグ、
   * ゲストプレイ状態などを参照し、 シーンメッセージを生成します。
   * @param sceneId 表示対象のシーンID（未指定時は start）
   * @param model 画面表示用データを格納する Model
   * @param session ゲーム状態を保持するHTTPセッション
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

    boolean foodEventUsed =
        Boolean.TRUE.equals(session.getAttribute("foodEventUsed"));

    log.info("SESSION FLAG: foodEventUsed={}", foodEventUsed);

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
   * ゲストモードでゲームを開始します。
   * ゲストプレイ用のセッション状態を初期化し、最初のシーンへリダイレクトします。
   * @param session ゲーム状態を保持するHTTPセッション
   * @return 最初のゲーム画面へのリダイレクト
   */
  @GetMapping("/guest/start")
  public String guestStart(HttpSession session) {
    session.setAttribute("guestMode", true);
    session.setAttribute("playerItems", new HashSet<String>());
    session.setAttribute("foodEventUsed", false);
    return "redirect:/game?sceneId=start";
  }

  /**
   * ゲストモード用の選択肢処理を行います。
   * ゲストプレイ状態をセッションへ設定し、通常の選択肢処理へ委譲します。
   * @param selected プレイヤーが選択した選択肢
   * @param currentScene 現在のシーンID
   * @param previousScene 直前のシーンID
   * @param model 画面表示用データを格納するModel
   * @param principal ログイン中ユーザーの識別情報
   * @param session ゲーム状態を保持するHTTPセッション
   * @return 次のゲーム画面へのリダイレクト
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
