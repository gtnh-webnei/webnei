package moe.takochan.webnei.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeCategoryListRequest {

    private String datasetId;
    private String q;
    private int page;
    private int size;
}
