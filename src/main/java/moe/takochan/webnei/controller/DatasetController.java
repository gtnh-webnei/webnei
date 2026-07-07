package moe.takochan.webnei.controller;

import moe.takochan.webnei.service.DatasetService;
import moe.takochan.webnei.dto.dataset.DatasetListResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataset")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public DatasetListResponse list() {
        return service.list();
    }
}
