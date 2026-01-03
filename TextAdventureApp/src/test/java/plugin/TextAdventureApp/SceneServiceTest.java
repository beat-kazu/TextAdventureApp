package plugin.textadventureapp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import plugin.textadventureapp.data.SceneData;
import plugin.textadventureapp.service.FoodCategoryService;
import plugin.textadventureapp.service.SceneService;

class SceneServiceTest {

//  @Test
//  void テストが実行できることを確認() {
//    assertTrue(true);
//  }
@Mock
FoodCategoryService foodCategoryService;

  @Test
  void startシーンが取得できること() {

    // Arrange
    SceneService sceneService = new SceneService(foodCategoryService);
    sceneService.init(); // ★ これが必要

    Set<String> playerItems = Set.of();
    boolean foodEventUsed = false;
    String favorite = "りんご";

    // Act
    SceneData scene = sceneService.getScene(
        "start", playerItems, foodEventUsed, favorite);

    // Assert
    assertNotNull(scene);
    assertEquals("start", scene.getId());
  }

  @Test
  void 存在しないIDの場合はendシーンになること() {

    // Arrange
    SceneService sceneService = new SceneService(foodCategoryService);
    sceneService.init();

    Set<String> playerItems = Set.of();
    boolean foodEventUsed = false;
    String favorite = "りんご";

    // Act
    SceneData scene = sceneService.getScene(
        "unknown-scene-id",
        playerItems,
        foodEventUsed,
        favorite
    );

    // Assert
    assertNotNull(scene);
    assertEquals("end", scene.getId());
  }

  @Test
  void foodCheckからMEATの場合はfoodResultMeatに分岐すること() {

    // Arrange
    FoodCategoryService foodCategoryService = new FoodCategoryService();
    SceneService sceneService = new SceneService(foodCategoryService);
    sceneService.init();

    Set<String> playerItems = Set.of();
    boolean foodEventUsed = false;

    // MEAT に分類される好物
    String favorite = "ステーキ";

    // Act
    SceneData scene = sceneService.getScene(
        "foodCheck",
        playerItems,
        foodEventUsed,
        favorite
    );

    // Assert
    assertNotNull(scene);
    assertEquals("foodResultMeat", scene.getId());
  }


}

