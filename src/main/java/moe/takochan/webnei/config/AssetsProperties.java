package moe.takochan.webnei.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("webnei.assets")
public record AssetsProperties(String publicUrl) {

    public AssetsProperties {
        if (publicUrl == null || publicUrl.isBlank()) {
            publicUrl = "/assets";
        } else {
            publicUrl = publicUrl.replaceAll("/+$", "");
        }
    }
}
