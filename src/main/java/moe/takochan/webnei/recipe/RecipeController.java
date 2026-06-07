package moe.takochan.webnei.recipe;

import java.util.List;

import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.recipe.dto.CategoryMachineDto;
import moe.takochan.webnei.recipe.dto.CategoryVoltageTierDto;
import moe.takochan.webnei.recipe.dto.HandlerBreakdownDto;
import moe.takochan.webnei.recipe.dto.RecipeCategoryDto;
import moe.takochan.webnei.recipe.dto.RecipeDto;

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

    private final RecipeCategoryService recipeCategoryService;
    private final RecipeService recipeService;

    public RecipeController(RecipeCategoryService recipeCategoryService, RecipeService recipeService) {
        this.recipeCategoryService = recipeCategoryService;
        this.recipeService = recipeService;
    }

    @GetMapping("/recipe-categories")
    public List<RecipeCategoryDto> listCategories(@PathVariable String datasetId) {
        return recipeCategoryService.listCategories(datasetId);
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
                datasetId, q, modId, hideEmpty, PageRequest.of(page, size, DEFAULT_CATEGORY_PAGE_SIZE, MAX_CATEGORY_PAGE_SIZE));
    }

    @GetMapping("/recipe-categories/mods")
    public List<ModOptionDto> listCategoryMods(@PathVariable String datasetId) {
        return recipeCategoryService.listCategoryMods(datasetId);
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
                datasetId,
                new RecipeLookupQuery(target, kind, handlerId, categoryId, voltageTier),
                PageRequest.of(page, size, DEFAULT_LOOKUP_PAGE_SIZE, MAX_LOOKUP_PAGE_SIZE));
    }

    @GetMapping("/recipes/lookup/breakdown")
    public List<HandlerBreakdownDto> lookupBreakdown(
            @PathVariable String datasetId,
            @RequestParam String target,
            @RequestParam(defaultValue = "recipe") String kind) {
        return recipeService.lookupBreakdown(datasetId, new RecipeLookupQuery(target, kind));
    }

    @GetMapping("/recipes/{recipeId}")
    public RecipeDto detail(@PathVariable String datasetId, @PathVariable String recipeId) {
        return recipeService.detail(datasetId, recipeId);
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
                datasetId, categoryId, q, voltageTier, PageRequest.of(page, size, DEFAULT_CATEGORY_PAGE_SIZE, MAX_CATEGORY_PAGE_SIZE));
    }

    @GetMapping("/categories/{categoryId}/machines")
    public List<CategoryMachineDto> listCategoryMachines(
            @PathVariable String datasetId, @PathVariable String categoryId) {
        return recipeCategoryService.listCategoryMachines(datasetId, categoryId);
    }

    @GetMapping("/categories/{categoryId}/voltage-tiers")
    public List<CategoryVoltageTierDto> listCategoryVoltageTiers(
            @PathVariable String datasetId,
            @PathVariable String categoryId,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String q) {
        return recipeCategoryService.listCategoryVoltageTiers(datasetId, categoryId, target, kind, q);
    }
}
