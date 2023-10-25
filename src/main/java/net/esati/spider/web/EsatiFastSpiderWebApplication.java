package net.esati.spider.web;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsatiFastSpiderWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(EsatiFastSpiderWebApplication.class, args);
    }
}
