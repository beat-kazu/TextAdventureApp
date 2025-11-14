package plugin.TextAdventureApp.data;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * アドベンチャーゲームにおける一つのシーン(場面)を表すデータセットです。
 */
@Setter
@Getter
public class SceneData {
  private String id;
  private String message;
  private List<String> choices;
  private Map<String, String> nextSceneMap;

  private String itemReward;
  private String requiredItem;
  private String previousSceneId;

  /**
   * シーンIDとメッセージを指定してインスタンスを生成します。
   * @param id シーンID
   * @param message 表示メッセージ
   */
  public SceneData(String id, String message) {
    this.id = id;
    this.message = message;
  }

  /**
   * 獲得できるアイテムを設定します。（チェーン対応）
   * @param itemName 獲得アイテム名
   * @return このインスタンス自身
   */
  public SceneData reward(String itemName) {
    this.itemReward = itemName;
    return this;
  }

  /**
   * 入場に必要なアイテム設定（チェーン対応）
   * @param itemName 入場に必要なアイテム名
   * @return このインスタンス自身
   */
  public SceneData requires(String itemName) {
    this.requiredItem = itemName;
    return this;
  }

  /**
   * このシーンデータを複製して新しいインスタンスを生成します。
   * @return このシーンの複製データ
   */
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
