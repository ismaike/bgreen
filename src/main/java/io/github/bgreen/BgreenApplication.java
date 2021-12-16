package io.github.bgreen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author maike
 * @date 2021/12/7 8:02 下午
 */
@SpringBootApplication
public class BgreenApplication {

    public static void main(String[] args) {
        SpringApplication.run(BgreenApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
