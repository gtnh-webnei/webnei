package moe.takochan.webnei.dto.recipe;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeCategoryListEntry {

    private String id;
    private String categoryId;
    private String displayName;
    private String modId;
    private String modName;
    private long recipeCount;
    private IconAsset icon;
}
