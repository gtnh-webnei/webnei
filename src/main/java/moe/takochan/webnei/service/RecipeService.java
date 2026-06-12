package moe.takochan.webnei.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.model.dto.CategoryBreakdownDto;
import moe.takochan.webnei.model.dto.DatasetSummary;
import moe.takochan.webnei.model.dto.FluidRef;
import moe.takochan.webnei.model.dto.HandlerBreakdownDto;
import moe.takochan.webnei.model.dto.ItemRef;
import moe.takochan.webnei.model.dto.LookupKindCountDto;
import moe.takochan.webnei.model.dto.LookupTargetHeaderDto;
import moe.takochan.webnei.model.dto.MetadataValueDto;
import moe.takochan.webnei.model.dto.PageRequest;
import moe.takochan.webnei.model.dto.RecipeDto;
import moe.takochan.webnei.model.dto.RecipeSlotCandidateDto;
import moe.takochan.webnei.model.dto.RecipeSlotDto;
import moe.takochan.webnei.model.dto.SlotLayoutDto;
import moe.takochan.webnei.model.entity.table.IngredientEntryEntity;
import moe.takochan.webnei.model.entity.table.RecipeFilterTagEntity;
import moe.takochan.webnei.model.entity.table.RecipeMetadataEntity;
import moe.takochan.webnei.model.entity.table.RecipeSearchDocumentEntity;
import moe.takochan.webnei.model.entity.table.RecipeSlotLayoutEntity;
import moe.takochan.webnei.model.entity.table.RecipeSlotMetadataEntity;
import moe.takochan.webnei.model.entity.view.RecipeBrowserEntity;
import moe.takochan.webnei.model.entity.view.RecipeLookupBreakdownEntity;
import moe.takochan.webnei.model.entity.view.RecipeLookupBrowserEntity;
import moe.takochan.webnei.model.entity.view.RecipeSlotBrowserEntity;
import moe.takochan.webnei.model.query.RecipeLookupQuery;
import moe.takochan.webnei.repository.table.IngredientEntryRepository;
import moe.takochan.webnei.repository.table.RecipeLookupCountRepository;
import moe.takochan.webnei.repository.table.RecipeMetadataRepository;
import moe.takochan.webnei.repository.table.RecipeSlotLayoutRepository;
import moe.takochan.webnei.repository.table.RecipeSlotMetadataRepository;
import moe.takochan.webnei.repository.view.RecipeBrowserRepository;
import moe.takochan.webnei.repository.view.RecipeLookupBreakdownRepository;
import moe.takochan.webnei.repository.view.RecipeLookupBrowserRepository;
import moe.takochan.webnei.repository.view.RecipeSlotBrowserRepository;
import moe.takochan.webnei.service.EntityRefService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RecipeService {

    private final RecipeBrowserRepository recipeRepo;
    private final RecipeLookupBrowserRepository lookupRepo;
    private final RecipeLookupCountRepository lookupCountRepo;
    private final RecipeLookupBreakdownRepository lookupBreakdownRepo;
    private final RecipeSlotBrowserRepository slotRepo;
    private final IngredientEntryRepository ingredientEntryRepo;
    private final RecipeSlotLayoutRepository slotLayoutRepo;
    private final RecipeMetadataRepository metadataRepo;
    private final RecipeSlotMetadataRepository slotMetadataRepo;
    private final EntityRefService entityRefService;
    private final AssetUrlBuilder assetUrlBuilder;
    private final ObjectMapper objectMapper;

    public RecipeService(RecipeBrowserRepository recipeRepo,
                        RecipeLookupBrowserRepository lookupRepo,
                        RecipeLookupCountRepository lookupCountRepo,
                        RecipeLookupBreakdownRepository lookupBreakdownRepo,
                        RecipeSlotBrowserRepository slotRepo,
                        IngredientEntryRepository ingredientEntryRepo,
                        RecipeSlotLayoutRepository slotLayoutRepo,
                        RecipeMetadataRepository metadataRepo,
                        RecipeSlotMetadataRepository slotMetadataRepo,
                        EntityRefService entityRefService,
                        AssetUrlBuilder assetUrlBuilder,
                        ObjectMapper objectMapper) {
        this.recipeRepo = recipeRepo;
        this.lookupRepo = lookupRepo;
        this.lookupCountRepo = lookupCountRepo;
        this.lookupBreakdownRepo = lookupBreakdownRepo;
        this.slotRepo = slotRepo;
        this.ingredientEntryRepo = ingredientEntryRepo;
        this.slotLayoutRepo = slotLayoutRepo;
        this.metadataRepo = metadataRepo;
        this.slotMetadataRepo = slotMetadataRepo;
        this.entityRefService = entityRefService;
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
        Map<String, Long> counts = lookupCounts(dataset.datasetId(), target);
        long recipeCount = counts.getOrDefault("recipe", 0L);
        long usageCount = counts.getOrDefault("usage", 0L);
        if (target.contains("@")) {
            ItemRef item = entityRefService.itemRefs(dataset, List.of(target)).get(target);
            if (item == null) throw new NotFoundException("Item not found: " + target);
            return new LookupTargetHeaderDto(
                    "item",
                    item.itemVariantId(),
                    item.displayName(),
                    item.assetUrl(),
                    item.modId(),
                    item.modName(),
                    null,
                    recipeCount,
                    usageCount);
        }
        FluidRef fluid = entityRefService.fluidRefs(dataset, List.of(target)).get(target);
        if (fluid == null) throw new NotFoundException("Fluid not found: " + target);
        return new LookupTargetHeaderDto(
                "fluid",
                fluid.fluidVariantId(),
                fluid.displayName(),
                fluid.assetUrl(),
                fluid.modId(),
                fluid.modName(),
                fluid.gaseous(),
                recipeCount,
                usageCount);
    }

    private Map<String, Long> lookupCounts(String datasetId, String targetId) {
        return lookupCountRepo.countByDatasetIdAndTargetId(datasetId, targetId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        LookupKindCountDto::lookupKind,
                        LookupKindCountDto::recipeCount));
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

        Map<String, List<RecipeLookupBreakdownEntity>> byHandler = new LinkedHashMap<>();
        for (RecipeLookupBreakdownEntity row : rows) {
            byHandler.computeIfAbsent(row.getHandlerId(), ignored -> new ArrayList<>()).add(row);
        }

        record HandlerOut(
                String handlerId,
                String displayName,
                String iconAssetPath,
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
                        assetUrlBuilder.build(dataset, row.getIconAssetPath(), null),
                        row.getRecipeCount()));
            }
            aggregates.add(new HandlerOut(
                    e.getKey(),
                    rep.getDisplayName(),
                    rep.getIconAssetPath(),
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
                        assetUrlBuilder.build(dataset, h.iconAssetPath(), null),
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
        Map<String, List<RecipeSlotDto>> slotMap = loadSlots(dataset, new ArrayList<>(headers.keySet()), layoutMap);
        Map<String, Map<String, MetadataValueDto>> metadataMap =
                loadMetadata(datasetId, new ArrayList<>(headers.keySet()));

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
                    metadataMap.getOrDefault(id, Map.of())));
        }
        return out;
    }

    private Map<String, List<RecipeSlotDto>> loadSlots(
            DatasetSummary dataset, List<String> recipeIds,
            Map<String, List<SlotLayoutDto>> layoutMap) {
        String datasetId = dataset.datasetId();
        List<RecipeSlotBrowserEntity> rows = slotRepo.findByDatasetIdAndRecipeIdIn(
                datasetId, recipeIds, Sort.by("recipeId").ascending().and(Sort.by("role").ascending()).and(Sort.by("slotIndex").ascending()));
        Map<String, List<RecipeSlotCandidateDto>> candidatesByGroup = loadCandidates(dataset, rows);
        Map<String, ItemRef> directItemRefs = entityRefService.itemRefs(dataset, rows.stream()
                .map(RecipeSlotBrowserEntity::getItemVariantId)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList());
        Map<String, FluidRef> directFluidRefs = entityRefService.fluidRefs(dataset, rows.stream()
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
        Map<String, Map<String, MetadataValueDto>> slotMetadata = loadSlotMetadata(datasetId, recipeIds);

        Map<String, List<RecipeSlotDto>> byRecipe = new LinkedHashMap<>();
        for (RecipeSlotBrowserEntity r : rows) {
            String item = nullIfEmpty(r.getItemVariantId());
            String fluid = nullIfEmpty(r.getFluidVariantId());
            ItemRef itemRef = item == null ? null : directItemRefs.get(item);
            FluidRef fluidRef = fluid == null ? null : directFluidRefs.get(fluid);
            String displayName = itemRef != null ? itemRef.displayName() : (fluidRef == null ? null : fluidRef.displayName());
            String modId = itemRef != null ? itemRef.modId() : (fluidRef == null ? null : fluidRef.modId());
            String modName = itemRef != null ? itemRef.modName() : (fluidRef == null ? null : fluidRef.modName());
            String tooltipText = itemRef == null ? null : itemRef.tooltipText();
            String assetUrl = itemRef != null ? itemRef.assetUrl() : (fluidRef == null ? null : fluidRef.assetUrl());
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
                            modName,
                            fluidRef == null ? null : fluidRef.gaseous(),
                            fluidRef == null ? null : fluidRef.temperature(),
                            tooltipText,
                            assetUrl,
                            nullIfEmpty(r.getGroupId()) == null ? List.of() : candidatesByGroup.getOrDefault(r.getGroupId(), List.of()),
                            placementForSlot(placementByCategoryRoleSlot, r),
                            nullIfEmpty(r.getSlotGroupKey()),
                            r.getSlotGroupOrder(),
                            slotMetadata.getOrDefault(slotMetadataKey(r.getRecipeId(), r.getRole(), r.getSlotIndex()), Map.of())));
        }
        byRecipe.values().forEach(list -> list.sort(Comparator
                .comparing(RecipeSlotDto::role)
                .thenComparingInt(RecipeSlotDto::slotIndex)));
        return byRecipe;
    }

    private String placementForSlot(
            Map<String, String> placementByCategoryRoleSlot,
            RecipeSlotBrowserEntity row) {
        String exact = placementByCategoryRoleSlot.get(
                row.getCategoryId() + ":" + row.getRole() + ":" + row.getSlotIndex());
        if (exact != null) {
            return exact;
        }
        if ("special_item".equals(row.getRole())) {
            return placementByCategoryRoleSlot.get(row.getCategoryId() + ":special_item:0");
        }
        return null;
    }

    private Map<String, List<RecipeSlotCandidateDto>> loadCandidates(
            DatasetSummary dataset, List<RecipeSlotBrowserEntity> slots) {
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
        Map<String, ItemRef> items = entityRefService.itemRefs(dataset, entries.stream()
                .map(IngredientEntryEntity::getItemVariantId)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList());
        Map<String, FluidRef> fluids = entityRefService.fluidRefs(dataset, entries.stream()
                .map(IngredientEntryEntity::getFluidVariantId)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList());
        Map<String, List<RecipeSlotCandidateDto>> out = new HashMap<>();
        for (IngredientEntryEntity e : entries) {
            String itemId = nullIfEmpty(e.getItemVariantId());
            String fluidId = nullIfEmpty(e.getFluidVariantId());
            if (itemId == null && fluidId == null) continue;
            ItemRef item = itemId == null ? null : items.get(itemId);
            FluidRef fluid = fluidId == null ? null : fluids.get(fluidId);
            String modId = item == null ? (fluid == null ? null : fluid.modId()) : item.modId();
            String modName = item == null ? (fluid == null ? null : fluid.modName()) : item.modName();
            out.computeIfAbsent(e.getGroupId(), ignored -> new ArrayList<>())
                    .add(new RecipeSlotCandidateDto(
                            itemId,
                            fluidId,
                            e.getAmount(),
                            item == null ? (fluid == null ? null : fluid.displayName()) : item.displayName(),
                            modId,
                            modName,
                            fluid == null ? null : fluid.gaseous(),
                            fluid == null ? null : fluid.temperature(),
                            item == null ? null : item.tooltipText(),
                            item == null ? (fluid == null ? null : fluid.assetUrl()) : item.assetUrl()));
        }
        return out;
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

    private Map<String, Map<String, MetadataValueDto>> loadSlotMetadata(String datasetId, List<String> recipeIds) {
        if (recipeIds.isEmpty()) return Map.of();
        Map<String, Map<String, MetadataValueDto>> out = new LinkedHashMap<>();
        for (RecipeSlotMetadataEntity e : slotMetadataRepo.findByDatasetIdAndRecipeIdIn(
                datasetId,
                recipeIds,
                Sort.by("recipeId").ascending()
                        .and(Sort.by("role").ascending())
                        .and(Sort.by("slotIndex").ascending())
                        .and(Sort.by("metadataKey").ascending()))) {
            out.computeIfAbsent(
                            slotMetadataKey(e.getRecipeId(), e.getRole(), e.getSlotIndex()),
                            ignored -> new LinkedHashMap<>())
                    .put(e.getMetadataKey(), new MetadataValueDto(
                            e.getValueType(),
                            e.getValueText(),
                            parseMetadataJson(e.getValueJson(), e.getRecipeId(), e.getMetadataKey())));
        }
        return out;
    }

    private static String slotMetadataKey(String recipeId, String role, int slotIndex) {
        return recipeId + '\n' + role + '\n' + slotIndex;
    }

    private Map<String, Map<String, MetadataValueDto>> loadMetadata(String datasetId, List<String> recipeIds) {
        Map<String, Map<String, MetadataValueDto>> out = new LinkedHashMap<>();
        for (RecipeMetadataEntity e : metadataRepo.findByDatasetIdAndRecipeIdIn(
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

    private static String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
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
            var ft = subquery.from(RecipeFilterTagEntity.class);
            subquery.select(ft.get("recipeId"));
            subquery.where(
                    cb.equal(ft.get("datasetId"), datasetId),
                    cb.equal(ft.get("recipeId"), root.get("recipeId")),
                    cb.equal(ft.get("tagKey"), "voltage_tier"),
                    cb.equal(ft.get("tagValue"), voltageTier));
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
            var ft = subquery.from(RecipeFilterTagEntity.class);
            subquery.select(ft.get("recipeId"));
            subquery.where(
                    cb.equal(ft.get("datasetId"), datasetId),
                    cb.equal(ft.get("recipeId"), root.get("recipeId")),
                    cb.equal(ft.get("tagKey"), "voltage_tier"),
                    cb.equal(ft.get("tagValue"), voltageTier));
            return cb.exists(subquery);
        };
    }
}
