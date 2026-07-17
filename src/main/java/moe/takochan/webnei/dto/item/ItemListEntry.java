package moe.takochan.webnei.dto.item;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemListEntry {

    private String id;
    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private IconAsset icon;
}
