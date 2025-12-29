package plugin.TextAdventureApp.DTO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import plugin.TextAdventureApp.data.SaveData;

  @Data
  public class SaveResponseDTO {
    private String username;
    private String currentSceneId;
    private String previousSceneId;
    private List<String> items;
    private LocalDateTime updatedAt;

}
