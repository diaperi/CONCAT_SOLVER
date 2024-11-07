package concat.SolverWeb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/images/")
                .setCachePeriod(0); // 캐시를 비활성화하여 새 파일이 즉시 로드되도록 설정

        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");

    }
}
