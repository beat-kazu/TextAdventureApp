package plugin.textadventureapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plugin.textadventureapp.constants.SceneIds;
import plugin.textadventureapp.data.PlayerData;
import plugin.textadventureapp.data.SaveData;
import plugin.textadventureapp.repository.PlayerRepository;
import plugin.textadventureapp.repository.SaveDataRepository;
import lombok.extern.slf4j.Slf4j;
import plugin.textadventureapp.util.JsonUtils;

/**
 * プレイヤーのゲーム進行状態（セーブデータ）を管理するServiceです。
 * セーブデータの保存、読み込み、削除を行います。
 */
@Slf4j
@Service
public class SaveDataService {

  private final SaveDataRepository saveDataRepository;
  private final PlayerRepository playerRepository;

  @Autowired
  private JsonUtils jsonUtils;

  /**
   * SaveDataServiceのコンストラクタ
   * @param saveDataRepository セーブデータ操作を行うRepository
   * @param playerRepository プレイヤー情報操作を行うRepository
   */
  public SaveDataService(SaveDataRepository saveDataRepository,
                          PlayerRepository playerRepository){
    this.saveDataRepository = saveDataRepository;
    this.playerRepository = playerRepository;
    }

  /**
   * 指定したユーザー名に紐づくセーブデータを取得する
   * セーブデータが存在しない場合はOptional.empty を返します。
   * @param username プレイヤーのユーザー名
   * @return セーブデータ
   */
  public Optional<SaveData> findByUsername(String username) {
    if (username == null || username.isBlank()) {
      log.warn("Find saveData skipped: username is null/blank");
      return Optional.empty();
    }

    log.debug("Find saveData: username={}", username);
    return saveDataRepository.findByPlayer_Username(username);
  }

  /**
   * 指定ユーザーのセーブデータを読み込みます。
   * セーブデータが存在しない場合は、新規セーブデータを生成して返します。(←機能現在未使用)
   * @param username プレイヤーのユーザー名
   * @return セーブデータ
   */
  public SaveData loadSaveData(String username){
    log.info("Load saveData: username={}", username);

    Optional<SaveData> optional = saveDataRepository.findByPlayer_Username(username);

    if (optional.isPresent()) {
      log.debug("SaveData found: username={}", username);
      return optional.get();
    }


    log.info("SaveData not found → create new: username={}", username);
    return createNewSaveData(username);
  }

  /**
   * プレイヤーの進行状況を保存する。
   * シーン情報、所持アイテム、イベントフラグをセーブデータへ反映して保存します。
   * @param username プレイヤーのユーザー名
   * @param currentSceneId 現在のシーンID
   * @param previousSceneId 直前のシーンID
   * @param items 所持アイテム一覧
   * @param flags イベントフラグ一覧
   * @return 保存されたセーブデータ
   */
  public SaveData saveProgress(String username,
                               String currentSceneId,
                               String previousSceneId,
                               List<String> items,
                               Map<String, Boolean> flags)
  {

    log.info(">>> saveProgress START: username={}, scene={}",
        username, currentSceneId);


    if (username == null || username.isBlank()) {
      log.error("Save failed: username is null");
      throw new GameException("Invalid username");
    }

      if (currentSceneId == null || currentSceneId.isBlank()){
      log.error("Save failed: currentSceneId is null");
      throw new GameException("Invalid currentSceneId");
    }

    if (items == null){
      log.warn("itemsJson is null → fallback []");
      items = new ArrayList<>();
    }

    if (flags == null) {
      log.warn("flagsJson is null → fallback {}");
      flags = new HashMap<>();
    }

    SaveData saveData = loadSaveData(username);
    log.debug("Before save: items={}, flags={}",
        saveData.getItems(), saveData.getFlags());


    saveData.setCurrentSceneId(currentSceneId);
    saveData.setPreviousSceneId(previousSceneId);
    saveData.setItems(jsonUtils.toJson(items));
    saveData.setFlags(jsonUtils.toJson(flags));
    SaveData saved = saveDataRepository.save(saveData);

    log.info("Save success: username={}, scene={}",
        username, currentSceneId);

    return saved;
  }
  /**
   * 新規プレイヤー用の初期セーブデータを生成します。
   * 開始シーン、空のアイテム一覧、空のイベントフラグを設定します。
   * @param username プレイヤーのユーザー名
   * @return 初期化済みセーブデータ
   */
  private SaveData createNewSaveData(String username){

    log.info("Create new SaveData: username={}", username);

    PlayerData player = playerRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.error("Player not found: username={}", username);
          return new GameException("Player not found");
        });

    SaveData save = new SaveData();
    save.setPlayer(player);
    save.setCurrentSceneId(SceneIds.START);
    save.setPreviousSceneId(null);
    save.setItems("[]");
    save.setFlags("{}");
    SaveData created = saveDataRepository.save(save);
    log.info("New SaveData created: username={}", username);
    return created;
  }

  /**
   * 指定したユーザーのセーブデータを削除する。
   * @param username プレイヤーのユーザー名
   * @return 削除成功時は true、対象データが存在しない場合は false
   */
  public boolean deleteByUsername(String username) {
    log.info("Delete saveData: username={}", username);

    if (username == null || username.isBlank()) {
      log.warn("Delete skipped: username is null/blank");
      return false;
    }

    Optional<SaveData> optional =
        saveDataRepository.findByPlayer_Username(username);

    if (optional.isEmpty()) {
      log.warn("Delete skipped: no data found username={}", username);
      return false;
    }

    saveDataRepository.delete(optional.get());
    return true;
  }

  /**
   * 指定したユーザーのセーブデータが存在するか判定する。
   * @param username プレイヤーのユーザー名
   * @return 存在する場合 true
   */
  public boolean existsByUsername(String username) {
    if (username == null || username.isBlank()) {
      log.warn("Exists check skipped: username is null/blank");
      return false;
    }

    boolean exists =
        saveDataRepository.existsByPlayer_Username(username);
    log.debug("Exists check: username={}, result={}",
        username, exists);

    return exists;
  }
}