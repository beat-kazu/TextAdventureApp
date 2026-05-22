package plugin.textadventureapp.service;


import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import plugin.textadventureapp.data.PlayerData;
import plugin.textadventureapp.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import plugin.textadventureapp.util.JsonUtils;

/**
 * プレイヤーアカウント情報を管理するServiceです。
 * 認証情報、プロフィール情報、イベントフラグ管理を行います。
 */
@Slf4j
@Service
public class PlayerService {

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JsonUtils jsonUtils;

  /**
   * プレイヤー情報を登録します。
   * ユーザー名重複チェック、パスワード暗号化、初期ロール設定を行った上でDBへ保存します。
   * @param player　登録対象のプレイヤー情報
   */
  public void registerPlayer(PlayerData  player){

    if (player == null) {
      log.error("Register failed: player is null");
      throw new IllegalArgumentException("player is null");
    }

    log.info("Register attempt: username={}", player.getUsername());


    if (player.getUsername() == null || player.getUsername().isBlank()) {
      throw new IllegalArgumentException("username is empty");
    }

    if (player.getPassword() == null || player.getPassword().isBlank()) {
      throw new IllegalArgumentException("password is empty");
    }

    if(playerRepository.existsByUsername(player.getUsername())){
      log.warn("Register failed: username already exists={}", player.getUsername());
      throw new IllegalArgumentException("このユーザー名は既に使われています。");
    }


    player.setPassword(passwordEncoder.encode(player.getPassword()));

    player.setRole("USER");

    if (player.getPlayerFlags() == null) {
      player.setPlayerFlags("{}");
    }

    playerRepository.save(player);
    log.info("Register success: username={}", player.getUsername());
  }

  /**
   * ユーザー名を指定してプレイヤー情報を取得します。
   * ユーザーが存在しない場合はOptional.empty を返します。
   * @param username ユーザー名
   * @return プレイヤー情報
   */
  public Optional<PlayerData> findByUsername(String username) {
    log.debug("Player fetch: username={}", username);

    if (username == null || username.isBlank()) {
      log.warn("Player fetch skipped: username is null/blank");
      return Optional.empty();
    }

    Optional<PlayerData> player = playerRepository.findByUsername(username);

    log.debug("Player fetch result: username={}, found={}", username, player.isPresent());
    return player;
  }

  /**
   * プレイヤーに保存されているイベントフラグを取得します。
   * JSON形式で保存されたフラグ情報をMap形式へ変換して返します。
   * @param player 対象プレイヤー
   * @return イベントフラグ一覧
   */
  public Map<String, Boolean> getFlags(PlayerData player){
    log.info(">>> getFlags START: username={}", player.getUsername());
    String raw = player.getPlayerFlags();
    log.debug("RAW flags: {}", raw);
    return jsonUtils.toFlagMap(raw);

  }

  /**
   * プレイヤーのイベントフラグを保存します。
   * Map形式のイベントフラグをJSON形式へ変換して保存します。
   * @param player 対象プレイヤー
   * @param flags 保存するイベントフラグ
   */
  public void saveFlags(PlayerData player, Map<String, Boolean> flags) {
    log.info("Saving flags: username={}, flags={}", player.getUsername(), flags);
    try {
      String json = jsonUtils.toJson(flags);
      log.debug("Saving flags: username={}, json={}",
          player.getUsername(), json);

      player.setPlayerFlags(json);
      playerRepository.save(player);
      log.info("Flags saved successfully: username={}", player.getUsername());
    } catch (Exception e) {
      log.error("Failed to save flags: username={}, flags={}",
          player.getUsername(), flags, e);
      throw new GameException("playerFlags save failed");
    }
  }

  /**
   * プレイヤーの foodEventUsed フラグを true に更新します。
   * 対象プレイヤーのイベントフラグを取得し、foodEventUsed を設定して保存します。
   * @param username プレイヤーのユーザー名
   */
  @Transactional
  public void markFoodEventUsed(String username) {
    log.info(">>> markFoodEventUsed START: username={}", username);

    PlayerData player = findByUsername(username)
        .orElseThrow(() -> {
          log.error("Player not found for foodEventUsed: {}", username);
              return new GameException("Player not found");
            });
    Map<String, Boolean> flags = getFlags(player);

    log.debug("Flags before update: {}", flags);


    // foodEventUsed を立てる
    flags.put("foodEventUsed", true);
    log.info("foodEventUsed set: username={}, flags={}", username, flags);
    // 既存メソッドを使用
    saveFlags(player, flags);
  }

  /**
   * プレイヤーのイベントフラグを指定内容で全置換します。
   * 既存フラグを新しいフラグ情報へ置き換えます。
   * @param username プレイヤーのユーザー名
   * @param flags 新しいイベントフラグ
   */
  @Transactional
  public void replaceFlags(String username, Map<String, Boolean> flags) {
    log.warn("Replacing ALL flags: username={}, newFlags={}", username, flags);
    PlayerData player = playerRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.error("Player not found: username={}", username);
          return new GameException("Player not found");
        });

    String json = jsonUtils.toJson(flags);
    player.setPlayerFlags(json);
  }

}
