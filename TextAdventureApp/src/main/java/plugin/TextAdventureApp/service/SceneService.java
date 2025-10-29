package plugin.TextAdventureApp.service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.SceneData;

/**
 * ゲームの場面遷移を制御するクラス
 */
@Service
public class SceneService{
  private Map<String, SceneData> scenes = new HashMap<>();

  /**
   * ゲーム開始時に呼び出される初期化メソッド
   */
  @PostConstruct
  public void init() {
    SceneData start = new SceneData("start","あなたは暗い森にいます。どちらに進みますか？");
    start.setChoices(List.of("洞窟へ進む", "村に戻る"));
    start.setNextSceneMap(Map.of("洞窟へ進む", "cave", "村に戻る", "village"));

    // 洞窟シーン
    SceneData cave = new SceneData("cave","洞窟に入りました…中は真っ暗です。");
    cave.setChoices(List.of("奥へ進む", "引き返す"));
    cave.setNextSceneMap(Map.of("奥へ進む", "deepCave", "引き返す", "village"));

    // 洞窟の奥シーン（確率イベントあり）
    SceneData deepCave = new SceneData("deepCave","とても真っ暗です。何かが潜んでいるような気がします…");
    deepCave.setChoices(List.of("探索する", "引き返す"));
    deepCave.setNextSceneMap(Map.of("探索する", "deepCaveEvent", "引き返す", "village"));

    // 確率イベントシーン
    SceneData deepCaveEvent = new SceneData("deepCaveEvent","探索中…");
    deepCaveEvent.setChoices(List.of("村に戻る","もっと奥に進む"));
    deepCaveEvent.setNextSceneMap(Map.of("村に戻る", "village","もっと奥に進む", "deepfullCave"));

    // 宝石を持っていないと通れないルート
    SceneData deepfullCave = new SceneData("deepfullCave", "光る壁が現れた。宝石が反応して道が開く！");
    deepfullCave.requires("宝石");
    deepfullCave.setChoices(List.of("奥へ進む", "村に戻る"));
    deepfullCave.setNextSceneMap(Map.of("奥へ進む", "treasure", "村に戻る", "village"));

    // 宝物部屋
    SceneData treasure = new SceneData("treasure", "あなたは宝物を見つけた！冒険は成功です。");
    treasure.setChoices(List.of("冒険を終える"));
    treasure.setNextSceneMap(Map.of("冒険を終える", "end"));

    // 宝石をもっていない場合の引き戻されるポイント
    SceneData backcave = new SceneData("backcave", "どこかで奥に進める何かを見つける必要がありそうだ。");
    backcave.setChoices(List.of("冒険を終える"));
    backcave.setChoices(List.of("もう一度洞窟にもどる", "あきらめて村に戻る"));
    backcave.setNextSceneMap(Map.of("もう一度洞窟にもどる", "cave", "あきらめて村に戻る", "village"));

    // 村
    SceneData village = new SceneData("village","村に戻りました。ひとまず安全です。");
    village.setChoices(List.of("もう一度冒険へ", "休む"));
    village.setNextSceneMap(Map.of("もう一度冒険へ", "start", "休む", "end"));

    // 終了
    SceneData end = new SceneData("end","あなたの冒険は終わりです。プレイしてくれてありがとう。");
    end.setChoices(List.of("GameOver"));
    end.setNextSceneMap(Map.of("GameOver", "home"));

    scenes.put("start", start);
    scenes.put("cave", cave);
    scenes.put("deepCave", deepCave);
    scenes.put("deepCaveEvent", deepCaveEvent);
    scenes.put("deepfullCave", deepfullCave);
    scenes.put("treasure", treasure);
    scenes.put("backcave", backcave);
    scenes.put("village", village);
    scenes.put("end", end);
  }

  /**
   * 指定されたシーンIDに対応する"SceneData"を取得する
   * @param id　取得したいシーンのID
   * @return　対応する"SceneData"オブジェクト
   */
  public SceneData getScene(String id) {
    //SceneData scene = scenes.getOrDefault(id, scenes.get("end"));
    SceneData original = scenes.getOrDefault(id, scenes.get("end"));
    if (original == null) return scenes.get("end");

    // 元データをコピーして返す
    SceneData scene = original.clone();

    // deepCaveEventの場合、確率イベントを発動
    if ("deepCaveEvent".equals(id)) {
      int chance = new SplittableRandom().nextInt(100); // 0〜99
      if (chance < 30) { // 30%の確率で成功
      //if (chance < 1) { // 30%の確率で成功 debug
        scene.setMessage("宝箱を見つけた！中にはキラキラした宝石が入っていた！");
        scene.reward("宝石");
      } else {
        scene.setMessage("何も見つからなかった…。ただの岩だった。");
      }
    }
    return scene;
  }

  /**
   * 選択肢に対応した次のシーン場面を返すメソッド
   * @param currentId　現在のシーンID
   * @param choice　プレイヤーの選んだ選択肢
   * @param previousId　ひとつ前のシーンID
   * @param playerItems　プレイヤーの所持アイテム
   * @return　次に表示するシーン
   */
  public SceneData getNextScene(String currentId, String choice, String previousId,Set<String> playerItems) {
    SceneData current = scenes.get(currentId);
    if (current == null) return scenes.get("end");

    String nextId = current.getNextSceneMap().getOrDefault(choice, "end");
    SceneData next = getScene(nextId);
    next.setPreviousSceneId(currentId); // 前のシーンを記録

    // 必要アイテムチェック
    if (next.getRequiredItem() != null &&
        !playerItems.contains(next.getRequiredItem())) {

      // 条件を満たさないなら、警告シーンへ
      SceneData blocked = new SceneData("blocked", "何かが足りないようだ…（進めない）");
      blocked.setChoices(List.of("戻る"));
      // 前のシーン ID を戻り先として設定
      blocked.setNextSceneMap(Map.of("戻る", "backcave"));
      scenes.put("blocked", blocked);
      return blocked;
    }


    // アイテム報酬があれば追加
    if (next.getItemReward() != null) {
      playerItems.add(next.getItemReward());
    }
    return next;
  }

  /**
   * 登録済のシーンを返すメソッド
   * @return　シーンIDに紐づいた全SceneData
   */
  public Map<String, SceneData> getScenes() {
    return scenes;
  }


}
