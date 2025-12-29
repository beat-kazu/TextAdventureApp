package plugin.TextAdventureApp.DTO;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadResponseDTO {
  private String currentSceneId;
  private String previousSceneId;
  private List<String> items;
  private Map<String, Boolean> flags;
}
