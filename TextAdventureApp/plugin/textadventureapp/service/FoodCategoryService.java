package plugin.textadventureapp.service;

import org.springframework.stereotype.Service;

/**
 * ユーザーの好物をカテゴリ分けするサービス
 */
@Service
  public class FoodCategoryService {

  public enum FoodCategory {
    MEAT,
    SWEET,
    RICE,
    OTHER
  }

  /**
   * ユーザーの好物をカテゴリ分けするメソッド
   * @param favorite　プレーヤーの好物
   * @return　カテゴライズされた好物情報
   */
    public FoodCategory categorize(String favorite) {
      if (favorite == null) return FoodCategory.OTHER;

      String f = favorite.toLowerCase();

      if (f.contains("肉") || f.contains("ステーキ") || f.contains("焼")) {
        return FoodCategory.MEAT;
      }
      if (f.contains("ケーキ") || f.contains("甘") || f.contains("チョコ")) {
        return FoodCategory.SWEET;
      }
      if (f.contains("ごはん") || f.contains("米") || f.contains("ラーメン")) {
        return FoodCategory.RICE;
      }
      return FoodCategory.OTHER;
    }
  }
