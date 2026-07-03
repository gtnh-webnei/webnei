package moe.takochan.webnei.dataset.api;

import java.util.List;

import moe.takochan.webnei.dataset.DatasetProperties;
import moe.takochan.webnei.dataset.service.DatasetService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService service;
    private final DatasetProperties properties;

    public DatasetController(DatasetService service, DatasetProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @GetMapping
    public List<DatasetSummary> list() {
        return service.list();
    }

    @GetMapping("/default")
    public DatasetDefault defaultDataset() {
        String defaultId = properties.getDefaultId();
        return new DatasetDefault(defaultId == null || defaultId.isBlank() ? null : defaultId);
    }
}
