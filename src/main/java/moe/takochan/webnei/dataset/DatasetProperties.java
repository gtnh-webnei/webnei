package moe.takochan.webnei.dataset;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "webnei.dataset")
public class DatasetProperties {

    /** 默认数据集完整 id，来自环境变量 WEBNEI_DEFAULT_DATASET_ID；为空表示不指定。 */
    private String defaultId = "";
}
