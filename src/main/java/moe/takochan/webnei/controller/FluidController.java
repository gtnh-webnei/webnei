package moe.takochan.webnei.controller;

import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.fluid.FluidListEntry;
import moe.takochan.webnei.dto.fluid.FluidListRequest;
import moe.takochan.webnei.service.FluidCatalogService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fluid")
public class FluidController {

    private final FluidCatalogService service;

    public FluidController(FluidCatalogService service) {
        this.service = service;
    }

    @PostMapping("/list")
    public PageResponse<FluidListEntry> list(@RequestBody FluidListRequest request) {
        return service.list(request.getDatasetId(), request.getQ(), request.getPage(), request.getSize());
    }
}
