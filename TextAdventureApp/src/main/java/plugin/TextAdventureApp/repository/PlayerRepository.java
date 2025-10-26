package plugin.TextAdventureApp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plugin.TextAdventureApp.data.PlayerData;

// Entityクラス（PlayerData）を扱うRepositoryの定義
@Repository
public interface PlayerRepository extends JpaRepository<PlayerData, Long > {

  boolean existsByUsername(String username);
  // 👇 これを追加（UserDetailsServiceで使う）
  PlayerData findByUsername(String username);
}