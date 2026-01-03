package plugin.textadventureapp.service;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plugin.textadventureapp.data.PlayerData;

/**
 * 表示用にプレイヤー情報（好物・名前）を提供する サービス
 */
@Service
public class FavoriteService {

  @Autowired
  private PlayerService playerService;

  /**
   * 現在のプレイヤーの favorite を返す
   * ゲストプレイまたは未ログインの場合、
   * あるいは favorite が未設定の場合は null を返す。
   * @param principal　ログイン中のユーザー情報
   * @param isGuest　ゲストプレイかどうか
   * @return　favorite（存在しない場合は null）
   */
  public String getFavorite(Principal principal, boolean isGuest) {
    if (principal == null || isGuest) {
      return null;
    }

    return playerService.findByUsername(principal.getName())
        .map(PlayerData::getFavorite)
        .filter(f -> f != null && !f.isBlank())
        .orElse(null);
  }


  /**
   * 表示用の favorite（好物）を取得
   * @param principal ログイン中のユーザー情報
   * @param isGuest ゲストプレイかどうか
   * @return 表示用の favorite
   */
  public String getFavoriteForDisplay(Principal principal, boolean isGuest) {
    String favorite = getFavorite(principal, isGuest);
    return favorite != null ? favorite : "ハンバーグ";
  }

  /**
   * 表示用のプレイヤー名を取得
   * @param principal ログイン中のユーザー情報
   * @param isGuest ゲストプレイかどうか
   * @return 表示用プレイヤー名（例: ○○さん）
   */
  public String getPlayerNameForDisplay(Principal principal, boolean isGuest) {
    if (isGuest || principal == null) return null;

    return playerService.findByUsername(principal.getName())
        .map(PlayerData::getNickname)
        .filter(n -> n != null && !n.isBlank())
        .map(n -> n + "さん")
        .orElse(null);
  }
}
