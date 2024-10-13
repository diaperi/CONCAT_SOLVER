package concat.SolverWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class SolverWebApplication {

    public static void main(String[] args) {

        // .env 파일을 로드합니다.
        Dotenv dotenv = Dotenv.load();

        // 환경 변수를 System property로 설정
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        SpringApplication.run(SolverWebApplication.class, args);
    }

}
