package plugin.TextAdventureApp.DTO;

import lombok.Data;
import java.util.List;

@Data
public class SaveRequestDTO {

  private String username;
  private String currentSceneId;
  private String previousSceneId;
  private List<String> items;
}
