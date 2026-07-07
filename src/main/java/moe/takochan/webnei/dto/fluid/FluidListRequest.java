package moe.takochan.webnei.dto.fluid;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FluidListRequest {

    private String datasetId;
    private String q;
    private int page;
    private int size;
}
