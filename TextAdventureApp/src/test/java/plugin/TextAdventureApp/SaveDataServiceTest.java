package plugin.TextAdventureApp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import plugin.TextAdventureApp.data.SaveData;
import plugin.TextAdventureApp.repository.PlayerRepository;
import plugin.TextAdventureApp.repository.SaveDataRepository;
import plugin.TextAdventureApp.service.SaveDataService;

@ExtendWith(MockitoExtension.class)
class SaveDataServiceTest {

  @Mock
  SaveDataRepository saveDataRepository;

  @Mock
  PlayerRepository playerRepository;

  SaveDataService saveDataService;

  @BeforeEach
  void setUp() {
    saveDataService = new SaveDataService(saveDataRepository, playerRepository);
  }

  @Test
  void loadSaveData_既存データがある場合はそれを返す() {

    // Arrange
    SaveData saveData = new SaveData();
    when(saveDataRepository.findByPlayer_Username("test"))
        .thenReturn(Optional.of(saveData));

    // Act
    SaveData result = saveDataService.loadSaveData("test");

    // Assert
    assertNotNull(result);
    verify(saveDataRepository).findByPlayer_Username("test");
  }

}
