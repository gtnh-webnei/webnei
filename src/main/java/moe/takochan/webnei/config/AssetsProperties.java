package moe.takochan.webnei.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("webnei.assets")
public record AssetsProperties(String root, String publicUrl) {

    public AssetsProperties {
        if (root == null || root.isBlank()) {
            root = "./assets";
        }
        if (publicUrl == null || publicUrl.isBlank()) {
            publicUrl = "/assets";
        } else {
            publicUrl = publicUrl.replaceAll("/+$", "");
        }
    }

    public boolean servesLocal() {
        return publicUrl.startsWith("/");
    }
}
