package moe.takochan.webnei.gtore;

import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}/gt")
public class GtOreController {

    private final GtOreService gtOreService;

    public GtOreController(GtOreService gtOreService) {
        this.gtOreService = gtOreService;
    }

    @GetMapping("/ore-veins")
    public PageResponse<GtOreVeinSummary> listOreVeins(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return gtOreService.listOreVeins(
                datasetId, q, dimension, materialName, PageRequest.of(page, size == null ? 50 : size));
    }

    @GetMapping("/ore-veins/{veinName}")
    public GtOreVeinDetail oreVeinDetail(@PathVariable String datasetId, @PathVariable String veinName) {
        return gtOreService.oreVeinDetail(datasetId, veinName);
    }

    @GetMapping("/small-ores")
    public PageResponse<GtSmallOreSummary> listSmallOres(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return gtOreService.listSmallOres(datasetId, q, dimension, PageRequest.of(page, size == null ? 50 : size));
    }

    @GetMapping("/small-ores/{oreGenName}")
    public GtSmallOreDetail smallOreDetail(@PathVariable String datasetId, @PathVariable String oreGenName) {
        return gtOreService.smallOreDetail(datasetId, oreGenName);
    }

    @GetMapping("/underground-fluids")
    public PageResponse<GtUndergroundFluidSummary> listUndergroundFluids(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return gtOreService.listUndergroundFluids(
                datasetId, q, dimension, PageRequest.of(page, size == null ? 50 : size));
    }

    @GetMapping("/underground-fluids/{fluidId}/{dimension}")
    public GtUndergroundFluidDetail undergroundFluidDetail(
            @PathVariable String datasetId, @PathVariable String fluidId, @PathVariable String dimension) {
        return gtOreService.undergroundFluidDetail(datasetId, fluidId, dimension);
    }

    @GetMapping("/bartworks-ores")
    public PageResponse<GtBartWorksOreSummary> listBartWorksOres(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return gtOreService.listBartWorksOres(
                datasetId, q, type, dimension, PageRequest.of(page, size == null ? 50 : size));
    }

    @GetMapping("/bartworks-ores/{entryId}")
    public GtBartWorksOreDetail bartWorksOreDetail(@PathVariable String datasetId, @PathVariable String entryId) {
        return gtOreService.bartWorksOreDetail(datasetId, entryId);
    }
}
