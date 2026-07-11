package moe.takochan.webnei.dto.aspect;

import lombok.Getter;

@Getter
public class AspectItemListRequest {

    private String datasetId;
    private String aspectId;
    private String q;
    private int page;
    private int size;
}
