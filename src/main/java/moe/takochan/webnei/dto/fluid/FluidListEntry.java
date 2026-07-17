package moe.takochan.webnei.dto.fluid;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FluidListEntry {

    private String id;
    private String displayName;
    private String modId;
    private String modName;
    private String registryName;
    private IconAsset icon;
}
