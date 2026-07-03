package moe.takochan.webnei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("moe.takochan.webnei")
@SpringBootApplication
public class WebneiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebneiApplication.class, args);
    }
}
