package io.potatoy.syiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // db date 자동 업데이트를 위해 사용
@SpringBootApplication
public class SyiaryApplication {

  public static void main(String[] args) {
    SpringApplication.run(SyiaryApplication.class, args);
  }

}
