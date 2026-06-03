package moe.takochan.webnei.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.recipe.dto.CategoryMachineDto;
import moe.takochan.webnei.recipe.dto.CategoryVoltageTierDto;
import moe.takochan.webnei.recipe.dto.HandlerBreakdownDto;
import moe.takochan.webnei.recipe.dto.RecipeCategoryDto;
import moe.takochan.webnei.recipe.dto.RecipeDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final DatasetService datasetService;
    private final RecipeDao recipeDao;
    private final RecipeCategoryBrowserRepository categoryRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final JdbcClient jdbc;

    public RecipeService(DatasetService datasetService, RecipeDao recipeDao,
                        RecipeCategoryBrowserRepository categoryRepo,
                        AssetUrlBuilder assetUrlBuilder, JdbcClient jdbc) {
        this.datasetService = datasetService;
        this.recipeDao = recipeDao;
        this.categoryRepo = categoryRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.jdbc = jdbc;
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

        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 50;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("displayOrder").ascending()
                        .and(Sort.by("categoryId").ascending()));

        Page<RecipeCategoryBrowserEntity> result = categoryRepo.findAll(spec, pageable);
        List<RecipeCategoryDto> items = toDtoList(result.getContent(), dataset);
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public List<ModOptionDto> listCategoryMods(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.listCategoryMods(dataset);
    }

    public RecipeDto detail(String datasetId, String recipeId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.loadRecipe(dataset, recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + recipeId));
    }

    public PageResponse<RecipeDto> lookup(
            String datasetId, RecipeLookupQuery query, PageRequest page) {
        if (query.target() == null || query.target().isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        DatasetSummary dataset = datasetService.requireById(datasetId);
        RecipeDao.LookupPage idPage = recipeDao.lookupRecipeIds(dataset, query, page);
        List<RecipeDto> recipes = recipeDao.loadRecipes(dataset, idPage.recipeIds());
        return PageResponse.of(recipes, page, idPage.total());
    }

    public List<HandlerBreakdownDto> lookupBreakdown(String datasetId, RecipeLookupQuery query) {
        if (query.target() == null || query.target().isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.lookupBreakdown(dataset, query);
    }

    public PageResponse<RecipeDto> listRecipesByCategory(
            String datasetId, String categoryId, String query, String voltageTier, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        RecipeDao.LookupPage idPage =
                recipeDao.listRecipeIdsByCategory(dataset, categoryId, query, voltageTier, page);
        List<RecipeDto> recipes = recipeDao.loadRecipes(dataset, idPage.recipeIds());
        return PageResponse.of(recipes, page, idPage.total());
    }

    public List<CategoryMachineDto> listCategoryMachines(String datasetId, String categoryId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.listCategoryMachines(dataset, categoryId);
    }

    public List<CategoryVoltageTierDto> listCategoryVoltageTiers(
            String datasetId, String categoryId, String target, String kind) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.listCategoryVoltageTiers(dataset, categoryId, target, kind);
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
        List<String> resources = new ArrayList<>();
        for (RecipeCategoryBrowserEntity e : entities) {
            if (e.getIconAssetPath() == null
                    && e.getIconImageResource() != null
                    && !e.getIconImageResource().isBlank()) {
                resources.add(e.getIconImageResource());
            }
        }
        if (resources.isEmpty()) return Map.of();

        Map<String, String> fallback = new HashMap<>();
        jdbc.sql("SELECT resource, exported_path FROM nei_texture_export WHERE dataset_id = :id AND resource IN (:res)")
                .param("id", datasetId)
                .param("res", resources)
                .query((rs, n) -> fallback.put(rs.getString("resource"), rs.getString("exported_path")))
                .list();
        return fallback;
    }

    private RecipeCategoryDto toDto(RecipeCategoryBrowserEntity e, DatasetSummary dataset,
                                    Map<String, String> iconFallback) {
        String iconPath = e.getIconAssetPath();
        if (iconPath == null && e.getIconImageResource() != null) {
            iconPath = iconFallback.get(e.getIconImageResource());
        }
        return new RecipeCategoryDto(
                e.getCategoryId(),
                e.getPlugin(),
                e.getHandlerId(),
                e.getDisplayName(),
                e.isShapeless(),
                e.getIconItemVariantId(),
                e.getIconDisplayName(),
                assetUrlBuilder.build(dataset, iconPath, null),
                e.getIconInfo(),
                e.getItemInputWidth(),
                e.getItemInputHeight(),
                e.getFluidInputWidth(),
                e.getFluidInputHeight(),
                e.getItemOutputWidth(),
                e.getItemOutputHeight(),
                e.getFluidOutputWidth(),
                e.getFluidOutputHeight(),
                e.isSupportsRecipeLookup(),
                e.isSupportsUsageLookup(),
                e.getDisplayOrder(),
                e.getCanvasWidth(),
                e.getCanvasHeight(),
                assetUrlBuilder.build(dataset, e.getBackgroundAssetPath(), null),
                e.getRecipeCount(),
                e.getMachineCount(),
                e.getModId(),
                e.getModName(),
                e.getHandlerClass(),
                e.getHandlerCanvasWidth(),
                e.getHandlerCanvasHeight(),
                e.getHandlerYShift(),
                e.isHandlerMultipleWidgetsAllowed(),
                e.getIconImageResource(),
                e.getIconImageX(),
                e.getIconImageY(),
                e.getIconImageWidth(),
                e.getIconImageHeight(),
                e.getIconImageTextureWidth(),
                e.getIconImageTextureHeight());
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
        return (root, cq, cb) -> {
            Predicate[] conditions = {
                    cb.like(cb.lower(root.get("displayName")), pattern),
                    cb.like(cb.lower(root.get("categoryId")), pattern),
                    cb.like(cb.lower(root.get("handlerId")), pattern)
            };
            return cb.or(conditions);
        };
    }
}
