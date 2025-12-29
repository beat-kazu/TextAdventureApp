package plugin.TextAdventureApp;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import plugin.TextAdventureApp.data.PlayerData;
import plugin.TextAdventureApp.repository.PlayerRepository;
import plugin.TextAdventureApp.service.PlayerService;
import plugin.TextAdventureApp.service.SaveDataService;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

  @InjectMocks
  PlayerService playerService;

  @Mock
  PlayerRepository playerRepository;

  //@Mock
  //ObjectMapper objectMapper;

  @Mock
  PasswordEncoder passwordEncoder;

  @Captor
  ArgumentCaptor<PlayerData> playerCaptor;

  @Mock
  ObjectMapper objectMapper;

  @Test
  void markFoodEventUsedを呼ぶと_foodEventUsedがtrueで保存される() throws Exception {
    // given
    PlayerData player = new PlayerData();
    player.setUsername("testuser");
    player.setPlayerFlags("{}");

    when(playerRepository.findByUsername("testuser"))
        .thenReturn(Optional.of(player));

    when(objectMapper.readValue(
        eq("{}"),
        any(TypeReference.class)
    )).thenReturn(new HashMap<>());

    when(objectMapper.writeValueAsString(anyMap()))
        .thenAnswer(invocation -> {
          Map<String, Object> map = invocation.getArgument(0);
          return map.toString(); // JSON厳密でなくてOK
        });

    // when
    playerService.markFoodEventUsed("testuser");

    // then
    verify(playerRepository).save(playerCaptor.capture());

    PlayerData savedPlayer = playerCaptor.getValue();
    assertTrue(savedPlayer.getPlayerFlags().contains("foodEventUsed"));
  }

//  @Test
//  void markFoodEventUsed_既存フラグがあっても_foodEventUsedが追加される() throws Exception {
//    // --- 準備 ---
//    PlayerData player = new PlayerData();
//    player.setUsername("testUser");
//
//    // 既存フラグを持つ状態
//    player.setPlayerFlags("{\"alreadyUsed\":true}");
//
//    when(playerRepository.findByUsername("testUser"))
//        .thenReturn(Optional.of(player));
//
//    // --- 実行 ---
//    playerService.markFoodEventUsed("testUser");
//
//    // --- 検証 ---
//    verify(playerRepository).save(argThat(savedPlayer -> {
//      try {
//        Map<String, Object> flags =
//            objectMapper.readValue(
//                savedPlayer.getPlayerFlags(),
//                new TypeReference<Map<String, Object>>() {}
//            );
//
//        assertEquals(true, flags.get("alreadyUsed"));
//        assertEquals(true, flags.get("foodEventUsed"));
//        return true;
//
//      } catch (Exception e) {
//        fail("JSONの読み取りに失敗しました: " + e.getMessage());
//        return false;
//      }
//    }));
//
//  }

//  @Test
//  void markFoodEventUsed_フラグが空Mapの場合_foodEventUsedが保存される() {
//    PlayerData player = new PlayerData();
//    player.setUsername("testUser");
//    player.setPlayerFlags("{}"); // 正常前提
//
//    when(playerRepository.findByUsername("testUser"))
//        .thenReturn(Optional.of(player));
//
//    playerService.markFoodEventUsed("testUser");
//
//    ArgumentCaptor<PlayerData> captor = ArgumentCaptor.forClass(PlayerData.class);
//    verify(playerRepository).save(captor.capture());
//
//    PlayerData saved = captor.getValue();
//
//    // ★ JSON文字列として中身を確認するだけ
//    assertTrue(saved.getPlayerFlags().contains("foodEventUsed"));
//  }



}
