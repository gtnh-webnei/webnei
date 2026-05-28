package moe.takochan.webnei.recipe;

import java.util.List;

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

import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final DatasetService datasetService;
    private final RecipeDao recipeDao;

    public RecipeService(DatasetService datasetService, RecipeDao recipeDao) {
        this.datasetService = datasetService;
        this.recipeDao = recipeDao;
    }

    public List<RecipeCategoryDto> listCategories(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.listCategories(dataset);
    }

    public PageResponse<RecipeCategoryDto> listCategoriesPage(
            String datasetId, String query, String plugin, boolean hideEmpty, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.listCategoriesPage(dataset, query, plugin, hideEmpty, page);
    }

    public List<String> listCategoryPlugins(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return recipeDao.listCategoryPlugins(dataset);
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
}
