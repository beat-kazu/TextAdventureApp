package plugin.TextAdventureApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.PlayerData;
import plugin.TextAdventureApp.repository.PlayerRepository;

/**
 * DBにプレーヤー情報を登録するクラス
 */
@Service
public class PlayerService {

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * プレーヤー情報を重複チェックや暗号化などをして、DBに登録するメソッド
   * @param player　ログインフォームから入力されたプレーヤー情報
   */
  public void registerPlayer(PlayerData  player){

    if(playerRepository.existsByUsername(player.getUsername())){
        throw new IllegalArgumentException("このユーザー名は既に使われています。");
    }

    player.setPassword(passwordEncoder.encode(player.getPassword()));

    player.setRole("USER");

    if (player.getPlayerFlags() == null) {
      player.setPlayerFlags("{}");
    }

    playerRepository.save(player);

  }
  public Optional<PlayerData> findByUsername(String username) {
    return playerRepository.findByUsername(username);
  }

  public Map<String, Object> getFlags(PlayerData player) {
    try {
      if (player.getPlayerFlags() == null || player.getPlayerFlags().isBlank()) {
        return new HashMap<>();
      }
      return objectMapper.readValue(
          player.getPlayerFlags(),
          new TypeReference<Map<String, Object>>() {}
      );
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  public void saveFlags(PlayerData player, Map<String, Object> flags) {
    try {
      player.setPlayerFlags(objectMapper.writeValueAsString(flags));
      playerRepository.save(player);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save playerFlags", e);
    }
  }

  @Transactional
  public void markFoodEventUsed(String username) {
    PlayerData player = findByUsername(username)
        .orElseThrow();

    Map<String, Object> flags = new HashMap<>();

    // 既存フラグを読み込み
    if (player.getPlayerFlags() != null) {
      try {
        flags = objectMapper.readValue(
            player.getPlayerFlags(),
            new TypeReference<Map<String, Object>>() {}
        );
      } catch (Exception e) {
        throw new RuntimeException("Failed to read playerFlags", e);
      }
    }

    // foodEventUsed を立てる
    flags.put("foodEventUsed", true);

    // ★ 既存メソッドを使用
    saveFlags(player, flags);
  }

  @Transactional
  public void replaceFlags(String username, Map<String, Boolean> flags) {
    PlayerData player = playerRepository.findByUsername(username)
        .orElseThrow();

    try {
      String json = objectMapper.writeValueAsString(flags);
      player.setPlayerFlags(json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
