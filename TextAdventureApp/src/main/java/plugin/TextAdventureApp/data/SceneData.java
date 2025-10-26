package plugin.TextAdventureApp.data;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Setter
@Getter
public class SceneData {
  private String id;             // シーンID (例: "start", "cave", "village")
  private String message;        // 表示メッセージ
  private List<String> choices;  // 選択肢
  private Map<String, String> nextSceneMap; // 選択肢→次のシーンID

  private String itemReward;     // このシーンで得られるアイテム
  private String requiredItem;   // このシーンに入るために必要なアイテム
  private String previousSceneId; // blocked時の戻り先用

  public SceneData(String id, String message) {
    this.id = id;
    this.message = message;
  }

  // アイテム報酬設定（チェーン対応）
  public SceneData reward(String itemName) {
    this.itemReward = itemName;
    return this;
  }


  // 入場に必要なアイテム設定（チェーン対応）
  public SceneData requires(String itemName) {
    this.requiredItem = itemName;
    return this;
  }

  public SceneData clone() {
    SceneData copy = new SceneData(this.id, this.message);
    if (this.choices != null) {
      copy.setChoices(List.copyOf(this.choices));
    }
    if (this.nextSceneMap != null) {
      copy.setNextSceneMap(Map.copyOf(this.nextSceneMap));
    }
    copy.requires(this.requiredItem);
    copy.reward(this.itemReward);
    copy.setPreviousSceneId(this.previousSceneId);
    return copy;
  }

}
