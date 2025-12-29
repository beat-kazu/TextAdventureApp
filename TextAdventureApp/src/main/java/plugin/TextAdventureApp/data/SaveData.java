package plugin.TextAdventureApp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter

/**
 *
 */
@Entity
@Table(name = "save_data", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class SaveData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "username", referencedColumnName = "username")
  private PlayerData player;

  @Column(name = "current_scene_id")
  private String currentSceneId;

  @Column(name = "previous_scene_id")
  private String previousSceneId;

  @Column(columnDefinition = "json")
  private String items;

  @Column(columnDefinition = "json")
  private String flags;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;
  }
