package plugin.TextAdventureApp.controller;

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
import plugin.TextAdventureApp.DTO.LoadResponseDTO;
import plugin.TextAdventureApp.DTO.SaveRequestDTO;
import plugin.TextAdventureApp.DTO.SaveResponseDTO;
import plugin.TextAdventureApp.data.SaveData;

import plugin.TextAdventureApp.service.PlayerService;
import plugin.TextAdventureApp.service.SaveDataService;

@RestController
@RequestMapping("/api/save")
public class SaveDataController {

  private final SaveDataService saveDataService;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;

  public SaveDataController(
      SaveDataService saveDataService,
      PlayerService playerService,
      ObjectMapper objectMapper){
    this.saveDataService = saveDataService;
    this.playerService = playerService;
    this.objectMapper = objectMapper;
  }


  // POST /api/save
  @PostMapping
  public ResponseEntity<?>  save(@RequestBody  SaveRequestDTO request,
      Principal principal,
      HttpSession session){

    // ゲストプレイ禁止
    Boolean guestMode = (Boolean) session.getAttribute("guestMode");
    if (guestMode != null && guestMode) {
      System.out.println("Guest is trying to save. Blocked.");
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("ゲストプレイ中はセーブできません");
    }

    if (principal == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("未ログイン時はセーブできません");
    }

    System.out.println("api/save called by: " + principal.getName() + ", payload items: " + request.getItems());
    // 1. ユーザー名を強制的に設定（クライアント任せにしない）
    request.setUsername(principal.getName());

    // セッションの items を統合する
    @SuppressWarnings("unchecked")
    Set<String> sessionItems = (Set<String>) session.getAttribute("playerItems");

    List<String> mergedItems = new ArrayList<>();

    // 確定済み items
    if (sessionItems != null) {
      mergedItems.addAll(sessionItems);
    }

    // ★ pendingReward を保存用に含める
    String pending = (String) session.getAttribute("pendingReward");
    if (pending != null && !mergedItems.contains(pending)) {
      mergedItems.add(pending);
    }

    // クライアントの items は信用せず、サーバー状態で上書き
    request.setItems(mergedItems);

    //if (sessionItems != null && !sessionItems.isEmpty()) {
    //  request.setItems(new ArrayList<>(sessionItems));
    //}

    // 2. items(List<String>) → JSON(String)
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

    System.out.println("flags = " + flags);
    System.out.println("flagsJson value = " + flagsJson);
    System.out.println("flagsJson class = " + flagsJson.getClass());

    // 3. サービスで保存
    SaveData saved = saveDataService.saveProgress(
        request.getUsername(),
        request.getCurrentSceneId(),
        request.getPreviousSceneId(),
        itemsJson,
        flagsJson
    );

    return ResponseEntity.ok(convertToResponseDTO(saved));
  }

  @PostMapping("/apply")
  public ResponseEntity<?> applyLoadedItemsToSession(
      @RequestBody LoadResponseDTO data,
      HttpSession session
  ) {
    // セッションの items を置換する
    session.setAttribute("playerItems", new HashSet<>(data.getItems()));

    // シーンもセッションに保持（任意）
    session.setAttribute("loadedSceneId", data.getCurrentSceneId());

    System.out.printf(">>> session updated by load: %s%n", data.getItems());
    Map<String, Boolean> flags = data.getFlags();
    Boolean foodEventUsed = flags.get("foodEventUsed");
    if (foodEventUsed != null) {
      session.setAttribute("foodEventUsed", foodEventUsed);
    }

    return ResponseEntity.ok("applied");
  }


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
    // デバッグ
    System.out.println("loadedFlags = " + loadedFlags);

    // PlayerService に渡す
    playerService.replaceFlags(principal.getName(), loadedFlags);

    // JSON → List<String>
    List<String> items = new ArrayList<>();
    //Map<String, Boolean> flags = new HashMap<>();
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
        //flags
        loadedFlags
    );

    return ResponseEntity.ok(dto);
  }

  // ============================================
  //  ロード画面 (/load) を返す Controller
  // ============================================
  @Controller
  public static class LoadPageController {

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
  // GET /api/save/load
  //@GetMapping("/load")
  //public String loadPage() {
  // return "load"; // load.html を表示
  //}

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

    // セッション初期化（重要）
    //session.removeAttribute("playerItems");
    //session.removeAttribute("loadedSceneId");

    return ResponseEntity.ok("deleted");
  }
}
