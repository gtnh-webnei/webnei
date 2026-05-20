package moe.takochan.webnei.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("webnei.cors")
public record CorsProperties(List<String> devOrigins) {

    public CorsProperties {
        if (devOrigins == null || devOrigins.isEmpty()) {
            devOrigins = List.of("http://localhost:5173");
        }
    }
}
