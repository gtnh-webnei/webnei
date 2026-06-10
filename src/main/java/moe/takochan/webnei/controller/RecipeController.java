package moe.takochan.webnei.controller;

import java.util.List;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.CategoryApplicableItemDto;
import moe.takochan.webnei.model.dto.CategoryVoltageTierDto;
import moe.takochan.webnei.model.dto.HandlerBreakdownDto;
import moe.takochan.webnei.model.dto.LookupTargetHeaderDto;
import moe.takochan.webnei.model.dto.ModOptionDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.model.dto.RecipeCategoryDto;
import moe.takochan.webnei.model.dto.RecipeDto;
import moe.takochan.webnei.model.query.RecipeLookupQuery;
import moe.takochan.webnei.service.DatasetResolver;
import moe.takochan.webnei.service.RecipeCategoryService;
import moe.takochan.webnei.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets/{datasetId}")
public class RecipeController {

    private static final int DEFAULT_CATEGORY_PAGE_SIZE = 24;
    private static final int MAX_CATEGORY_PAGE_SIZE = 96;
    private static final int DEFAULT_LOOKUP_PAGE_SIZE = 12;
    private static final int MAX_LOOKUP_PAGE_SIZE = 48;

    private final DatasetResolver datasetResolver;
    private final RecipeCategoryService recipeCategoryService;
    private final RecipeService recipeService;

    public RecipeController(DatasetResolver datasetResolver, RecipeCategoryService recipeCategoryService, RecipeService recipeService) {
        this.datasetResolver = datasetResolver;
        this.recipeCategoryService = recipeCategoryService;
        this.recipeService = recipeService;
    }

    @GetMapping("/recipe-categories")
    public List<RecipeCategoryDto> listCategories(@PathVariable String datasetId) {
        return recipeCategoryService.listCategories(datasetResolver.resolve(datasetId));
    }

    @GetMapping("/recipe-categories/page")
    public PageResponse<RecipeCategoryDto> listCategoriesPage(
            @PathVariable String datasetId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String modId,
            @RequestParam(defaultValue = "false") boolean hideEmpty,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return recipeCategoryService.listCategoriesPage(
                datasetResolver.resolve(datasetId), q, modId, hideEmpty, PageRequest.of(page, size, DEFAULT_CATEGORY_PAGE_SIZE, MAX_CATEGORY_PAGE_SIZE));
    }

    @GetMapping("/recipe-categories/mods")
    public List<ModOptionDto> listCategoryMods(@PathVariable String datasetId) {
        return recipeCategoryService.listCategoryMods(datasetResolver.resolve(datasetId));
    }

    @GetMapping("/recipes/lookup")
    public PageResponse<RecipeDto> lookup(
            @PathVariable String datasetId,
            @RequestParam String target,
            @RequestParam(defaultValue = "recipe") String kind,
            @RequestParam(required = false) String handlerId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String voltageTier,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return recipeService.lookup(
                datasetResolver.resolve(datasetId),
                new RecipeLookupQuery(target, kind, handlerId, categoryId, voltageTier),
                PageRequest.of(page, size, DEFAULT_LOOKUP_PAGE_SIZE, MAX_LOOKUP_PAGE_SIZE));
    }

    @GetMapping("/recipes/lookup/breakdown")
    public List<HandlerBreakdownDto> lookupBreakdown(
            @PathVariable String datasetId,
            @RequestParam String target,
            @RequestParam(defaultValue = "recipe") String kind) {
        return recipeService.lookupBreakdown(datasetResolver.resolve(datasetId), new RecipeLookupQuery(target, kind));
    }

    @GetMapping("/recipes/lookup/target")
    public LookupTargetHeaderDto lookupTargetHeader(
            @PathVariable String datasetId,
            @RequestParam String target) {
        return recipeService.lookupTargetHeader(datasetResolver.resolve(datasetId), target);
    }

    @GetMapping("/recipes/{recipeId}")
    public RecipeDto detail(@PathVariable String datasetId, @PathVariable String recipeId) {
        return recipeService.detail(datasetResolver.resolve(datasetId), recipeId);
    }

    @GetMapping("/categories/{categoryId}/recipes")
    public PageResponse<RecipeDto> listByCategory(
            @PathVariable String datasetId,
            @PathVariable String categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String voltageTier,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return recipeService.listRecipesByCategory(
                datasetResolver.resolve(datasetId), categoryId, q, voltageTier, PageRequest.of(page, size, DEFAULT_CATEGORY_PAGE_SIZE, MAX_CATEGORY_PAGE_SIZE));
    }

    @GetMapping("/categories/{categoryId}/applicable-items")
    public List<CategoryApplicableItemDto> listCategoryApplicableItems(
            @PathVariable String datasetId, @PathVariable String categoryId) {
        return recipeCategoryService.listCategoryApplicableItems(datasetResolver.resolve(datasetId), categoryId);
    }

    @GetMapping("/categories/{categoryId}/voltage-tiers")
    public List<CategoryVoltageTierDto> listCategoryVoltageTiers(
            @PathVariable String datasetId,
            @PathVariable String categoryId,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String q) {
        return recipeCategoryService.listCategoryVoltageTiers(datasetResolver.resolve(datasetId), categoryId, target, kind, q);
    }
}
