package plugin.TextAdventureApp.service;

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

    playerRepository.save(player);

  }

}
