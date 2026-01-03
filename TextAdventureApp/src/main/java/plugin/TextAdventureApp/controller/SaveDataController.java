package plugin.textadventureapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

/**
 * ゲーム進行データ（セーブ／ロード／削除）を扱う コントローラー。
 */
@RestController
@RequestMapping("/api/save")
public class SaveDataController {

  private final SaveDataService saveDataService;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;

  /**
   * SaveDataController のコンストラクタ。
   * @param saveDataService ゲーム進行データの保存・読込を担当するサービス
   * @param playerService プレイヤー情報およびイベントフラグ管理を担当するサービス
   * @param objectMapper JSON ⇔ オブジェクト変換を行うための Jackson ObjectMapper
   */
  public SaveDataController(
      SaveDataService saveDataService,
      PlayerService playerService,
      ObjectMapper objectMapper){
    this.saveDataService = saveDataService;
    this.playerService = playerService;
    this.objectMapper = objectMapper;
  }

  /**
   * 現在のゲーム進行状態を保存するメソッド。
   * @param request　進行状態（シーン・選択情報）
   * @param principal　認証済みユーザー情報
   * @param session　現在の HTTP セッション
   * @return　保存結果を含むレスポンス
   */
  // POST /api/save
  @PostMapping
  public ResponseEntity<?>  save(@RequestBody  SaveRequestDTO request,
      Principal principal,
      HttpSession session){

    // ゲストプレイ禁止
    Boolean guestMode = (Boolean) session.getAttribute("guestMode");
    if (guestMode != null && guestMode) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("ゲストプレイ中はセーブできません");
    }

    if (principal == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("未ログイン時はセーブできません");
    }

    // ユーザー名を強制的に設定（クライアント任せにしない）
    request.setUsername(principal.getName());

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

    // items(List<String>) → JSON(String)
    String itemsJson;
    try{
      itemsJson = objectMapper.writeValueAsString(request.getItems());
    }catch(Exception e){
      return ResponseEntity.badRequest().body("Invalid items format");
    }
    Map<String, Boolean> flags = new HashMap<>();

 playerService.findByUsername(principal.getName())
        .map(playerService::getFlags)
     .ifPresent(dbFlags -> {
       Object foodEventUsed = dbFlags.get("foodEventUsed");
       if (foodEventUsed instanceof Boolean b) {
         flags.put("foodEventUsed", b);
       }
     });


    String flagsJson ;
    try {
      flagsJson = objectMapper.writeValueAsString(flags);
    } catch (JsonProcessingException e) {
      return ResponseEntity.badRequest().body("Invalid flags format");
    }


    // サービスで保存
    SaveData saved = saveDataService.saveProgress(
        request.getUsername(),
        request.getCurrentSceneId(),
        request.getPreviousSceneId(),
        itemsJson,
        flagsJson
    );

    return ResponseEntity.ok(convertToResponseDTO(saved));
  }

  /**
   * ロードしたゲーム進行データをセッションへ反映する。
   * @param data ロード済み進行データ
   * @param session セッション
   * @return
   */
  @PostMapping("/apply")
  public ResponseEntity<?> applyLoadedItemsToSession(
      @RequestBody LoadResponseDTO data,
      HttpSession session
  ) {
    // セッションの items を置換する
    session.setAttribute("playerItems", new HashSet<>(data.getItems()));

    // シーンもセッションに保持（任意）
    session.setAttribute("loadedSceneId", data.getCurrentSceneId());

    Map<String, Boolean> flags = data.getFlags();
    Boolean foodEventUsed = flags.get("foodEventUsed");
    if (foodEventUsed != null) {
      session.setAttribute("foodEventUsed", foodEventUsed);
    }

    return ResponseEntity.ok("applied");
  }

  /**
   * ログインユーザーのセーブデータを取得する。
   * @param principal 認証済みユーザー情報
   * @return ロード結果を含むレスポンス（存在しない場合はエラー）
   */
  // ロード API
  // GET /api/save
  @GetMapping
  public ResponseEntity<?> load(Principal principal) {

    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインしてください");
    }

    String username = principal.getName();

    SaveData data = saveDataService.loadSaveData(username);
    if (data == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("セーブデータがありません");
    }

    // flags を Map に復元
    Map<String, Boolean> loadedFlags;
    if(data.getFlags() == null || data.getFlags().isBlank()) {
      loadedFlags = new HashMap<>();
    }else {
      try {
        loadedFlags = objectMapper.readValue(
            data.getFlags(),
            new TypeReference<Map<String, Boolean>>() {
            }
        );
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("Invalid flags data");
      }
    }

    // PlayerService に渡す
    playerService.replaceFlags(principal.getName(), loadedFlags);

    // JSON → List<String>
    List<String> items = new ArrayList<>();
    if (data.getItems() != null && !data.getItems().isEmpty()) {
      try {
        items = objectMapper.readValue(data.getItems(), new TypeReference<>() {});
      } catch (Exception e) {
        items = new ArrayList<>();
      }
    }

    LoadResponseDTO dto = new LoadResponseDTO(
        data.getCurrentSceneId(),
        data.getPreviousSceneId(),
        items,
        loadedFlags
    );

    return ResponseEntity.ok(dto);
  }

  /**
   * セーブデータのロード画面（load.html）を表示するための Controller。
   */
  @Controller
  public static class LoadPageController {

    /**
     * セーブデータのロード画面を表示する。
     * @return ロード画面（load.html）
     */
    @GetMapping("/load")
    public String loadPage() {
      return "load"; // load.html を表示
    }
  }

//DTO 変換
  private SaveResponseDTO convertToResponseDTO(SaveData data) {
    List<String> itemsList;
    try {
      itemsList = objectMapper.readValue(data.getItems(), new TypeReference<List<String>>() {});
    } catch (Exception e) {
      throw  new RuntimeException("Invalid JSON format in DB: items");
    }

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
   * ログインユーザーのセーブデータを削除する。
   * @param principal 認証済みユーザー情報
   * @param session HTTP セッション
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

    boolean deleted = saveDataService.deleteByUsername(principal.getName());
    if (!deleted) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    return ResponseEntity.ok("deleted");
  }
}
