package plugin.TextAdventureApp.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.PlayerData;
import plugin.TextAdventureApp.repository.PlayerRepository;

/**
 * Spring Securityでユーザ情報を取得する為のクラス
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PlayerRepository playerRepository;

  /**
   * プレイヤー名を引数にし、データベースからプレーヤー情報を取得するメソッド
   * @param username　ログインフォームで入力されたプレイヤー名
   * @return　認証処理で使用されるユーザ情報
   * @throws UsernameNotFoundException　入力されたプレイヤー名に対応するプレイヤーが存在しない場合
   */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      PlayerData player = playerRepository.findByUsername(username);
      if(player == null){
        throw new UsernameNotFoundException("User not found: " + username);
      }
      return User.builder()
          .username(player.getUsername())
          .password(player.getPassword())
          .roles(player.getRole() !=null ? player.getRole() : "USER")
          .build();
    }
}
