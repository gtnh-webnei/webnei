package moe.takochan.webnei.dto.item;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemListRequest {

    private String datasetId;
    private String q;
    private int page;
    private int size;
}
