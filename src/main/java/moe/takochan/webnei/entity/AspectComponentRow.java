package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("v_aspect_component_browser")
public class AspectComponentRow {

    private String datasetId;
    private String parentAspectId;
    private int componentIndex;
    private String aspectId;
    private String displayName;
    private int color;
    private String iconPath;
    private Integer iconWidth;
    private Integer iconHeight;
    private String iconMetadataJson;
}
