package plugin.textadventureapp.service;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 画面表示用のプレイヤー情報を提供するServiceです。
 * 好きな食べ物やプレイヤー名など、表示向けに加工した情報を取得します。
 */
@Slf4j
@Service
public class FavoriteService {

  @Autowired
  private PlayerService playerService;

  /**
   * ログイン中プレイヤーの好きな食べ物を取得します。
   * ゲストプレイ中、未ログイン、または favorite 未設定時は null を返します。
   *
   * @param principal 　ログイン中のユーザーの識別情報
   * @param isGuest   　ゲストプレイ中かどうか
   * @return　好きな食べ物（取得できない場合は null）
   */
  public String getFavorite(Principal principal, boolean isGuest) {
    if (principal == null) {
      log.debug("getFavorite: principal is null");
      return null;
    }

    if (isGuest) {
      log.debug("getFavorite: guest mode");
      return null;
    }

    String username = principal.getName();
    log.debug("getFavorite: username={}", username);


    try {
      return playerService.findByUsername(username)
          .map(player -> {
            String fav = player.getFavorite();
            if (fav == null || fav.isBlank()) {
              log.debug("getFavorite: favorite is empty username={}", username);
              return null;
            }
            return fav;
          })
          .orElseGet(() -> {
            log.warn("getFavorite: player not found username={}", username);
            return null;
          });
    } catch (Exception e) {
      log.error("getFavorite failed: username={}", username, e);
      return null;
    }

  }


  /**
   * 画面表示用の好きな食べ物を取得します。
   *
   * favorite が取得できない場合は、デフォルト値を返します。
   *
   * @param principal ログイン中ユーザーの識別情報
   * @param isGuest   ゲストプレイ中かどうか
   * @return 表示用の好きな食べ物
   */
  public String getFavoriteForDisplay(Principal principal, boolean isGuest) {
    String favorite = getFavorite(principal, isGuest);

    if (favorite == null) {
      log.debug("getFavoriteForDisplay: fallback to default");
      return "ハンバーグ";
    }
    return favorite;
  }

  /**
   * 画面表示用のプレイヤー名を取得します。
   * ニックネーム取得時は、表示用に「さん」を付与します。
   * @param principal ログイン中ユーザーの識別情報
   * @param isGuest   ゲストプレイ中かどうか
   * @return 表示用プレイヤー名（例: ○○さん）
   */
  public String getPlayerNameForDisplay(Principal principal, boolean isGuest) {
    if (principal == null) {
      log.debug("getPlayerName: principal is null");
      return null;
    }

    if (isGuest) {
      log.debug("getPlayerName: guest mode");
      return null;
    }
    String username = principal.getName();

    try {
      return playerService.findByUsername(username)
          .map(player -> {
            String name = player.getNickname();
            if (name == null || name.isBlank()) {
              log.debug("getPlayerName: nickname empty username={}", username);
              return null;
            }
            return name + "さん";
          })
          .orElseGet(() -> {
            log.warn("getPlayerName: player not found username={}", username);
            return null;
          });

    } catch (Exception e) {
      log.error("getPlayerName failed: username={}", username, e);
      return null;
    }
  }
}
