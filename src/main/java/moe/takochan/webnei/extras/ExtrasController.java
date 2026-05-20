package moe.takochan.webnei.extras;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class ExtrasController {

    private final ExtrasService extrasService;

    public ExtrasController(ExtrasService extrasService) {
        this.extrasService = extrasService;
    }

    @GetMapping("/items/{itemVariantId}/extras")
    public ItemExtras itemExtras(
            @PathVariable String datasetId,
            @PathVariable String itemVariantId) {
        return extrasService.itemExtras(datasetId, itemVariantId);
    }

    @GetMapping("/items/{itemVariantId}/containers")
    public List<FluidContainerEntry> itemContainers(
            @PathVariable String datasetId,
            @PathVariable String itemVariantId) {
        return extrasService.allContainersForItem(datasetId, itemVariantId);
    }

    @GetMapping("/fluids/{fluidVariantId}/extras")
    public FluidExtras fluidExtras(
            @PathVariable String datasetId,
            @PathVariable String fluidVariantId) {
        return extrasService.fluidExtras(datasetId, fluidVariantId);
    }
}
