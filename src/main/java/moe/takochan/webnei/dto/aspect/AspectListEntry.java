package moe.takochan.webnei.dto.aspect;

import java.util.List;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AspectListEntry {

    private String id;
    private String displayName;
    private String description;
    private boolean primal;
    private int color;
    private IconAsset icon;
    private List<AspectBrief> components;
    private long associatedItemCount;
}
