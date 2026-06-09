package moe.takochan.webnei.service;

import moe.takochan.webnei.model.dto.DatasetSummary;
import org.springframework.stereotype.Component;

@Component
public class DatasetResolver {

    private final DatasetService datasetService;

    public DatasetResolver(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    public DatasetSummary resolve(String datasetId) {
        return datasetService.requireById(datasetId);
    }
}
