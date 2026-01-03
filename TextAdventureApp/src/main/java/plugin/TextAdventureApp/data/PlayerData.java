package plugin.textadventureapp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

/**
 * プレイヤー情報のDBテーブルの構成を定義
 */
@Entity
@Table(name = "player_data", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class PlayerData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  private String role;

  private String nickname;
  private String favorite;

  @Column(columnDefinition = "json")
  private String playerFlags;


}

