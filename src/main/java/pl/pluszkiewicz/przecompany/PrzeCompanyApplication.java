package pl.pluszkiewicz.przecompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class PrzeCompanyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrzeCompanyApplication.class, args);
    }

}
