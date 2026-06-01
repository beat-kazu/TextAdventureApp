package plugin.textadventureapp.DTO;

import lombok.Data;
import java.util.List;

/**
 * クライアントから送信されるセーブ要求を受け取るための DTO。
 */
@Data
public class SaveRequestDTO {

  private String username;
  private String currentSceneId;
  private String previousSceneId;
  private List<String> items;
}
