package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("v_recipe_category_browser")
public class RecipeCategoryBrowserRow {

    private String datasetId;

    @TableId(value = "category_id", type = IdType.INPUT)
    private String categoryId;

    private String displayName;
    private String modId;
    private String modName;
    private long recipeCount;
    private String iconPath;
    private Integer iconWidth;
    private Integer iconHeight;
    private String iconMetadataJson;
}
