package moe.takochan.webnei.dto.aspect;

import moe.takochan.webnei.dto.common.IconAsset;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AspectBrief {

    private String id;
    private String displayName;
    private int color;
    private IconAsset icon;
}
