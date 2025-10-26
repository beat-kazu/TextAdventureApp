package plugin.TextAdventureApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.PlayerData;
import plugin.TextAdventureApp.repository.PlayerRepository;

@Service
public class PlayerService {

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  // 新規登録処理
  public void registerPlayer(PlayerData  player){

    //重複チェック
    if(playerRepository.existsByUsername(player.getUsername())){
        throw new IllegalArgumentException("このユーザー名は既に使われています。");
    }

    // パスワード暗号化
    player.setPassword(passwordEncoder.encode(player.getPassword()));

    //ロール設定(デフォルトUSER)
    player.setRole("USER");

    //DB保存
    playerRepository.save(player);

  }

}
