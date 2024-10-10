package concat.SolverWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolverWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolverWebApplication.class, args);
    }

}
