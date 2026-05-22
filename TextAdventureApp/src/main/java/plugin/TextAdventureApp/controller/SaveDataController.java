package plugin.textadventureapp.controller;


import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plugin.textadventureapp.DTO.LoadResponseDTO;
import plugin.textadventureapp.DTO.SaveRequestDTO;
import plugin.textadventureapp.DTO.SaveResponseDTO;
import plugin.textadventureapp.data.SaveData;

import plugin.textadventureapp.service.PlayerService;
import plugin.textadventureapp.service.SaveDataService;
import lombok.extern.slf4j.Slf4j;
import plugin.textadventureapp.util.JsonUtils;
/**
 * ゲーム進行データのセーブ、ロード、削除を管理するREST Controllerです。
 *
 * セーブデータとセッション状態の同期処理も行います。
 */
@Slf4j
@RestController
@RequestMapping("/api/save")
public class SaveDataController {

  private final SaveDataService saveDataService;
  private final PlayerService playerService;
  private final JsonUtils jsonUtils;

  /**
   * SaveDataControllerで使用する各Serviceを受け取ります。
   * @param saveDataService セーブデータの保存・読込を管理するService
   * @param playerService プレイヤー情報を管理するService
   * @param jsonUtils　JSON変換処理を行うユーティリティ
   */
  public SaveDataController(
      SaveDataService saveDataService,
      PlayerService playerService,
      JsonUtils jsonUtils){
    this.saveDataService = saveDataService;
    this.playerService = playerService;
    this.jsonUtils = jsonUtils;
  }

  /**
   * 現在のゲーム進行状態を保存します。
   *
   * セッション内のアイテム情報やイベントフラグを取得し、セーブデータとして永続化します。
   * @param request　保存対象のゲーム進行情報
   * @param principal　認証済みユーザー情報
   * @param session　現在の HTTP セッション
   * @return　保存結果を含むレスポンス
   */
  // POST /api/save
  @PostMapping
  public ResponseEntity<?>  save(@RequestBody  SaveRequestDTO request,
      Principal principal,
      HttpSession session){
    String username = (principal != null) ? principal.getName() : null;


    log.info(">>> SAVE START: user={}", username);

    if (username  == null) {
      log.warn("SAVE BLOCKED: anonymous user");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("未ログイン時はセーブできません");
    }

    // ゲストプレイ禁止
    Boolean guestMode = (Boolean) session.getAttribute("guestMode");
    if (Boolean.TRUE.equals(guestMode)) {
      log.warn("SAVE BLOCKED: guest mode user={}", username);
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("ゲストプレイ中はセーブできません");
    }

    // ユーザー名を強制的に設定（クライアント任せにしない）
    request.setUsername(username);

    // セッションの items を統合する
    @SuppressWarnings("unchecked")
    Set<String> sessionItems = (Set<String>) session.getAttribute("playerItems");

    List<String> mergedItems = new ArrayList<>();

    // 確定済み items
    if (sessionItems != null) {
      mergedItems.addAll(sessionItems);
    }

    // pendingReward を保存用に含める
    String pending = (String) session.getAttribute("pendingReward");
    if (pending != null && !mergedItems.contains(pending)) {
      mergedItems.add(pending);
    }

    // クライアントの items は信用せず、サーバー状態で上書き
    request.setItems(mergedItems);

    String itemsJson = jsonUtils.toJson(request.getItems());

    Map<String, Boolean> flagsMap = new HashMap<>();

    Boolean foodEventUsed =
        (Boolean) session.getAttribute("foodEventUsed");

    if (Boolean.TRUE.equals(foodEventUsed)) {
      flagsMap.put("foodEventUsed", true);
    }

    log.info("SAVE FLAGS(session): {}", flagsMap);

    String flagsJson = jsonUtils.toJson(flagsMap);

    // サービスで保存
    SaveData saved;
    try {
      saved = saveDataService.saveProgress(
          username,
          request.getCurrentSceneId(),
          request.getPreviousSceneId(),
          request.getItems(),
          flagsMap
      );
    } catch (Exception e){
      log.error(">>> SAVE DB失敗: user={}", username, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("セーブに失敗しました");
    }

    log.info(">>> SAVE SUCCESS: user={}, id={}", saved.getPlayer().getUsername(), saved.getId());
    return ResponseEntity.ok(convertToResponseDTO(saved));
  }

  /**
   * ロード済みのゲーム進行状態をセッションへ反映します。
   *
   * アイテム情報、シーン情報、イベントフラグをセッションへ復元します。
   *
   * @param data ロード済みのゲーム進行データ
   * @param session ゲーム状態を保持するHTTPセッション
   * @return　セッション反映結果を示すレスポンス
   */
  @PostMapping("/apply")
  public ResponseEntity<?> applyLoadedItemsToSession(
      @RequestBody LoadResponseDTO data,
      HttpSession session
  ) {
    // セッションの items を置換する
    List<String> items = (data.getItems() != null) ? data.getItems() : new ArrayList<>();
    session.setAttribute("playerItems", new HashSet<>(items));

    // シーンもセッションに保持（任意）
    session.setAttribute("loadedSceneId", data.getCurrentSceneId());

    Map<String, Boolean> flags = (data.getFlags() != null) ? data.getFlags() : new HashMap<>();
    Boolean foodEventUsed = flags.get("foodEventUsed");
    if (foodEventUsed != null) {
      session.setAttribute("foodEventUsed", foodEventUsed);
    }

    return ResponseEntity.ok("applied");
  }

  /**
   * ログインユーザーのセーブデータを取得します。
   *
   * 保存済みのシーン情報、アイテム情報、イベントフラグを読み込みます。
   * @param principal 認証済みユーザー情報
   * @return セーブデータを含むレスポンス
   */
  // ロード API
  // GET /api/save
  @GetMapping
  public ResponseEntity<?> load(Principal principal) {

    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインしてください");
    }

    String username = principal.getName();

    Optional<SaveData> optional = saveDataService.findByUsername(username);

    if (optional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("セーブデータがありません");
    }
    SaveData data = optional.get();

    // ===== items =====
    List<String> items =   jsonUtils.toStringList(data.getItems());

    Map<String, Boolean> flags =
        jsonUtils.toFlagMap(data.getFlags());

    LoadResponseDTO dto = new LoadResponseDTO(
        data.getCurrentSceneId(),
        data.getPreviousSceneId(),
        items,
        flags
    );

    return ResponseEntity.ok(dto);
  }

  /**
   * セーブデータのロード画面を表示するControllerです。
   */
  @Controller
  public static class LoadPageController {

    /**
     * ロード画面を表示します。
     * @return ロード画面（load.html）
     */
    @GetMapping("/load")
    public String loadPage() {
      return "load"; // load.html を表示
    }
  }

  /**
   * SaveDataをレスポンス用DTOへ変換します。
   * JSON形式で保存されたアイテム情報をList形式へ変換して設定します。
   * @param data セーブデータ
   * @return レスポンス用DTO
   */
  private SaveResponseDTO convertToResponseDTO(SaveData data) {
    List<String> itemsList =
        jsonUtils.toStringList(data.getItems());

    // SaveResponseDTO へ詰め替え
    SaveResponseDTO dto = new SaveResponseDTO();
    dto.setUsername(data.getPlayer().getUsername());
    dto.setCurrentSceneId(data.getCurrentSceneId());
    dto.setPreviousSceneId(data.getPreviousSceneId());
    dto.setItems(itemsList);
    dto.setUpdatedAt(data.getUpdatedAt());

    return dto;
  }

  /**
   *  ログインユーザーのセーブデータを削除します。
   *  ゲストプレイ中は削除できません。
   * @param principal 認証済みユーザー情報
   * @param session 現在のHTTPセッション
   * @return 削除結果を示すレスポンス
   */
  // DELETE API
  // POST /api/save
  @DeleteMapping
  public ResponseEntity<?> delete(Principal principal, HttpSession session) {

    // ゲスト禁止
    Boolean guestMode = (Boolean) session.getAttribute("guestMode");
    if (guestMode != null && guestMode) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("ゲストプレイ中は削除できません");
    }

    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("未ログインです");
    }

    String username = principal.getName();
    boolean deleted = saveDataService.deleteByUsername(username);
    if (!deleted) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    return ResponseEntity.ok("deleted");
  }
}
