package plugin.textadventureapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import plugin.textadventureapp.service.SaveDataService;
import plugin.textadventureapp.service.SceneService;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class AdvControllerTest {


  @Autowired
  MockMvc mockMvc;

  @MockBean
  SceneService sceneService;

  @MockBean
  SaveDataService saveDataService;

  @Test
  void home_ログイン済みで表示される() throws Exception {
    mockMvc.perform(get("/home"))
        .andExpect(status().isOk());
  }
}
