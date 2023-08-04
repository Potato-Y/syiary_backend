package io.potatoy.syiary.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt") // 자바 클래스에 프로피티값을 가져와 사용하는 애너테이션
public class JwtProperties {
    // application.yml 값들을 변수로 접근하는데 사용할 클래스
    private String issuer;
    private String secretKey;
}
