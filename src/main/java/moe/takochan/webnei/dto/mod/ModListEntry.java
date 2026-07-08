package moe.takochan.webnei.dto.mod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModListEntry {

    private String id;
    private String modId;
    private String name;
    private String version;
}
