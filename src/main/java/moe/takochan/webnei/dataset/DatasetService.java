package moe.takochan.webnei.dataset;

import java.util.List;
import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "modId", "modId",
            "name", "name",
            "version", "version",
            "sourceType", "sourceType",
            "sourceFileName", "sourceFileName",
            "enabled", "enabled");

    private final DatasetRepository datasetRepo;
    private final ModRepository modRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final ObjectMapper objectMapper;

    public DatasetService(DatasetRepository datasetRepo, ModRepository modRepo,
                          AssetUrlBuilder assetUrlBuilder,
                          ObjectMapper objectMapper) {
        this.datasetRepo = datasetRepo;
        this.modRepo = modRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.objectMapper = objectMapper;
    }

    public List<DatasetSummary> list() {
        return datasetRepo.findAll(Sort.by("packSlug", "packVersion", "variant", "language"))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public DatasetSummary requireById(String datasetId) {
        return datasetRepo.findById(datasetId)
                .map(this::toSummary)
                .orElseThrow(() -> new NotFoundException("Dataset not found: " + datasetId));
    }

    public DatasetDetail detail(DatasetSummary dataset) {
        List<ModSummary> mods = modRepo.findByDatasetIdOrderByNameAsc(dataset.datasetId())
                .stream()
                .map(this::toModSummary)
                .toList();
        return new DatasetDetail(dataset, mods);
    }

    public PageResponse<ModSummary> listModsPage(
            DatasetSummary dataset, String query, String sortField, boolean descending, PageRequest page) {
        String datasetId = dataset.datasetId();

        String sortColumn = sortField != null ? SORT_COLUMNS.getOrDefault(sortField, "modId") : "modId";
        Sort sort = descending ? Sort.by(sortColumn).descending() : Sort.by(sortColumn).ascending();
        sort = sort.and(Sort.by("modId").ascending());
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 50;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageIndex, pageSize, sort);

        Specification<ModEntity> spec = hasDatasetId(datasetId);
        if (query != null && !query.isBlank()) {
            spec = spec.and(textSearch(query));
        }

        Page<ModEntity> result = modRepo.findAll(spec, pageable);
        List<ModSummary> items = result.stream().map(this::toModSummary).toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    private DatasetSummary toSummary(DatasetEntity e) {
        DatasetSummary base = new DatasetSummary(
                e.getDatasetId(), e.getPackSlug(), e.getPackVersion(), e.getVariant(),
                e.getLanguage(), e.getDisplayName(), e.getSchemaVersion(), e.getExporterVersion(),
                e.getMinecraftVersion(), e.getCreatedAt(), parsePlugins(e.getActivePlugins()),
                null, null);
        return withDisplaySpecUrl(base);
    }

    private List<String> parsePlugins(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to parse active_plugins JSON", ex);
        }
    }

    private static Specification<ModEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<ModEntity> textSearch(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> {
            Predicate[] conditions = {
                    cb.like(cb.lower(root.get("modId")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("version")), pattern),
                    cb.like(cb.lower(root.get("sourceFileName")), pattern)
            };
            return cb.or(conditions);
        };
    }

    private ModSummary toModSummary(ModEntity e) {
        return new ModSummary(e.getModId(), e.getName(), e.getVersion(),
                e.getSourceType(), e.getSourceFileName(), e.getSourceSha256(), e.isEnabled());
    }

    /** spec 文件外挂在 dataset 资源目录下：<assets>/<pack>/<ver>/<variant>/spec/display.json + i18n/<language>.json */
    private DatasetSummary withDisplaySpecUrl(DatasetSummary ds) {
        String specUrl = assetUrlBuilder.build(ds, "spec/display.json", null);
        String messagesUrl = assetUrlBuilder.build(ds, "spec/i18n/" + ds.language() + ".json", null);
        return new DatasetSummary(
                ds.datasetId(), ds.packSlug(), ds.packVersion(), ds.variant(),
                ds.language(), ds.displayName(), ds.schemaVersion(), ds.exporterVersion(),
                ds.minecraftVersion(), ds.createdAt(), ds.activePlugins(), specUrl, messagesUrl);
    }
}
