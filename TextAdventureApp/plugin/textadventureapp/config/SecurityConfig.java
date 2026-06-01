package plugin.textadventureapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * アプリ全体の認証・認可設定を行うクラスです。
 * Spring Securityフレームワークを活用して、ログイン認証、アクセス制御、ログアウト処理、
 * パスワードハッシュ化の設定を管理します。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * HTTPリクエストに対するセキュリティ設定を行います。
   * URL毎のアクセス許可設定、ログイン画面遷移、
   * ログアウト時のセッション破棄などを定義します。
   * @param http　HTTPリクエストのセキュリティ設定を構築するためのオブジェクト
   * @return　構築済みのSecurityFilterChain
   * @throws Exception　セキュリティ設定の構築中にエラーが発生した場合
   */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/register","/home","/login","/api/save", "/guest/**","/game/**","/css/**").permitAll()
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
              .loginPage("/login")
              .defaultSuccessUrl("/home", true)
              .permitAll()
          )
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout")
              .invalidateHttpSession(true)
              .deleteCookies("JSESSIONID")
              .permitAll()
          );
      return http.build();
    }

  /**
   * パスワードをハッシュ化するためのPasswordEncoderを生成します。
   * BCryptアルゴリズムを使用して、パスワードを安全に暗号化します。
   * @return　パスワードをハッシュ化するためのインスタンス
   */
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }


}
