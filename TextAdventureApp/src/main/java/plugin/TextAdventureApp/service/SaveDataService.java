package plugin.textadventureapp.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import plugin.textadventureapp.data.PlayerData;
import plugin.textadventureapp.data.SaveData;
import plugin.textadventureapp.repository.PlayerRepository;
import plugin.textadventureapp.repository.SaveDataRepository;

/**
 * プレイヤーのゲーム進行状態（セーブデータ）を管理する サービス
 */
@Service
public class SaveDataService {

  private final SaveDataRepository saveDataRepository;
  private final PlayerRepository playerRepository;

  /**
   * SaveDataServiceのコンストラクタ
   * @param saveDataRepository プレーヤーセーブデータに関するデータベース操作を行うインターフェース
   * @param playerRepository プレーヤー情報に関するデータベース操作を行うインターフェース
   */
  public SaveDataService(SaveDataRepository saveDataRepository,
                          PlayerRepository playerRepository){
    this.saveDataRepository = saveDataRepository;
    this.playerRepository = playerRepository;
    }

  /**
   * 指定したユーザー名に紐づくセーブデータを取得する
   * @param username プレイヤーのユーザー名
   * @return セーブデータ（存在しない場合は Optional.empty）
   */
  public Optional<SaveData> findByUsername(String username) {
    return saveDataRepository.findByPlayer_Username(username);
  }

  /**
   * セーブデータをロードする。
   * @param username プレイヤーのユーザー名
   * @return ロードされたセーブデータ
   */
  public SaveData loadSaveData(String username){
    Optional<SaveData> optional = saveDataRepository.findByPlayer_Username(username);

    return optional.orElseGet(() -> createNewSaveData(username));
  }

  /**
   * プレイヤーの進行状況を保存する。
   * @param username プレイヤーのユーザー名
   * @param currentSceneId 現在のシーンID
   * @param previousSceneId 直前のシーンID
   * @param itemsJson 所持アイテム情報（JSON形式）
   * @param flagsJson イベントフラグ情報（JSON形式）
   * @return 保存されたセーブデータ
   */
  public SaveData saveProgress(String username,
                               String currentSceneId,
                               String previousSceneId,
                               String itemsJson,
                               String flagsJson)
  {

    if (currentSceneId == null || currentSceneId.isEmpty()) {
      throw new IllegalArgumentException("currentSceneId cannot be null");
    }

    if (itemsJson == null) {
      itemsJson = "[]"; // fallback
    }


    SaveData saveData = loadSaveData(username);
    saveData.setFlags(flagsJson);

    saveData.setCurrentSceneId(currentSceneId);
    saveData.setPreviousSceneId(previousSceneId);
    saveData.setItems(itemsJson);
    return saveDataRepository.save(saveData);

  }
  /**
   * 新規ユーザー用のデフォルトデータ生成
   * @param username プレイヤーのユーザー名
   * @return 初期化されたセーブデータ
   */
  private SaveData createNewSaveData(String username){

    PlayerData player = playerRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Player not found"));

    SaveData save = new SaveData();
    save.setPlayer(player);
    save.setCurrentSceneId("start");
    save.setPreviousSceneId(null);
    save.setItems("[]");
    save.setFlags("{}");
    return saveDataRepository.save(save);
  }

  /**
   * 指定したユーザーのセーブデータを削除する。
   * @param username プレイヤーのユーザー名
   * @return 削除成功時 true、対象が存在しない場合 false
   */
  public boolean deleteByUsername(String username) {

    Optional<SaveData> optional =
        saveDataRepository.findByPlayer_Username(username);

    if (optional.isEmpty()) {
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
    return saveDataRepository.existsByPlayer_Username(username);
  }

}
