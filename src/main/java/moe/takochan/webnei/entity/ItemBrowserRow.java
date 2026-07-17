package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("v_item_browser")
public class ItemBrowserRow {

    private String datasetId;

    @TableId(value = "item_variant_id", type = IdType.INPUT)
    private String itemVariantId;

    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private int listIndex;
    private String iconPath;
    private Integer iconWidth;
    private Integer iconHeight;
    private String iconMetadataJson;
}
