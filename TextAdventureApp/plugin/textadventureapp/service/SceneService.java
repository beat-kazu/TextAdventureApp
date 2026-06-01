package plugin.textadventureapp.service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import lombok.Getter;
import org.springframework.stereotype.Service;
import plugin.textadventureapp.data.SceneData;
import lombok.extern.slf4j.Slf4j;
import plugin.textadventureapp.constants.SceneIds;
/**
 * テキストアドベンチャーゲームのシーン遷移およびイベント制御を行うServiceです。
 *
 * シーン情報管理、条件分岐、イベント判定を担当します。
 */
@Slf4j
@Service
public class SceneService{


  public static final int BOUND = 100;
  public static final int RATE = 30;
  private final FoodCategoryService foodCategoryService;

  @Getter
  private Map<String, SceneData> scenes = new HashMap<>();

  /**
   * SceneService のコンストラクタ。
   * @param foodCategoryService 好物カテゴリ判定を行うService
   */
  public SceneService(FoodCategoryService foodCategoryService) {
    this.foodCategoryService = foodCategoryService;
  }
  /**
   * ゲームシーン情報を初期化します。
   *
   * アプリ起動時に実行され、シーン遷移情報やイベント情報を登録します。
   */
  @PostConstruct
  public void init() {
    SceneData start = new SceneData(SceneIds.START,"あなたは暗い森にいます。どちらに進みますか？");
    start.setChoices(List.of("洞窟へ進む","村に戻る"));
    start.setNextSceneMap(Map.of("洞窟へ進む",SceneIds.CAVE,"村に戻る",SceneIds.VILLAGE));

    // 洞窟シーン
    SceneData cave = new SceneData(SceneIds.CAVE,"洞窟に入りました…中は真っ暗です。");
    cave.setChoices(List.of("奥へ進む","引き返す"));
    cave.setNextSceneMap(Map.of("奥へ進む",SceneIds.DEEP_CAVE,"引き返す",SceneIds.VILLAGE));

    // 洞窟の奥シーン（確率イベントあり）
    SceneData deepCave = new SceneData(SceneIds.DEEP_CAVE,"とても真っ暗です。何かが潜んでいるような気がします…");
    deepCave.setChoices(List.of("探索する","引き返す"));
    deepCave.setNextSceneMap(Map.of("探索する",SceneIds.DEEP_CAVE_EVENT,"引き返す",SceneIds.VILLAGE));

    // 確率イベントシーン
    SceneData deepCaveEvent = new SceneData(SceneIds.DEEP_CAVE_EVENT,"探索中…");
    deepCaveEvent.setChoices(List.of("村に戻る","もっと奥に進む"));
    deepCaveEvent.setNextSceneMap(Map.of("村に戻る",SceneIds.VILLAGE,"もっと奥に進む",SceneIds.DEEP_FULL_CAVE));

    // 宝石を持っていないと通れないルート
    SceneData deepfullCave = new SceneData(SceneIds.DEEP_FULL_CAVE,"光る壁が現れた。宝石が反応して道が開く！");
    deepfullCave.requires("宝石");
    deepfullCave.setChoices(List.of("奥へ進む","村に戻る"));
    deepfullCave.setNextSceneMap(Map.of("奥へ進む",SceneIds.TREASURE,"村に戻る",SceneIds.VILLAGE));

    // 宝物部屋
    SceneData treasure = new SceneData(SceneIds.TREASURE,"あなたは宝物を見つけた！冒険は成功です。");
    treasure.setChoices(List.of("冒険を終える"));
    treasure.setNextSceneMap(Map.of("冒険を終える",SceneIds.END));

    // 宝石をもっていない場合の引き戻されるポイント
    SceneData backcave = new SceneData(SceneIds.BACKCAVE,"どこかで奥に進める何かを見つける必要がありそうだ。");
    backcave.setChoices(List.of("冒険を終える"));
    backcave.setChoices(List.of("もう一度洞窟にもどる","あきらめて村に戻る"));
    backcave.setNextSceneMap(Map.of("もう一度洞窟にもどる",SceneIds.CAVE,"あきらめて村に戻る",SceneIds.VILLAGE));

    // 村
    SceneData village = new SceneData(SceneIds.VILLAGE,"村に戻りました。ひとまず安全です。");
    village.setChoices(List.of("もう一度冒険へ","休む","村を探索する"));
    village.setNextSceneMap(Map.of("もう一度冒険へ",SceneIds.START,"休む",SceneIds.END,"村を探索する",SceneIds.FOOD_EVENT));

    // 村（ゲストプレイ）
    SceneData villageGuest = new SceneData(SceneIds.VILLAGE_GUEST,"村に戻りました。ひとまず安全です。");
    villageGuest.setChoices(List.of("もう一度冒険へ","休む"));
    villageGuest.setNextSceneMap(Map.of("もう一度冒険へ",SceneIds.START,"休む",SceneIds.END));

    // 村（foodEvent 通過後）
    SceneData villageAfterFood = new SceneData(SceneIds.VILLAGE_AFTER_FOOD,"村に戻りました。広場は少し静かです。\nあの屋台の姿はもうありません。");
    villageAfterFood.setChoices(List.of("もう一度冒険へ","休む"));
    villageAfterFood.setNextSceneMap(Map.of("もう一度冒険へ",SceneIds.START,"休む",SceneIds.END));


    //屋台イベント
    SceneData foodEvent  = new SceneData(SceneIds.FOOD_EVENT,"村の広場で、怪しげな屋台があなたを呼び止めた。\n「お前の好物は…{favorite}だったな？」");
    foodEvent.setChoices(List.of("屋台をのぞく","無視して立ち去る"));
    foodEvent.setNextSceneMap(Map.of("屋台をのぞく",SceneIds.FOOD_CHECK,"無視して立ち去る",SceneIds.START));

    //好物チェック
    SceneData foodCheck   = new SceneData(SceneIds.FOOD_CHECK,"あなたの好物を思い出した瞬間、不思議な出来事が起こった…");
    foodCheck.setChoices(List.of("進む"));
    foodCheck.setNextSceneMap(Map.of("進む",SceneIds.FOOD_RESULT));

    //好物イベント1
    SceneData foodResultMeat = new SceneData(SceneIds.FOOD_RESULT_MEAT,"{favorite}の話をしていると、力が湧いてきた！");
    foodResultMeat.setChoices(List.of("冒険を続ける"));
    foodResultMeat.setNextSceneMap(Map.of("冒険を続ける",SceneIds.START));

    //好物イベント2
    SceneData foodResultSweet = new SceneData(SceneIds.FOOD_RESULT_SWEET,"{favorite}を思い出して少し元気が出た。慎重に進もう。");
    foodResultSweet.setChoices(List.of("冒険を続ける"));
    foodResultSweet.setNextSceneMap(Map.of("冒険を続ける",SceneIds.START));

    //好物イベント3
    SceneData foodResultRice = new SceneData(SceneIds.FOOD_RESULT_RICE,"{favorite}の温かさを思い出し、落ち着きを取り戻した。");
    foodResultRice.setChoices(List.of("冒険を続ける"));
    foodResultRice.setNextSceneMap(Map.of("冒険を続ける",SceneIds.START));

    //好物イベント4
    SceneData foodResultOther = new SceneData(SceneIds.FOOD_RESULT_OTHER,"好物のことを考えたが、特に何も起こらなかった。");
    foodResultOther.setChoices(List.of("冒険を続ける"));
    foodResultOther.setNextSceneMap(Map.of("冒険を続ける",SceneIds.START));

    // 終了
    SceneData end = new SceneData(SceneIds.END,"{player}の冒険は終わりです。プレイしてくれてありがとう。");
    end.setChoices(List.of("GameOver"));
    end.setNextSceneMap(Map.of("GameOver","home"));

    SceneData ending_food = new SceneData(SceneIds.ENDING_FOOD,"{player}の冒険は終わりです。\r\nあの屋台のおやじも祝福してくれているよ。\r\nプレイしてくれてありがとう。");
    ending_food.setChoices(List.of("GameOver"));
    ending_food.setNextSceneMap(Map.of("GameOver","home"));

    SceneData blocked = new SceneData(SceneIds.BLOCKED,"何かが足りないようだ…（進めない）");
    blocked.setChoices(List.of("戻る"));
    blocked.setNextSceneMap(Map.of("戻る",SceneIds.BACKCAVE));


    scenes.put(SceneIds.START, start);
    scenes.put(SceneIds.CAVE, cave);
    scenes.put(SceneIds.DEEP_CAVE, deepCave);
    scenes.put(SceneIds.DEEP_CAVE_EVENT, deepCaveEvent);
    scenes.put(SceneIds.DEEP_FULL_CAVE, deepfullCave);
    scenes.put(SceneIds.TREASURE, treasure);
    scenes.put(SceneIds.BACKCAVE, backcave);
    scenes.put(SceneIds.VILLAGE, village);
    scenes.put(SceneIds.VILLAGE_GUEST, villageGuest);
    scenes.put(SceneIds.VILLAGE_AFTER_FOOD, villageAfterFood);
    scenes.put(SceneIds.FOOD_EVENT, foodEvent);
    scenes.put(SceneIds.FOOD_CHECK, foodCheck);
    scenes.put(SceneIds.FOOD_RESULT_MEAT, foodResultMeat );
    scenes.put(SceneIds.FOOD_RESULT_SWEET, foodResultSweet );
    scenes.put(SceneIds.FOOD_RESULT_RICE, foodResultRice );
    scenes.put(SceneIds.FOOD_RESULT_OTHER, foodResultOther  );
    scenes.put(SceneIds.END, end);
    scenes.put(SceneIds.ENDING_FOOD, ending_food);
    scenes.put(SceneIds.BLOCKED, blocked);

    log.info("SceneService initialized. scene count={}", scenes.size());
  }

  /**
   *  シーンメッセージのプレースホルダを解決する
   *   {favorite} や {player} を表示用文字列へ置換します。
   * @param scene 対象のシーン
   * @param favorite プレイヤーの好物
   * @param playerName 表示用のプレイヤー名
   */
  public void resolveMessage(SceneData scene,
      String favorite,
      String playerName) {

    String message = scene.getMessage();
    if (message == null) return;

    String originalMessage = message;

    if (message.contains("{favorite}")) {
      message = message.replace(
          "{favorite}",
          (favorite != null && !favorite.isBlank())
              ? favorite
              : "ハンバーグ"
      );
    }

    if (message.contains("{player}")) {
      message = message.replace(
          "{player}",
          (playerName != null && !playerName.isBlank())
              ? playerName
              : "あなた"
      );
    }
    log.debug("Resolve message: before={}, after={}", originalMessage, message);

    scene.setMessage(message);
  }


  /**
   * 指定シーンIDに対応するシーン情報を取得します。
   * 条件分岐やイベント判定を考慮した表示用 SceneData を返します。
   * @param id 取得したいシーンのID
   * @param playerItems プレイヤーの所持アイテム
   * @param foodEventUsed 好物イベント実行済みフラグ
   * @param favorite プレイヤーの好物
   * @return 表示用 SceneData
   */
  public SceneData getScene(
      String id, Set<String> playerItems,boolean foodEventUsed,String favorite) {
        return getSceneInternal(id, playerItems, foodEventUsed, favorite);
  }

  /**
   * 好物イベントを使用済みとして
   * マークすべきシーンか判定します。
   * @param sceneId 現在のシーンID
   * @return 使用済みとする場合 true
   */
  public boolean shouldMarkFoodEventUsed(String sceneId) {
    return sceneId.startsWith(SceneIds.FOOD_RESULT);
  }

  /**
   * シーン取得の内部処理を行います。
   *
   * 特殊イベントや条件分岐を考慮しながら、
   * 実際に表示する SceneData を決定する。
   * @param id シーンID
   * @param playerItems プレイヤーの所持アイテム
   * @param foodEventUsed 好物イベント実行済みフラグ
   * @param favorite プレイヤーの好物
   * @return 判定後の SceneData
   */
  private SceneData getSceneInternal(
      String id,
      Set<String> playerItems,
      boolean foodEventUsed,
      String favorite
  ) {
    SceneData original = scenes.getOrDefault(id, scenes.get(SceneIds.END));
    if (original == null){
      log.error("Scene not found: id={}", id);
      return scenes.get(SceneIds.END);
    }

    SceneData scene = original.clone();

    // deepCave 確率イベント
    applyDeepCaveEvent(id, playerItems, scene);

    // foodCheck → foodResult 分岐
    if (SceneIds.FOOD_CHECK.equals(id)) {
      FoodCategoryService.FoodCategory category =
          foodCategoryService.categorize(favorite);

      log.info("Food category decision: favorite={}, category={}",
          favorite, category);

      String nextId = switch (category) {
        case MEAT -> SceneIds.FOOD_RESULT_MEAT;
        case SWEET -> SceneIds.FOOD_RESULT_SWEET;
        case RICE -> SceneIds.FOOD_RESULT_RICE;
        default -> SceneIds.FOOD_RESULT_OTHER;
      };

      return getSceneInternal(nextId, playerItems, foodEventUsed, favorite);
    }

    return scene;
  }

  /**
   * deepCaveEvent 用の確率イベント処理を適用します。
   * 一定確率でアイテム取得イベントを発生させます。
   * @param id 現在のシーンID
   * @param playerItems プレイヤーの所持アイテム
   * @param scene 対象シーン
   */
  private void  applyDeepCaveEvent(String id, Set<String> playerItems, SceneData scene) {
    if (!SceneIds.DEEP_CAVE_EVENT.equals(id)) return;


    if (playerItems.contains("宝石")) {
      scene.setMessage("宝箱は空っぽだった。ここで見つかるものはもう無さそうだ。");
      return;
    }

    int chance = new SplittableRandom().nextInt(BOUND);

    log.info("DeepCave event: chance={}", chance);

    if (chance < RATE) {
      log.info("Item acquired: 宝石");
      scene.setMessage("宝箱を見つけた！中にはキラキラした宝石が入っていた！");
      scene.reward("宝石");
    } else {
      log.info("No item acquired");
      scene.setMessage("何も見つからなかった…。ただの岩だった。");
    }
  }


  /**
   * プレイヤー選択に応じて次シーンを決定します。
   * シーン遷移判定、条件チェック、必須アイテム判定を行います。
   * @param currentId 現在のシーンID
   * @param choice プレイヤーの選択肢
   * @param playerItems プレイヤーの所持アイテム
   * @param foodEventUsed 好物イベント実行済みフラグ
   * @param favorite プレイヤーの好物
   * @return 次に表示するシーン
   */
  public SceneData getNextScene(String currentId, String choice,Set<String> playerItems, boolean foodEventUsed,String favorite) {
    SceneData current = scenes.get(currentId);


    log.info("NextScene decision start: current={}, choice={}, items={}",
        currentId, choice, playerItems);

    if (current == null)
    {
      log.error("Current scene not found: {}", currentId);
      throw new GameException("Invalid scene: " + currentId);
    }

    String nextId = current.getNextSceneMap().getOrDefault(choice, SceneIds.END);

    log.info("TRANSITION: {} --({})--> {}", currentId, choice, nextId);

    SceneData next = getScene(nextId, playerItems, foodEventUsed, favorite);
    next.setPreviousSceneId(currentId);

    log.info("Next sceneId resolved: {} -> {}", currentId, nextId);

    log.info("Next scene result: {}", next.getId());
    return checkRequiredItem(next, playerItems);
  }

  /**
   * 指定シーンへの進行可否を判定します。
   * 必須アイテム不足時は進行不可とします。
   *
   * @param playerItems プレイヤーの所持アイテム
   * @param scene 対象シーン
   * @return 進行不可の場合 true
   */
  private boolean  isBlocked(Set<String> playerItems, SceneData scene) {
    return scene.getRequiredItem() != null
        && !playerItems.contains(scene.getRequiredItem());
  }


  /**
   * 進行不可時に表示するブロック用シーンを生成します。
   *
   * @return ブロック用 SceneData
   */
  private SceneData createBlockedScene() {
    return scenes.get(SceneIds.BLOCKED);
  }

  /**
   * シーンが要求する必須アイテムを
   * プレイヤーが所持しているかを確認し、
   * 進行不可の場合はブロック用シーンに差し替える。
   *
   * @param scene 判定対象のシーン
   * @param playerItems プレイヤーの所持アイテム一覧
   * @return 進行可能な場合は scene、進行不可の場合はブロック用 SceneData
   */
  private SceneData checkRequiredItem(SceneData scene, Set<String> playerItems) {
    if (isBlocked(playerItems, scene)) {
      log.warn("Blocked: scene={}, required={}, items={}",
          scene.getId(), scene.getRequiredItem(), playerItems);

      return createBlockedScene();
    }
    return scene;
  }

}