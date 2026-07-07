package moe.takochan.webnei.dto.item;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemListEntry {

    private String id;
    private String itemVariantId;
    private String itemId;
    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private int damage;
    private int listIndex;
    private IconAsset icon;
    private String tooltipText;
    private String chemicalExpression;
}
