package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("v_aspect_item_browser")
public class AspectItemBrowserRow {

    private String datasetId;
    private String aspectId;
    private String itemVariantId;
    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private int amount;
    private String iconPath;
    private Integer iconWidth;
    private Integer iconHeight;
    private String iconMetadataJson;
}
