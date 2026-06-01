package plugin.textadventureapp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import plugin.textadventureapp.data.SaveData;

/**
 * JpaRepositoryを継承して、セーブデータ情報に関するデータベース操作を行うインターフェース
 */
public interface SaveDataRepository extends JpaRepository<SaveData,Long> {
  Optional<SaveData> findByPlayer_Username(String username);

  boolean existsByPlayer_Username(String username);
}
