package plugin.TextAdventureApp.service;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.PlayerData;

@Service
public class FavoriteService {

  @Autowired
  private PlayerService playerService;

  /**
   * 現在のプレイヤーの favorite を返す
   * ・ゲスト or 未ログイン → null
   * ・未設定 → null
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
   * 表示用 favorite（null を安全にデフォルトへ変換）
   */
  public String getFavoriteForDisplay(Principal principal, boolean isGuest) {
    String favorite = getFavorite(principal, isGuest);
    return favorite != null ? favorite : "ハンバーグ";
  }

  public String getPlayerNameForDisplay(Principal principal, boolean isGuest) {
    if (isGuest || principal == null) return null;

    return playerService.findByUsername(principal.getName())
        .map(PlayerData::getNickname)
        .filter(n -> n != null && !n.isBlank())
        .map(n -> n + "さん")
        .orElse(null);
  }
}
