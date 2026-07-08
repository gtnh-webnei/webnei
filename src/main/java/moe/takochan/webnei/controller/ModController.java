package moe.takochan.webnei.controller;

import java.util.List;

import moe.takochan.webnei.dto.mod.ModListEntry;
import moe.takochan.webnei.dto.mod.ModListRequest;
import moe.takochan.webnei.service.ModCatalogService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mod")
public class ModController {

    private final ModCatalogService service;

    public ModController(ModCatalogService service) {
        this.service = service;
    }

    @PostMapping("/list")
    public List<ModListEntry> list(@RequestBody ModListRequest request) {
        return service.list(request.getDatasetId(), request.getQ());
    }
}
