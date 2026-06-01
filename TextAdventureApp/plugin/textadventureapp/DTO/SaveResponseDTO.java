package plugin.textadventureapp.DTO;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * セーブ処理完了後の結果をクライアントへ返却する DTO
 */
  @Data
  public class SaveResponseDTO {
    private String username;
    private String currentSceneId;
    private String previousSceneId;
    private List<String> items;
    private LocalDateTime updatedAt;

}
