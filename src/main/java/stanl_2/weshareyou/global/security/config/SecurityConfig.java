package stanl_2.weshareyou.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import stanl_2.weshareyou.global.security.filter.JWTTokenGeneratorFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("local")
public class SecurityConfig {

    @Bean
    SecurityFilterChain defalutSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrfConfig -> csrfConfig.disable());
        http.addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class)
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .authorizeHttpRequests((requests -> requests
                .requestMatchers("/api/v1/member/register").permitAll()
                .anyRequest().authenticated()));
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());

        return http.build();
    }




    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 사용자 비밀 번호가 유출 되었는지 확인하는 메소드
     * From Spring Security 6.3부터 도입
     * @return
     * */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    // 인증 메커니즘을 시작하는 역할
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder){
        // 인증 제공자 객체
        UsernamePwdAuthenticationProvider authenticationProvider =
                new UsernamePwdAuthenticationProvider(userDetailsService, passwordEncoder);

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        // provider manager는 Authentication 객체 내부의 비밀번호를 지우지 못하게 설정(유효성 검사)
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }

}