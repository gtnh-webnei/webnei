package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("v_fluid_browser")
public class FluidBrowserRow {

    private String datasetId;

    @TableId(value = "fluid_id", type = IdType.INPUT)
    private String fluidId;

    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private String iconPath;
    private Integer iconWidth;
    private Integer iconHeight;
    private String iconMetadataJson;
}
