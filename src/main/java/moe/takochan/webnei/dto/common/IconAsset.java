package moe.takochan.webnei.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IconAsset {

    private String url;
    private Integer width;
    private Integer height;
    private String metadataJson;
}
