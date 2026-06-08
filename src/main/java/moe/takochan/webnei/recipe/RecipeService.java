package moe.takochan.webnei.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.fluid.FluidModOptionRepository;
import moe.takochan.webnei.fluid.FluidVariantBrowserEntity;
import moe.takochan.webnei.fluid.FluidVariantRepository;
import moe.takochan.webnei.item.ItemModOptionRepository;
import moe.takochan.webnei.item.ItemVariantBrowserEntity;
import moe.takochan.webnei.item.ItemVariantRepository;
import moe.takochan.webnei.recipe.dto.CategoryBreakdownDto;
import moe.takochan.webnei.recipe.dto.GregTechRecipeDto;
import moe.takochan.webnei.recipe.dto.GregTechSpecialItemDto;
import moe.takochan.webnei.recipe.dto.HandlerBreakdownDto;
import moe.takochan.webnei.recipe.dto.LookupTargetHeaderDto;
import moe.takochan.webnei.recipe.dto.MetadataValueDto;
import moe.takochan.webnei.recipe.dto.RecipeDto;
import moe.takochan.webnei.recipe.dto.RecipeSlotCandidateDto;
import moe.takochan.webnei.recipe.dto.RecipeSlotDto;
import moe.takochan.webnei.recipe.dto.SlotLayoutDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final RecipeBrowserRepository recipeRepo;
    private final RecipeLookupBrowserRepository lookupRepo;
    private final RecipeLookupBreakdownRepository lookupBreakdownRepo;
    private final RecipeSlotBrowserRepository slotRepo;
    private final IngredientEntryRepository ingredientEntryRepo;
    private final RecipeSlotLayoutRepository slotLayoutRepo;
    private final GregTechRecipeRepository gregTechRecipeRepo;
    private final GregTechRecipeMetadataRepository metadataRepo;
    private final GregTechSpecialItemRepository specialItemRepo;
    private final ItemVariantRepository itemVariantRepo;
    private final FluidVariantRepository fluidVariantRepo;
    private final ItemModOptionRepository itemModOptionRepo;
    private final FluidModOptionRepository fluidModOptionRepo;
    private final NeiTextureExportRepository textureRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final ObjectMapper objectMapper;

    public RecipeService(RecipeBrowserRepository recipeRepo,
                        RecipeLookupBrowserRepository lookupRepo,
                        RecipeLookupBreakdownRepository lookupBreakdownRepo,
                        RecipeSlotBrowserRepository slotRepo,
                        IngredientEntryRepository ingredientEntryRepo,
                        RecipeSlotLayoutRepository slotLayoutRepo,
                        GregTechRecipeRepository gregTechRecipeRepo,
                        GregTechRecipeMetadataRepository metadataRepo,
                        GregTechSpecialItemRepository specialItemRepo,
                        ItemVariantRepository itemVariantRepo,
                        FluidVariantRepository fluidVariantRepo,
                        ItemModOptionRepository itemModOptionRepo,
                        FluidModOptionRepository fluidModOptionRepo,
                        NeiTextureExportRepository textureRepo,
                        AssetUrlBuilder assetUrlBuilder,
                        ObjectMapper objectMapper) {
        this.recipeRepo = recipeRepo;
        this.lookupRepo = lookupRepo;
        this.lookupBreakdownRepo = lookupBreakdownRepo;
        this.slotRepo = slotRepo;
        this.ingredientEntryRepo = ingredientEntryRepo;
        this.slotLayoutRepo = slotLayoutRepo;
        this.gregTechRecipeRepo = gregTechRecipeRepo;
        this.metadataRepo = metadataRepo;
        this.specialItemRepo = specialItemRepo;
        this.itemVariantRepo = itemVariantRepo;
        this.fluidVariantRepo = fluidVariantRepo;
        this.itemModOptionRepo = itemModOptionRepo;
        this.fluidModOptionRepo = fluidModOptionRepo;
        this.textureRepo = textureRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.objectMapper = objectMapper;
    }

    public RecipeDto detail(DatasetSummary dataset, String recipeId) {
        return loadRecipe(dataset, recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + recipeId));
    }

    public LookupTargetHeaderDto lookupTargetHeader(DatasetSummary dataset, String target) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        if (target.contains("@")) {
            ItemVariantBrowserEntity item = itemVariantRepo.findById(new ItemVariantBrowserEntity.ItemVariantId(dataset.datasetId(), target))
                    .orElseThrow(() -> new NotFoundException("Item not found: " + target));
            return new LookupTargetHeaderDto(
                    "item",
                    item.getItemVariantId(),
                    item.getDisplayName(),
                    assetUrlBuilder.build(dataset, item.getAssetPath(), item.getAssetSha256()),
                    item.getModId(),
                    modName(dataset.datasetId(), item.getModId()),
                    null);
        }
        FluidVariantBrowserEntity fluid = fluidVariantRepo.findById(new FluidVariantBrowserEntity.FluidVariantId(dataset.datasetId(), target))
                .orElseThrow(() -> new NotFoundException("Fluid not found: " + target));
        return new LookupTargetHeaderDto(
                "fluid",
                fluid.getFluidVariantId(),
                fluid.getDisplayName(),
                assetUrlBuilder.build(dataset, fluid.getAssetPath(), null),
                fluid.getModId(),
                fluidModOptionRepo.findByDatasetIdAndModId(dataset.datasetId(), fluid.getModId())
                        .map(e -> e.getName())
                        .orElse(fluid.getModId()),
                fluid.isGaseous());
    }

    public PageResponse<RecipeDto> lookup(
            DatasetSummary dataset, RecipeLookupQuery query, PageRequest page) {
        if (query.target() == null || query.target().isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        String datasetId = dataset.datasetId();
        Page<RecipeLookupBrowserEntity> result = lookupRepo.findAll(
                lookupSpec(datasetId, query),
                pageRequest(page, Sort.by("displayOrder").ascending()
                        .and(Sort.by("recipeId").ascending())));
        List<String> recipeIds = result.stream().map(RecipeLookupBrowserEntity::getRecipeId).toList();
        List<RecipeDto> recipes = loadRecipes(dataset, recipeIds);
        return new PageResponse<>(recipes, page.page(), page.size(), result.getTotalElements());
    }

    public List<HandlerBreakdownDto> lookupBreakdown(DatasetSummary dataset, RecipeLookupQuery query) {
        if (query.target() == null || query.target().isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        String datasetId = dataset.datasetId();
        String kind = query.isUsage() ? "usage" : "recipe";
        List<RecipeLookupBreakdownEntity> rows = lookupBreakdownRepo.findByDatasetIdAndTargetIdAndLookupKindOrderByDisplayOrderAscCategoryIdAsc(
                datasetId, query.target(), kind);

        Map<String, String> iconFallback = resolveBreakdownIconFallbacks(rows, datasetId);
        Map<String, List<RecipeLookupBreakdownEntity>> byHandler = new LinkedHashMap<>();
        for (RecipeLookupBreakdownEntity row : rows) {
            byHandler.computeIfAbsent(row.getHandlerId(), ignored -> new ArrayList<>()).add(row);
        }

        record HandlerOut(
                String handlerId,
                String displayName,
                String iconAssetPath,
                String iconImageResource,
                int representativeOrder,
                long total,
                List<CategoryBreakdownDto> categories) {}

        List<HandlerOut> aggregates = new ArrayList<>();
        for (Map.Entry<String, List<RecipeLookupBreakdownEntity>> e : byHandler.entrySet()) {
            List<RecipeLookupBreakdownEntity> handlerRows = e.getValue();
            handlerRows.sort(Comparator
                    .comparingInt(RecipeLookupBreakdownEntity::getDisplayOrder)
                    .thenComparing(RecipeLookupBreakdownEntity::getCategoryId));
            RecipeLookupBreakdownEntity rep = handlerRows.get(0);
            long total = 0L;
            List<CategoryBreakdownDto> categories = new ArrayList<>(handlerRows.size());
            for (RecipeLookupBreakdownEntity row : handlerRows) {
                total += row.getRecipeCount();
                categories.add(new CategoryBreakdownDto(
                        row.getCategoryId(),
                        row.getDisplayName(),
                        assetUrlBuilder.build(dataset, iconPath(row.getIconAssetPath(), row.getIconImageResource(), iconFallback), null),
                        row.getRecipeCount()));
            }
            aggregates.add(new HandlerOut(
                    e.getKey(),
                    rep.getDisplayName(),
                    rep.getIconAssetPath(),
                    rep.getIconImageResource(),
                    rep.getDisplayOrder(),
                    total,
                    categories));
        }
        aggregates.sort(Comparator
                .comparingLong(HandlerOut::total).reversed()
                .thenComparingInt(HandlerOut::representativeOrder)
                .thenComparing(HandlerOut::handlerId));
        return aggregates.stream()
                .map(h -> new HandlerBreakdownDto(
                        h.handlerId(),
                        h.displayName(),
                        assetUrlBuilder.build(dataset, iconPath(h.iconAssetPath(), h.iconImageResource(), iconFallback), null),
                        h.total(),
                        h.categories()))
                .toList();
    }

    public PageResponse<RecipeDto> listRecipesByCategory(
            DatasetSummary dataset, String categoryId, String query, String voltageTier, PageRequest page) {
        String datasetId = dataset.datasetId();
        Page<RecipeBrowserEntity> result = recipeRepo.findAll(
                recipeCategorySpec(datasetId, categoryId, query, voltageTier),
                pageRequest(page, Sort.by("displayOrder").ascending()
                        .and(Sort.by("recipeId").ascending())));
        List<String> recipeIds = result.stream().map(RecipeBrowserEntity::getRecipeId).toList();
        List<RecipeDto> recipes = loadRecipes(dataset, recipeIds);
        return new PageResponse<>(recipes, page.page(), page.size(), result.getTotalElements());
    }

    private Optional<RecipeDto> loadRecipe(DatasetSummary dataset, String recipeId) {
        List<RecipeDto> results = loadRecipes(dataset, List.of(recipeId));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private List<RecipeDto> loadRecipes(DatasetSummary dataset, List<String> recipeIds) {
        if (recipeIds.isEmpty()) return List.of();
        String datasetId = dataset.datasetId();

        Map<String, RecipeBrowserEntity> headers = new HashMap<>();
        for (RecipeBrowserEntity h : recipeRepo.findAllById(recipeIds.stream()
                .map(id -> new RecipeBrowserEntity.RecipeBrowserId(datasetId, id))
                .toList())) {
            headers.put(h.getRecipeId(), h);
        }
        if (headers.isEmpty()) return List.of();

        List<String> categoryIds = headers.values().stream()
                .map(RecipeBrowserEntity::getCategoryId)
                .distinct()
                .toList();
        Map<String, List<SlotLayoutDto>> layoutMap = loadLayouts(datasetId, categoryIds);
        List<GregTechSpecialItemEntity> specialRows = specialItemRepo.findByDatasetIdAndRecipeIdIn(
                datasetId, new ArrayList<>(headers.keySet()), Sort.by("recipeId").ascending().and(Sort.by("listIndex").ascending()));
        Map<String, List<RecipeSlotDto>> slotMap = loadSlots(dataset, new ArrayList<>(headers.keySet()), specialRows, layoutMap);
        Map<String, GregTechRecipeDto> gtMap = loadGregTechInfo(dataset, new ArrayList<>(headers.keySet()), specialRows);

        List<RecipeDto> out = new ArrayList<>(recipeIds.size());
        for (String id : recipeIds) {
            RecipeBrowserEntity h = headers.get(id);
            if (h == null) continue;
            out.add(new RecipeDto(
                    id,
                    h.getCategoryId(),
                    h.getCategoryDisplayName(),
                    h.getSourcePlugin(),
                    h.getModName(),
                    h.getSourceRef(),
                    h.getDescription(),
                    slotMap.getOrDefault(id, List.of()),
                    gtMap.get(id)));
        }
        return out;
    }

    private Map<String, List<RecipeSlotDto>> loadSlots(
            DatasetSummary dataset, List<String> recipeIds, List<GregTechSpecialItemEntity> specialRows,
            Map<String, List<SlotLayoutDto>> layoutMap) {
        String datasetId = dataset.datasetId();
        List<RecipeSlotBrowserEntity> rows = slotRepo.findByDatasetIdAndRecipeIdIn(
                datasetId, recipeIds, Sort.by("recipeId").ascending().and(Sort.by("role").ascending()).and(Sort.by("slotIndex").ascending()));
        Map<String, String> modNames = loadModNames(datasetId);
        Map<String, List<RecipeSlotCandidateDto>> candidatesByGroup = loadCandidates(dataset, rows, modNames);
        Map<String, ItemVariantBrowserEntity> directItemRefs = loadItemRefs(datasetId, rows.stream()
                .map(RecipeSlotBrowserEntity::getItemVariantId)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList());
        Map<String, FluidVariantBrowserEntity> directFluidRefs = loadFluidRefs(datasetId, rows.stream()
                .map(RecipeSlotBrowserEntity::getFluidVariantId)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList());
        Map<String, String> placementByCategoryRoleSlot = new HashMap<>();
        for (Map.Entry<String, List<SlotLayoutDto>> e : layoutMap.entrySet()) {
            for (SlotLayoutDto layout : e.getValue()) {
                if (layout.placement() != null) {
                    placementByCategoryRoleSlot.put(e.getKey() + ":" + layout.role() + ":" + layout.slotIndex(), layout.placement());
                }
            }
        }
        Map<String, String> categoryByRecipe = new HashMap<>();
        for (RecipeSlotBrowserEntity r : rows) {
            categoryByRecipe.putIfAbsent(r.getRecipeId(), r.getCategoryId());
        }

        Map<String, List<RecipeSlotDto>> byRecipe = new LinkedHashMap<>();
        for (RecipeSlotBrowserEntity r : rows) {
            String item = nullIfEmpty(r.getItemVariantId());
            String fluid = nullIfEmpty(r.getFluidVariantId());
            String displayName = item != null ? r.getItemDisplayName() : r.getFluidDisplayName();
            String modId = item != null ? r.getItemModId() : r.getFluidModId();
            FluidVariantBrowserEntity fluidRef = fluid == null ? null : directFluidRefs.get(fluid);
            String tooltipText = item != null && directItemRefs.get(item) != null ? directItemRefs.get(item).getTooltipText() : null;
            String assetPath = item != null ? r.getItemAssetPath() : r.getFluidAssetPath();
            byRecipe.computeIfAbsent(r.getRecipeId(), ignored -> new ArrayList<>())
                    .add(new RecipeSlotDto(
                            r.getRole(),
                            r.getSlotIndex(),
                            item,
                            fluid,
                            r.getAmount(),
                            r.getProbability(),
                            nullIfEmpty(r.getGroupId()),
                            displayName,
                            modId,
                            modName(modNames, modId),
                            fluidRef == null ? null : fluidRef.isGaseous(),
                            fluidRef == null ? null : fluidRef.getTemperature(),
                            tooltipText,
                            assetUrlBuilder.build(dataset, assetPath, null),
                            nullIfEmpty(r.getGroupId()) == null ? List.of() : candidatesByGroup.getOrDefault(r.getGroupId(), List.of()),
                            placementByCategoryRoleSlot.get(r.getCategoryId() + ":" + r.getRole() + ":" + r.getSlotIndex())));
        }
        Map<String, ItemVariantBrowserEntity> itemRefs = loadItemRefs(datasetId, specialRows.stream()
                .map(GregTechSpecialItemEntity::getItemVariantId)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList());
        for (GregTechSpecialItemEntity si : specialRows) {
            ItemVariantBrowserEntity item = itemRefs.get(si.getItemVariantId());
            String categoryId = categoryByRecipe.get(si.getRecipeId());
            String placement = categoryId == null
                    ? null
                    : placementByCategoryRoleSlot.get(categoryId + ":special_item:" + si.getListIndex());
            byRecipe.computeIfAbsent(si.getRecipeId(), ignored -> new ArrayList<>())
                    .add(new RecipeSlotDto(
                            "special_item",
                            si.getListIndex(),
                            si.getItemVariantId(),
                            null,
                            1,
                            1.0,
                            null,
                            item == null ? null : item.getDisplayName(),
                            item == null ? null : item.getModId(),
                            item == null ? null : modName(modNames, item.getModId()),
                            null,
                            null,
                            item == null ? null : item.getTooltipText(),
                            assetUrlBuilder.build(dataset, item == null ? null : item.getAssetPath(), item == null ? null : item.getAssetSha256()),
                            List.of(),
                            placement));
        }
        byRecipe.values().forEach(list -> list.sort(Comparator
                .comparing(RecipeSlotDto::role)
                .thenComparingInt(RecipeSlotDto::slotIndex)));
        return byRecipe;
    }

    private Map<String, List<RecipeSlotCandidateDto>> loadCandidates(
            DatasetSummary dataset, List<RecipeSlotBrowserEntity> slots, Map<String, String> modNames) {
        String datasetId = dataset.datasetId();
        List<String> groupIds = slots.stream()
                .filter(s -> nullIfEmpty(s.getItemVariantId()) == null && nullIfEmpty(s.getFluidVariantId()) == null)
                .map(RecipeSlotBrowserEntity::getGroupId)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
        if (groupIds.isEmpty()) return Map.of();
        List<IngredientEntryEntity> entries = ingredientEntryRepo.findByDatasetIdAndGroupIdIn(
                datasetId, groupIds, Sort.by("groupId").ascending().and(Sort.by("itemVariantId").ascending()).and(Sort.by("fluidVariantId").ascending()));
        Map<String, ItemVariantBrowserEntity> items = loadItemRefs(datasetId, entries.stream()
                .map(IngredientEntryEntity::getItemVariantId)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList());
        Map<String, FluidVariantBrowserEntity> fluids = loadFluidRefs(datasetId, entries.stream()
                .map(IngredientEntryEntity::getFluidVariantId)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList());
        Map<String, List<RecipeSlotCandidateDto>> out = new HashMap<>();
        for (IngredientEntryEntity e : entries) {
            String itemId = nullIfEmpty(e.getItemVariantId());
            String fluidId = nullIfEmpty(e.getFluidVariantId());
            if (itemId == null && fluidId == null) continue;
            ItemVariantBrowserEntity item = itemId == null ? null : items.get(itemId);
            FluidVariantBrowserEntity fluid = fluidId == null ? null : fluids.get(fluidId);
            String modId = item == null ? (fluid == null ? null : fluid.getModId()) : item.getModId();
            out.computeIfAbsent(e.getGroupId(), ignored -> new ArrayList<>())
                    .add(new RecipeSlotCandidateDto(
                            itemId,
                            fluidId,
                            e.getAmount(),
                            item == null ? (fluid == null ? null : fluid.getDisplayName()) : item.getDisplayName(),
                            modId,
                            modName(modNames, modId),
                            fluid == null ? null : fluid.isGaseous(),
                            fluid == null ? null : fluid.getTemperature(),
                            item == null ? null : item.getTooltipText(),
                            assetUrlBuilder.build(dataset,
                                    item == null ? (fluid == null ? null : fluid.getAssetPath()) : item.getAssetPath(),
                                    item == null ? null : item.getAssetSha256())));
        }
        return out;
    }

    private Map<String, String> loadModNames(String datasetId) {
        Map<String, String> names = new HashMap<>();
        itemModOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .forEach(e -> names.put(e.getModId(), e.getName()));
        fluidModOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .forEach(e -> names.putIfAbsent(e.getModId(), e.getName()));
        return names;
    }

    private static String modName(Map<String, String> modNames, String modId) {
        if (modId == null || modId.isBlank()) return null;
        return modNames.getOrDefault(modId, modId);
    }

    private Map<String, List<SlotLayoutDto>> loadLayouts(String datasetId, List<String> categoryIds) {
        if (categoryIds.isEmpty()) return Map.of();
        Map<String, List<SlotLayoutDto>> map = new LinkedHashMap<>();
        for (RecipeSlotLayoutEntity e : slotLayoutRepo.findByDatasetIdAndCategoryIdIn(
                datasetId, categoryIds, Sort.by("categoryId").ascending().and(Sort.by("role").ascending()).and(Sort.by("slotIndex").ascending()))) {
            map.computeIfAbsent(e.getCategoryId(), ignored -> new ArrayList<>())
                    .add(new SlotLayoutDto(e.getRole(), e.getSlotIndex(), e.getX(), e.getY(), e.getWidth(), e.getHeight(), e.getSlotStyle(), e.getPlacement()));
        }
        return map;
    }

    private Map<String, GregTechRecipeDto> loadGregTechInfo(
            DatasetSummary dataset, List<String> recipeIds, List<GregTechSpecialItemEntity> specialRows) {
        Map<String, List<GregTechSpecialItemDto>> specialItems = mapGregTechSpecialItems(dataset, specialRows);
        Map<String, Map<String, MetadataValueDto>> metadata = loadGregTechMetadata(dataset.datasetId(), recipeIds);
        Map<String, GregTechRecipeDto> out = new HashMap<>();
        for (GregTechRecipeEntity e : gregTechRecipeRepo.findByDatasetIdAndRecipeIdIn(dataset.datasetId(), recipeIds)) {
            out.put(e.getRecipeId(), new GregTechRecipeDto(
                    e.getRecipeKind(),
                    e.isVisibleInNei(),
                    e.getVoltageTier(),
                    e.getVoltage(),
                    e.getAmperage(),
                    e.getDurationTicks(),
                    e.getSpecialValue(),
                    specialItems.getOrDefault(e.getRecipeId(), List.of()),
                    metadata.getOrDefault(e.getRecipeId(), Map.of())));
        }
        return out;
    }

    private Map<String, List<GregTechSpecialItemDto>> mapGregTechSpecialItems(
            DatasetSummary dataset, List<GregTechSpecialItemEntity> specialRows) {
        Map<String, ItemVariantBrowserEntity> itemRefs = loadItemRefs(dataset.datasetId(), specialRows.stream()
                .map(GregTechSpecialItemEntity::getItemVariantId)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList());
        Map<String, String> modNames = loadModNames(dataset.datasetId());
        Map<String, List<GregTechSpecialItemDto>> out = new LinkedHashMap<>();
        for (GregTechSpecialItemEntity e : specialRows) {
            ItemVariantBrowserEntity item = itemRefs.get(e.getItemVariantId());
            out.computeIfAbsent(e.getRecipeId(), ignored -> new ArrayList<>())
                    .add(new GregTechSpecialItemDto(
                            e.getItemVariantId(),
                            item == null ? null : item.getDisplayName(),
                            item == null ? null : item.getModId(),
                            item == null ? null : modName(modNames, item.getModId()),
                            assetUrlBuilder.build(dataset, item == null ? null : item.getAssetPath(), item == null ? null : item.getAssetSha256())));
        }
        return out;
    }

    private Map<String, Map<String, MetadataValueDto>> loadGregTechMetadata(String datasetId, List<String> recipeIds) {
        Map<String, Map<String, MetadataValueDto>> out = new LinkedHashMap<>();
        for (GregTechRecipeMetadataEntity e : metadataRepo.findByDatasetIdAndRecipeIdIn(
                datasetId, recipeIds, Sort.by("recipeId").ascending().and(Sort.by("metadataKey").ascending()))) {
            out.computeIfAbsent(e.getRecipeId(), ignored -> new LinkedHashMap<>())
                    .put(e.getMetadataKey(), new MetadataValueDto(e.getValueType(), e.getValueText(), parseMetadataJson(e.getValueJson(), e.getRecipeId(), e.getMetadataKey())));
        }
        return out;
    }

    private JsonNode parseMetadataJson(String raw, String recipeId, String key) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return objectMapper.readTree(raw);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to parse gregtech_recipe_metadata.value_json for recipe=" + recipeId + ", key=" + key, ex);
        }
    }

    private String modName(String datasetId, String modId) {
        if (modId == null || modId.isBlank()) return null;
        return itemModOptionRepo.findByDatasetIdAndModId(datasetId, modId)
                .map(e -> e.getName())
                .orElse(modId);
    }

    private Map<String, ItemVariantBrowserEntity> loadItemRefs(String datasetId, List<String> itemVariantIds) {
        if (itemVariantIds.isEmpty()) return Map.of();
        Map<String, ItemVariantBrowserEntity> map = new HashMap<>();
        for (ItemVariantBrowserEntity e : itemVariantRepo.findByDatasetIdAndItemVariantIdIn(datasetId, itemVariantIds)) {
            map.put(e.getItemVariantId(), e);
        }
        return map;
    }

    private Map<String, FluidVariantBrowserEntity> loadFluidRefs(String datasetId, List<String> fluidVariantIds) {
        if (fluidVariantIds.isEmpty()) return Map.of();
        Map<String, FluidVariantBrowserEntity> map = new HashMap<>();
        for (FluidVariantBrowserEntity e : fluidVariantRepo.findByDatasetIdAndFluidVariantIdIn(datasetId, fluidVariantIds)) {
            map.put(e.getFluidVariantId(), e);
        }
        return map;
    }

    private static String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    private Map<String, String> resolveBreakdownIconFallbacks(List<RecipeLookupBreakdownEntity> rows, String datasetId) {
        List<String> resources = rows.stream()
                .filter(e -> e.getIconAssetPath() == null)
                .map(RecipeLookupBreakdownEntity::getIconImageResource)
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

    private static Pageable pageRequest(PageRequest page, Sort sort) {
        PageRequest checkedPage = Objects.requireNonNull(page, "page");
        return org.springframework.data.domain.PageRequest.of(checkedPage.page(), checkedPage.size(), sort);
    }

    private static Specification<RecipeBrowserEntity> recipeCategorySpec(
            String datasetId, String categoryId, String q, String voltageTier) {
        Specification<RecipeBrowserEntity> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("datasetId"), datasetId),
                cb.equal(root.get("categoryId"), categoryId));
        if (q != null && !q.isBlank()) {
            spec = spec.and(recipeTextSearch(q));
        }
        if (voltageTier != null && !voltageTier.isBlank()) {
            spec = spec.and(recipeVoltageTier(datasetId, voltageTier));
        }
        return spec;
    }

    private static Specification<RecipeBrowserEntity> recipeTextSearch(String q) {
        String pattern = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            Subquery<Integer> documentMatch = query.subquery(Integer.class);
            var document = documentMatch.from(RecipeSearchDocumentEntity.class);
            documentMatch.select(cb.literal(1));
            documentMatch.where(
                    cb.equal(document.get("datasetId"), root.get("datasetId")),
                    cb.equal(document.get("recipeId"), root.get("recipeId")),
                    cb.equal(document.get("categoryId"), root.get("categoryId")),
                    cb.like(document.get("searchText"), pattern));
            return cb.exists(documentMatch);
        };
    }

    private static Specification<RecipeBrowserEntity> recipeVoltageTier(String datasetId, String voltageTier) {
        return (root, query, cb) -> {
            Subquery<String> subquery = query.subquery(String.class);
            var gt = subquery.from(GregTechRecipeEntity.class);
            subquery.select(gt.get("recipeId"));
            subquery.where(
                    cb.equal(gt.get("datasetId"), datasetId),
                    cb.equal(gt.get("recipeId"), root.get("recipeId")),
                    cb.equal(gt.get("voltageTier"), voltageTier));
            return cb.exists(subquery);
        };
    }

    private static Specification<RecipeLookupBrowserEntity> lookupSpec(String datasetId, RecipeLookupQuery query) {
        String kind = query.isUsage() ? "usage" : "recipe";
        Specification<RecipeLookupBrowserEntity> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("datasetId"), datasetId),
                cb.equal(root.get("targetId"), query.target()),
                cb.equal(root.get("lookupKind"), kind));
        if (query.hasCategoryFilter()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("categoryId"), query.categoryId()));
        }
        if (query.hasHandlerFilter()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("handlerId"), query.handlerId()));
        }
        if (query.hasVoltageTierFilter()) {
            spec = spec.and(lookupVoltageTier(datasetId, query.voltageTier()));
        }
        return spec;
    }

    private static Specification<RecipeLookupBrowserEntity> lookupVoltageTier(String datasetId, String voltageTier) {
        return (root, query, cb) -> {
            Subquery<String> subquery = query.subquery(String.class);
            var gt = subquery.from(GregTechRecipeEntity.class);
            subquery.select(gt.get("recipeId"));
            subquery.where(
                    cb.equal(gt.get("datasetId"), datasetId),
                    cb.equal(gt.get("recipeId"), root.get("recipeId")),
                    cb.equal(gt.get("voltageTier"), voltageTier));
            return cb.exists(subquery);
        };
    }
}
