package moe.takochan.webnei.dto.mod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModListRequest {

    private String datasetId;
    private String q;
}
