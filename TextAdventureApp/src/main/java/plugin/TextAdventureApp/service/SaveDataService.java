package plugin.TextAdventureApp.service;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.PlayerData;
import plugin.TextAdventureApp.data.SaveData;
import plugin.TextAdventureApp.repository.PlayerRepository;
import plugin.TextAdventureApp.repository.SaveDataRepository;

@Service
public class SaveDataService {

  private final SaveDataRepository saveDataRepository;
  private final PlayerRepository playerRepository;

  public SaveDataService(SaveDataRepository saveDataRepository,
                          PlayerRepository playerRepository){
    this.saveDataRepository = saveDataRepository;
    this.playerRepository = playerRepository;
    }

  public Optional<SaveData> findByUsername(String username) {
    return saveDataRepository.findByPlayer_Username(username);
  }

  /** セーブデータのロード（無ければ自動生成） */
  public SaveData loadSaveData(String username){
    Optional<SaveData> optional = saveDataRepository.findByPlayer_Username(username);

    return optional.orElseGet(() -> createNewSaveData(username));
  }
  /** セーブ進行の保存 */
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
  /** 新規ユーザー用のデフォルトデータ生成 */
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

  public boolean deleteByUsername(String username) {

    Optional<SaveData> optional =
        saveDataRepository.findByPlayer_Username(username);

    if (optional.isEmpty()) {
      return false;
    }

    saveDataRepository.delete(optional.get());
    return true;
  }

  public boolean existsByUsername(String username) {
    return saveDataRepository.existsByPlayer_Username(username);
  }

}
