package stanl_2.weshareyou.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import stanl_2.weshareyou.domain.chat.dateConverter.DateToTimestampConverter;

//@Configuration
//public class WebConfig {
//
//
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("http://localhost:5173") // Vue 개발 서버 도메인 허용
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
//            }
//        };
//    }
//}
@Configuration
public class WebConfig {

    private final DateToTimestampConverter dateToTimestampConverter;

    public WebConfig(DateToTimestampConverter dateToTimestampConverter) {
        this.dateToTimestampConverter = dateToTimestampConverter;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            // CORS 설정
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:30080") // Vue 개발 서버 도메인 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization", "Last-Event-ID");
            }

            // 변환기 추가
            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(dateToTimestampConverter);
            }
        };
    }
}