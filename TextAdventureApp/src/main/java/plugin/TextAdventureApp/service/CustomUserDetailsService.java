package plugin.TextAdventureApp.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import plugin.TextAdventureApp.data.PlayerData;
import plugin.TextAdventureApp.repository.PlayerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      PlayerData player = playerRepository.findByUsername(username);
      if(player == null){
        throw new UsernameNotFoundException("User not found: " + username);
      }
      return User.builder()
          .username(player.getUsername())
          .password(player.getPassword())
          .roles(player.getRole() !=null ? player.getRole() : "USER")
          .build();
    }
}
