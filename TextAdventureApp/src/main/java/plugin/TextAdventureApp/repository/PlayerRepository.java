package plugin.TextAdventureApp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plugin.TextAdventureApp.data.PlayerData;

/**
 * JpaRepositoryを継承して、プレーヤー情報に関するデータベース操作を行うインターフェース
 */
@Repository
public interface PlayerRepository extends JpaRepository<PlayerData, Long > {

  boolean existsByUsername(String username);
  //PlayerData findByUsername(String username);
  Optional<PlayerData> findByUsername(String username);
}