package moe.takochan.webnei.extras;

import java.util.List;
import java.util.Map;

import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.stereotype.Service;

@Service
public class ExtrasService {

    private final DatasetService datasetService;
    private final ExtrasDao extrasDao;

    public ExtrasService(DatasetService datasetService, ExtrasDao extrasDao) {
        this.datasetService = datasetService;
        this.extrasDao = extrasDao;
    }

    public ItemExtras itemExtras(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        int preview = ExtrasDao.containerPreviewLimit();

        List<String> oreNames = extrasDao.listOreDictNames(dataset, itemVariantId);
        List<FluidContainerEntry> containers = extrasDao.listContainersForItem(dataset, itemVariantId, preview);
        long containerTotal = containers.size() < preview
                ? containers.size()
                : extrasDao.countContainersForItem(dataset, itemVariantId);
        List<AspectEntry> aspects = extrasDao.listAspectsForItem(dataset, itemVariantId);
        Map<String, Long> counts = extrasDao.recipeCounts(dataset, "item", itemVariantId);

        return new ItemExtras(
                oreNames,
                containers,
                containerTotal,
                aspects,
                counts.getOrDefault("usage", 0L),
                counts.getOrDefault("recipe", 0L));
    }

    public List<FluidContainerEntry> allContainersForItem(String datasetId, String itemVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return extrasDao.listContainersForItem(dataset, itemVariantId, null);
    }

    public FluidExtras fluidExtras(String datasetId, String fluidVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        List<FluidContainerEntry> containers = extrasDao.listContainersForFluid(dataset, fluidVariantId);
        List<FluidBlockEntry> blocks = extrasDao.listBlocksForFluid(dataset, fluidVariantId);
        Map<String, Long> counts = extrasDao.recipeCounts(dataset, "fluid", fluidVariantId);

        return new FluidExtras(
                containers,
                blocks,
                counts.getOrDefault("usage", 0L),
                counts.getOrDefault("recipe", 0L));
    }
}
