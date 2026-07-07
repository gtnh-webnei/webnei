package moe.takochan.webnei.dto.dataset;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据集列表响应。{@code defaultId} 为后端配置的默认数据集 id（可为空），
 * 由前端在无本地选择时优先采用，替代原独立的 /dataset/default 接口。
 */
@Getter
@AllArgsConstructor
public class DatasetListResponse {

    private String defaultId;
    private List<DatasetListEntry> items;
}
