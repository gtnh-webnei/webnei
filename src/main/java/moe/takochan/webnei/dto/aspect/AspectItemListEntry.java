package moe.takochan.webnei.dto.aspect;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AspectItemListEntry {

    private String id;
    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private int amount;
    private IconAsset icon;
}
