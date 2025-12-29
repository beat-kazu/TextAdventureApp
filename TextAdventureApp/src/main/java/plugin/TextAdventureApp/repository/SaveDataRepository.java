package plugin.TextAdventureApp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import plugin.TextAdventureApp.data.SaveData;

public interface SaveDataRepository extends JpaRepository<SaveData,Long> {
  Optional<SaveData> findByPlayer_Username(String username);

  boolean existsByPlayer_Username(String username);
}
