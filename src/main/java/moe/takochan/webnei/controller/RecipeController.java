package moe.takochan.webnei.controller;

import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.recipe.RecipeCategoryListEntry;
import moe.takochan.webnei.dto.recipe.RecipeCategoryListRequest;
import moe.takochan.webnei.service.RecipeCatalogService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final RecipeCatalogService service;

    public RecipeController(RecipeCatalogService service) {
        this.service = service;
    }

    @PostMapping("/categories/list")
    public PageResponse<RecipeCategoryListEntry> listCategories(@RequestBody RecipeCategoryListRequest request) {
        return service.listCategories(request.getDatasetId(), request.getQ(), request.getPage(), request.getSize());
    }
}
