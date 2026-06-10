package moe.takochan.webnei.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.CategoryApplicableItemDto;
import moe.takochan.webnei.model.dto.CategoryVoltageTierDto;
import moe.takochan.webnei.model.dto.DatasetSummary;
import moe.takochan.webnei.model.dto.ModOptionDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.model.dto.RecipeCategoryDto;
import moe.takochan.webnei.model.entity.view.RecipeCategoryBrowserEntity;
import moe.takochan.webnei.repository.table.RecipeFilterTagRepository;
import moe.takochan.webnei.repository.view.RecipeCategoryBrowserRepository;
import moe.takochan.webnei.repository.view.RecipeCategoryApplicableItemBrowserRepository;
import moe.takochan.webnei.repository.view.RecipeCategoryVoltageTierRepository;
import moe.takochan.webnei.repository.view.RecipeLookupVoltageTierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RecipeCategoryService {

    private final RecipeCategoryBrowserRepository categoryRepo;
    private final RecipeCategoryApplicableItemBrowserRepository applicableItemRepo;
    private final RecipeCategoryVoltageTierRepository voltageTierRepo;
    private final RecipeLookupVoltageTierRepository lookupVoltageTierRepo;
    private final RecipeFilterTagRepository filterTagRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public RecipeCategoryService(RecipeCategoryBrowserRepository categoryRepo,
                                 RecipeCategoryApplicableItemBrowserRepository applicableItemRepo,
                                 RecipeCategoryVoltageTierRepository voltageTierRepo,
                                 RecipeLookupVoltageTierRepository lookupVoltageTierRepo,
                                 RecipeFilterTagRepository filterTagRepo,
                                 AssetUrlBuilder assetUrlBuilder) {
        this.categoryRepo = categoryRepo;
        this.applicableItemRepo = applicableItemRepo;
        this.voltageTierRepo = voltageTierRepo;
        this.lookupVoltageTierRepo = lookupVoltageTierRepo;
        this.filterTagRepo = filterTagRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<RecipeCategoryDto> listCategories(DatasetSummary dataset) {
        String datasetId = dataset.datasetId();
        List<RecipeCategoryBrowserEntity> entities = categoryRepo.findAll(
                hasDatasetId(datasetId),
                Sort.by("displayOrder").ascending().and(Sort.by("categoryId").ascending()));
        return toDtoList(entities, dataset);
    }

    public PageResponse<RecipeCategoryDto> listCategoriesPage(
            DatasetSummary dataset, String query, String modId, boolean hideEmpty, PageRequest page) {
        String datasetId = dataset.datasetId();
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

    public List<ModOptionDto> listCategoryMods(DatasetSummary dataset) {
        return categoryRepo.findModOptions(dataset.datasetId())
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .sorted(Comparator.comparing(ModOptionDto::name).thenComparing(ModOptionDto::modId))
                .toList();
    }

    public List<CategoryApplicableItemDto> listCategoryApplicableItems(DatasetSummary dataset, String categoryId) {
        String datasetId = dataset.datasetId();
        return applicableItemRepo.findByDatasetIdAndCategoryId(
                        datasetId,
                        categoryId,
                        Sort.by("displayOrder").ascending().and(Sort.by("itemVariantId").ascending()))
                .stream()
                .map(e -> new CategoryApplicableItemDto(
                        e.getItemVariantId(),
                        e.getDisplayName(),
                        assetUrlBuilder.build(dataset, e.getAssetPath(), null),
                        e.getRole(),
                        e.getDisplayOrder()))
                .toList();
    }

    public List<CategoryVoltageTierDto> listCategoryVoltageTiers(
            DatasetSummary dataset, String categoryId, String target, String kind, String query) {
        String datasetId = dataset.datasetId();
        boolean hasLookup = target != null && !target.isBlank() && kind != null && !kind.isBlank();
        if (hasLookup) {
            return lookupVoltageTierRepo.findByDatasetIdAndCategoryIdAndTargetIdAndLookupKindOrderByMinVoltageAsc(
                            datasetId, categoryId, target, kind)
                    .stream()
                    .map(e -> new CategoryVoltageTierDto(e.getVoltageTier(), e.getRecipeCount()))
                    .toList();
        }
        if (query != null && !query.isBlank()) {
            return filterTagRepo.findTagValuesByCategoryAndSearch(
                            datasetId, categoryId, "voltage_tier", "%" + query.trim().toLowerCase() + "%")
                    .stream()
                    .map(row -> new CategoryVoltageTierDto(row.getTagValue(), row.getRecipeCount()))
                    .toList();
        }
        return voltageTierRepo.findByDatasetIdAndCategoryIdOrderByMinVoltageAsc(datasetId, categoryId)
                .stream()
                .map(e -> new CategoryVoltageTierDto(e.getVoltageTier(), e.getRecipeCount()))
                .toList();
    }

    private List<RecipeCategoryDto> toDtoList(List<RecipeCategoryBrowserEntity> entities, DatasetSummary dataset) {
        List<RecipeCategoryDto> dtos = new ArrayList<>(entities.size());
        for (RecipeCategoryBrowserEntity e : entities) {
            dtos.add(toDto(e, dataset));
        }
        return dtos;
    }

    private RecipeCategoryDto toDto(RecipeCategoryBrowserEntity e, DatasetSummary dataset) {
        return new RecipeCategoryDto(
                e.getCategoryId(),
                e.getHandlerId(),
                e.getDisplayName(),
                e.isShapeless(),
                assetUrlBuilder.build(dataset, e.getIconAssetPath(), null),
                e.getItemInputWidth(),
                e.getItemInputHeight(),
                e.getFluidInputWidth(),
                e.getFluidInputHeight(),
                e.getItemOutputWidth(),
                e.getItemOutputHeight(),
                e.getFluidOutputWidth(),
                e.getFluidOutputHeight(),
                e.getRecipeCount(),
                e.getApplicableItemCount(),
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
