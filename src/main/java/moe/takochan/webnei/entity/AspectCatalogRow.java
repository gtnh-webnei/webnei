package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("v_aspect_browser")
public class AspectCatalogRow {

    private String datasetId;
    private String aspectId;
    private String displayName;
    private String description;
    private boolean primal;
    private int color;
    private String iconPath;
    private Integer iconWidth;
    private Integer iconHeight;
    private String iconMetadataJson;
    private long associatedItemCount;
}
