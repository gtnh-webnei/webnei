package moe.takochan.webnei.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.recipe.dto.CategoryMachineDto;
import moe.takochan.webnei.recipe.dto.CategoryVoltageTierDto;
import moe.takochan.webnei.recipe.dto.RecipeCategoryDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RecipeCategoryService {

    private final DatasetService datasetService;
    private final RecipeCategoryBrowserRepository categoryRepo;
    private final RecipeCategoryMachineBrowserRepository machineRepo;
    private final RecipeCategoryVoltageTierRepository voltageTierRepo;
    private final RecipeLookupVoltageTierRepository lookupVoltageTierRepo;
    private final GregTechRecipeRepository gregTechRecipeRepo;
    private final NeiTextureExportRepository textureRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public RecipeCategoryService(DatasetService datasetService,
                                 RecipeCategoryBrowserRepository categoryRepo,
                                 RecipeCategoryMachineBrowserRepository machineRepo,
                                 RecipeCategoryVoltageTierRepository voltageTierRepo,
                                 RecipeLookupVoltageTierRepository lookupVoltageTierRepo,
                                 GregTechRecipeRepository gregTechRecipeRepo,
                                 NeiTextureExportRepository textureRepo,
                                 AssetUrlBuilder assetUrlBuilder) {
        this.datasetService = datasetService;
        this.categoryRepo = categoryRepo;
        this.machineRepo = machineRepo;
        this.voltageTierRepo = voltageTierRepo;
        this.lookupVoltageTierRepo = lookupVoltageTierRepo;
        this.gregTechRecipeRepo = gregTechRecipeRepo;
        this.textureRepo = textureRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<RecipeCategoryDto> listCategories(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        List<RecipeCategoryBrowserEntity> entities = categoryRepo.findAll(
                hasDatasetId(datasetId),
                Sort.by("displayOrder").ascending().and(Sort.by("categoryId").ascending()));
        return toDtoList(entities, dataset);
    }

    public PageResponse<RecipeCategoryDto> listCategoriesPage(
            String datasetId, String query, String modId, boolean hideEmpty, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);

        Specification<RecipeCategoryBrowserEntity> spec = hasDatasetId(datasetId);
        if (modId != null && !modId.isBlank()) {
            spec = spec.and(modIdEq(modId));
        }
        if (hideEmpty) {
            spec = spec.and(recipeCountGt0());
        }
        if (query != null && !query.isBlank()) {
            spec = spec.and(categorySearch(query));
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page.page(), page.size(),
                Sort.by("displayOrder").ascending()
                        .and(Sort.by("categoryId").ascending()));

        Page<RecipeCategoryBrowserEntity> result = categoryRepo.findAll(spec, pageable);
        List<RecipeCategoryDto> items = toDtoList(result.getContent(), dataset);
        return new PageResponse<>(items, page.page(), page.size(), result.getTotalElements());
    }

    public List<ModOptionDto> listCategoryMods(String datasetId) {
        datasetService.requireById(datasetId);
        Map<String, String> mods = new HashMap<>();
        for (RecipeCategoryBrowserEntity e : categoryRepo.findAll(hasDatasetId(datasetId))) {
            if (e.getModId() != null && !e.getModId().isBlank()) {
                mods.putIfAbsent(e.getModId(), e.getModName() == null || e.getModName().isBlank() ? e.getModId() : e.getModName());
            }
        }
        return mods.entrySet().stream()
                .map(e -> new ModOptionDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ModOptionDto::name).thenComparing(ModOptionDto::modId))
                .toList();
    }

    public List<CategoryMachineDto> listCategoryMachines(String datasetId, String categoryId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return machineRepo.findByDatasetIdAndCategoryId(
                        datasetId,
                        categoryId,
                        Sort.by("displayOrder").ascending().and(Sort.by("itemVariantId").ascending()))
                .stream()
                .map(e -> new CategoryMachineDto(
                        e.getItemVariantId(),
                        e.getDisplayName(),
                        assetUrlBuilder.build(dataset, e.getAssetPath(), null),
                        e.getRole(),
                        e.getDisplayOrder()))
                .toList();
    }

    public List<CategoryVoltageTierDto> listCategoryVoltageTiers(
            String datasetId, String categoryId, String target, String kind, String query) {
        datasetService.requireById(datasetId);
        boolean hasLookup = target != null && !target.isBlank() && kind != null && !kind.isBlank();
        if (hasLookup) {
            return lookupVoltageTierRepo.findByDatasetIdAndCategoryIdAndTargetIdAndLookupKindOrderByMinVoltageAsc(
                            datasetId, categoryId, target, kind)
                    .stream()
                    .map(e -> new CategoryVoltageTierDto(e.getVoltageTier(), e.getRecipeCount()))
                    .toList();
        }
        if (query != null && !query.isBlank()) {
            return gregTechRecipeRepo.findVoltageTiersByCategoryAndSearch(
                            datasetId, categoryId, "%" + query.trim().toLowerCase() + "%")
                    .stream()
                    .map(row -> new CategoryVoltageTierDto((String) row[0], (Long) row[1]))
                    .toList();
        }
        return voltageTierRepo.findByDatasetIdAndCategoryIdOrderByMinVoltageAsc(datasetId, categoryId)
                .stream()
                .map(e -> new CategoryVoltageTierDto(e.getVoltageTier(), e.getRecipeCount()))
                .toList();
    }

    private List<RecipeCategoryDto> toDtoList(List<RecipeCategoryBrowserEntity> entities, DatasetSummary dataset) {
        Map<String, String> iconFallback = resolveIconFallbacks(entities, dataset.datasetId());
        List<RecipeCategoryDto> dtos = new ArrayList<>(entities.size());
        for (RecipeCategoryBrowserEntity e : entities) {
            dtos.add(toDto(e, dataset, iconFallback));
        }
        return dtos;
    }

    private Map<String, String> resolveIconFallbacks(List<RecipeCategoryBrowserEntity> entities, String datasetId) {
        List<String> resources = entities.stream()
                .filter(e -> e.getIconAssetPath() == null)
                .map(RecipeCategoryBrowserEntity::getIconImageResource)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
        return loadTextureFallbacks(datasetId, resources);
    }

    private Map<String, String> loadTextureFallbacks(String datasetId, List<String> resources) {
        if (resources.isEmpty()) return Map.of();
        Map<String, String> fallback = new HashMap<>();
        for (NeiTextureExportEntity e : textureRepo.findByDatasetIdAndResourceIn(datasetId, resources)) {
            fallback.put(e.getResource(), e.getExportedPath());
        }
        return fallback;
    }

    private static String iconPath(String iconAssetPath, String iconImageResource, Map<String, String> iconFallback) {
        return iconAssetPath != null ? iconAssetPath : iconFallback.get(iconImageResource);
    }

    private RecipeCategoryDto toDto(RecipeCategoryBrowserEntity e, DatasetSummary dataset,
                                    Map<String, String> iconFallback) {
        return new RecipeCategoryDto(
                e.getCategoryId(),
                e.getHandlerId(),
                e.getDisplayName(),
                e.isShapeless(),
                assetUrlBuilder.build(dataset, iconPath(e.getIconAssetPath(), e.getIconImageResource(), iconFallback), null),
                e.getItemInputWidth(),
                e.getItemInputHeight(),
                e.getFluidInputWidth(),
                e.getFluidInputHeight(),
                e.getItemOutputWidth(),
                e.getItemOutputHeight(),
                e.getFluidOutputWidth(),
                e.getFluidOutputHeight(),
                e.getRecipeCount(),
                e.getMachineCount(),
                e.getModName(),
                e.getHandlerClass());
    }

    private static Specification<RecipeCategoryBrowserEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<RecipeCategoryBrowserEntity> modIdEq(String modId) {
        return (root, query, cb) -> cb.equal(root.get("modId"), modId);
    }

    private static Specification<RecipeCategoryBrowserEntity> recipeCountGt0() {
        return (root, query, cb) -> cb.gt(root.get("recipeCount"), 0L);
    }

    private static Specification<RecipeCategoryBrowserEntity> categorySearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("displayName")), pattern),
                cb.like(cb.lower(root.get("categoryId")), pattern),
                cb.like(cb.lower(root.get("handlerId")), pattern));
    }
}
