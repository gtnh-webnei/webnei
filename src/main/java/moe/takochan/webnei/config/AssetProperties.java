package moe.takochan.webnei.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "webnei.assets")
public class AssetProperties {

    private String publicUrl = "/assets";
}
