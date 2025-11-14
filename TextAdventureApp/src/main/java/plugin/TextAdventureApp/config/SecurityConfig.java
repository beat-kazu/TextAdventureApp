package plugin.TextAdventureApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

/**
 * Spring Securityフレームワークを活用して、ログイン認証を行うclassです。
 * アプリ全体のセキュリティ設定を行います。
 */
public class SecurityConfig {

  /**
   * リクエスト毎に許可/不許可を選別し、ログイン認証後の振る舞いを設定
   * @param http　HTTPリクエストのセキュリティ設定を構築するためのオブジェクト
   * @return　構築済みのSecurityFilterChain
   * @throws Exception　セキュリティ設定の構築中にエラーが発生した場合
   */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/register","/home","/login", "/guest/**","/css/**").permitAll()
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
              .loginPage("/login")
              .defaultSuccessUrl("/start", true)
              .permitAll()
          )
          .logout(logout -> logout.permitAll());
      return http.build();
    }

  /**
   * パスワードをハッシュ化するためのインターフェース
   * @return　パスワードをハッシュ化するためのインスタンス
   */
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }


}
