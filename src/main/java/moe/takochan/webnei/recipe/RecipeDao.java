package moe.takochan.webnei.recipe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.recipe.dto.CategoryBreakdownDto;
import moe.takochan.webnei.recipe.dto.GregTechRecipeDto;
import moe.takochan.webnei.recipe.dto.GregTechSpecialItemDto;
import moe.takochan.webnei.recipe.dto.HandlerBreakdownDto;
import moe.takochan.webnei.recipe.dto.RecipeCategoryDto;
import moe.takochan.webnei.recipe.dto.RecipeDto;
import moe.takochan.webnei.recipe.dto.RecipeSlotCandidateDto;
import moe.takochan.webnei.recipe.dto.RecipeSlotDto;
import moe.takochan.webnei.recipe.dto.SlotLayoutDto;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RecipeDao {

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public RecipeDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<RecipeCategoryDto> listCategories(DatasetSummary dataset) {
        return queryCategories(dataset, null, null, false, null);
    }

    public PageResponse<RecipeCategoryDto> listCategoriesPage(
            DatasetSummary dataset, String query, String plugin, boolean hideEmpty, PageRequest page) {
        String filter = (query == null || query.isBlank()) ? null : "%" + query.toLowerCase() + "%";
        String pluginParam = (plugin == null || plugin.isBlank()) ? null : plugin;

        long total = jdbc.sql("""
                        SELECT COUNT(*) FROM v_recipe_category_browser
                        WHERE dataset_id = :datasetId
                          AND (CAST(:plugin AS TEXT) IS NULL OR plugin = CAST(:plugin AS TEXT))
                          AND (CAST(:hideEmpty AS BOOLEAN) IS NULL
                               OR CAST(:hideEmpty AS BOOLEAN) = false
                               OR recipe_count > 0)
                          AND (CAST(:filter AS TEXT) IS NULL
                               OR lower(display_name) LIKE CAST(:filter AS TEXT)
                               OR lower(category_id) LIKE CAST(:filter AS TEXT)
                               OR lower(handler_id) LIKE CAST(:filter AS TEXT))
                        """)
                .param("datasetId", dataset.datasetId())
                .param("plugin", pluginParam)
                .param("hideEmpty", hideEmpty)
                .param("filter", filter)
                .query(Long.class)
                .single();

        List<RecipeCategoryDto> items = queryCategories(dataset, filter, pluginParam, hideEmpty, page);
        return PageResponse.of(items, page, total);
    }

    public List<String> listCategoryPlugins(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT DISTINCT plugin FROM v_recipe_category_browser
                        WHERE dataset_id = :datasetId AND plugin <> ''
                        ORDER BY plugin
                        """)
                .param("datasetId", dataset.datasetId())
                .query(String.class)
                .list();
    }

    private List<RecipeCategoryDto> queryCategories(
            DatasetSummary dataset, String filter, String plugin, boolean hideEmpty, PageRequest page) {
        StringBuilder sql = new StringBuilder("""
                SELECT category_id, plugin, handler_id, display_name, ui_kind, ui_template_id,
                       shapeless, icon_item_variant_id, icon_display_name, icon_asset_path,
                       icon_info, item_input_width, item_input_height, fluid_input_width, fluid_input_height,
                       item_output_width, item_output_height, fluid_output_width, fluid_output_height,
                       supports_recipe_lookup, supports_usage_lookup, display_order,
                       canvas_width, canvas_height, background_asset_path,
                       recipe_count, machine_count
                FROM v_recipe_category_browser
                WHERE dataset_id = :datasetId
                  AND (CAST(:plugin AS TEXT) IS NULL OR plugin = CAST(:plugin AS TEXT))
                  AND (CAST(:hideEmpty AS BOOLEAN) IS NULL
                       OR CAST(:hideEmpty AS BOOLEAN) = false
                       OR recipe_count > 0)
                  AND (CAST(:filter AS TEXT) IS NULL
                       OR lower(display_name) LIKE CAST(:filter AS TEXT)
                       OR lower(category_id) LIKE CAST(:filter AS TEXT)
                       OR lower(handler_id) LIKE CAST(:filter AS TEXT))
                ORDER BY display_order, category_id
                """);
        if (page != null) {
            sql.append("OFFSET :offset LIMIT :limit\n");
        }
        var spec = jdbc.sql(sql.toString())
                .param("datasetId", dataset.datasetId())
                .param("plugin", plugin)
                .param("hideEmpty", hideEmpty)
                .param("filter", filter);
        if (page != null) {
            spec = spec.param("offset", page.offset()).param("limit", page.size());
        }
        return spec
                .query((rs, n) -> new RecipeCategoryDto(
                        rs.getString("category_id"),
                        rs.getString("plugin"),
                        rs.getString("handler_id"),
                        rs.getString("display_name"),
                        rs.getString("ui_kind"),
                        rs.getString("ui_template_id"),
                        rs.getBoolean("shapeless"),
                        rs.getString("icon_item_variant_id"),
                        rs.getString("icon_display_name"),
                        assetUrlBuilder.build(dataset, rs.getString("icon_asset_path"), null),
                        rs.getString("icon_info"),
                        rs.getInt("item_input_width"),
                        rs.getInt("item_input_height"),
                        rs.getInt("fluid_input_width"),
                        rs.getInt("fluid_input_height"),
                        rs.getInt("item_output_width"),
                        rs.getInt("item_output_height"),
                        rs.getInt("fluid_output_width"),
                        rs.getInt("fluid_output_height"),
                        rs.getBoolean("supports_recipe_lookup"),
                        rs.getBoolean("supports_usage_lookup"),
                        rs.getInt("display_order"),
                        nullableInt(rs, "canvas_width"),
                        nullableInt(rs, "canvas_height"),
                        assetUrlBuilder.build(dataset, rs.getString("background_asset_path"), null),
                        rs.getLong("recipe_count"),
                        rs.getLong("machine_count")))
                .list();
    }

    public LookupPage listRecipeIdsByCategory(
            DatasetSummary dataset, String categoryId, String query, PageRequest page) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) {
            long total = jdbc.sql("""
                            SELECT COUNT(*) FROM recipe
                            WHERE dataset_id = :datasetId AND category_id = :categoryId
                            """)
                    .param("datasetId", dataset.datasetId())
                    .param("categoryId", categoryId)
                    .query(Long.class)
                    .single();

            List<String> ids = jdbc.sql("""
                            SELECT recipe_id FROM recipe
                            WHERE dataset_id = :datasetId AND category_id = :categoryId
                            ORDER BY recipe_id
                            OFFSET :offset LIMIT :limit
                            """)
                    .param("datasetId", dataset.datasetId())
                    .param("categoryId", categoryId)
                    .param("offset", page.offset())
                    .param("limit", page.size())
                    .query(String.class)
                    .list();
            return new LookupPage(ids, total);
        }

        String pattern = "%" + q.toLowerCase() + "%";
        String matchSql = """
                SELECT DISTINCT recipe_id FROM (
                  SELECT recipe_id FROM v_recipe_item_slot
                  WHERE dataset_id = :datasetId
                    AND category_id = :categoryId
                    AND (LOWER(COALESCE(display_name, '')) LIKE :pattern
                         OR LOWER(item_variant_id) LIKE :pattern)
                  UNION ALL
                  SELECT recipe_id FROM v_recipe_fluid_slot
                  WHERE dataset_id = :datasetId
                    AND category_id = :categoryId
                    AND (LOWER(COALESCE(display_name, '')) LIKE :pattern
                         OR LOWER(fluid_variant_id) LIKE :pattern)
                ) m
                """;

        long total = jdbc.sql("SELECT COUNT(*) FROM (" + matchSql + ") c")
                .param("datasetId", dataset.datasetId())
                .param("categoryId", categoryId)
                .param("pattern", pattern)
                .query(Long.class)
                .single();
        List<String> ids = jdbc.sql(matchSql + " ORDER BY recipe_id OFFSET :offset LIMIT :limit")
                .param("datasetId", dataset.datasetId())
                .param("categoryId", categoryId)
                .param("pattern", pattern)
                .param("offset", page.offset())
                .param("limit", page.size())
                .query(String.class)
                .list();
        return new LookupPage(ids, total);
    }

    public LookupPage lookupRecipeIds(
            DatasetSummary dataset, RecipeLookupQuery query, PageRequest page) {
        String kind = query.isUsage() ? "usage" : "recipe";
        String handlerId = query.hasHandlerFilter() ? query.handlerId() : null;
        String categoryId = query.hasCategoryFilter() ? query.categoryId() : null;
        boolean needsCategoryJoin = handlerId != null;

        StringBuilder baseFrom = new StringBuilder("FROM recipe_lookup_index rli\n");
        if (needsCategoryJoin) {
            baseFrom.append("""
                    JOIN recipe_category rc
                      ON rc.dataset_id = rli.dataset_id AND rc.category_id = rli.category_id
                    """);
        }

        StringBuilder filters = new StringBuilder("""
                WHERE rli.dataset_id = :datasetId
                  AND rli.target_id = :target
                  AND rli.lookup_kind = :kind
                """);
        if (handlerId != null) {
            filters.append("  AND rc.handler_id = :handlerId\n");
        }
        if (categoryId != null) {
            filters.append("  AND rli.category_id = :categoryId\n");
        }

        var totalSpec = jdbc.sql("SELECT COUNT(DISTINCT rli.recipe_id) " + baseFrom + filters)
                .param("datasetId", dataset.datasetId())
                .param("target", query.target())
                .param("kind", kind);
        if (handlerId != null) totalSpec = totalSpec.param("handlerId", handlerId);
        if (categoryId != null) totalSpec = totalSpec.param("categoryId", categoryId);
        long total = totalSpec.query(Long.class).single();

        var idsSpec = jdbc.sql("""
                        SELECT recipe_id FROM (
                          SELECT rli.recipe_id, MIN(rli.display_order) AS ord
                          """ + baseFrom + filters + """
                          GROUP BY rli.recipe_id
                        ) t
                        ORDER BY ord, recipe_id
                        OFFSET :offset LIMIT :limit
                        """)
                .param("datasetId", dataset.datasetId())
                .param("target", query.target())
                .param("kind", kind)
                .param("offset", page.offset())
                .param("limit", page.size());
        if (handlerId != null) idsSpec = idsSpec.param("handlerId", handlerId);
        if (categoryId != null) idsSpec = idsSpec.param("categoryId", categoryId);
        List<String> ids = idsSpec.query(String.class).list();
        return new LookupPage(ids, total);
    }

    public List<HandlerBreakdownDto> lookupBreakdown(
            DatasetSummary dataset, RecipeLookupQuery query) {
        String kind = query.isUsage() ? "usage" : "recipe";

        record BreakdownRow(
                String handlerId,
                String categoryId,
                String displayName,
                String iconAssetPath,
                int displayOrder,
                long count) {}

        List<BreakdownRow> rows = jdbc.sql("""
                        SELECT vc.handler_id,
                               rli.category_id,
                               vc.display_name,
                               vc.icon_asset_path,
                               vc.display_order,
                               COUNT(DISTINCT rli.recipe_id) AS cnt
                        FROM recipe_lookup_index rli
                        JOIN v_recipe_category_browser vc
                          ON vc.dataset_id = rli.dataset_id AND vc.category_id = rli.category_id
                        WHERE rli.dataset_id = :datasetId
                          AND rli.target_id = :target
                          AND rli.lookup_kind = :kind
                        GROUP BY vc.handler_id, rli.category_id, vc.display_name, vc.icon_asset_path, vc.display_order
                        """)
                .param("datasetId", dataset.datasetId())
                .param("target", query.target())
                .param("kind", kind)
                .query((rs, n) -> new BreakdownRow(
                        rs.getString("handler_id"),
                        rs.getString("category_id"),
                        rs.getString("display_name"),
                        rs.getString("icon_asset_path"),
                        rs.getInt("display_order"),
                        rs.getLong("cnt")))
                .list();

        Map<String, List<BreakdownRow>> byHandler = new LinkedHashMap<>();
        for (BreakdownRow r : rows) {
            byHandler.computeIfAbsent(r.handlerId(), k -> new ArrayList<>()).add(r);
        }

        record HandlerOut(
                String handlerId,
                String displayName,
                String iconAssetPath,
                int representativeOrder,
                long total,
                List<CategoryBreakdownDto> categories) {}

        List<HandlerOut> aggregates = new ArrayList<>();
        for (Map.Entry<String, List<BreakdownRow>> e : byHandler.entrySet()) {
            List<BreakdownRow> handlerRows = e.getValue();
            handlerRows.sort(Comparator
                    .comparingInt(BreakdownRow::displayOrder)
                    .thenComparing(BreakdownRow::categoryId));
            BreakdownRow rep = handlerRows.get(0);
            long total = 0L;
            List<CategoryBreakdownDto> categories = new ArrayList<>(handlerRows.size());
            for (BreakdownRow r : handlerRows) {
                total += r.count();
                categories.add(new CategoryBreakdownDto(
                        r.categoryId(),
                        r.displayName(),
                        assetUrlBuilder.build(dataset, r.iconAssetPath(), null),
                        r.count()));
            }
            aggregates.add(new HandlerOut(
                    e.getKey(),
                    rep.displayName(),
                    rep.iconAssetPath(),
                    rep.displayOrder(),
                    total,
                    categories));
        }
        aggregates.sort(Comparator
                .comparingLong(HandlerOut::total).reversed()
                .thenComparingInt(HandlerOut::representativeOrder)
                .thenComparing(HandlerOut::handlerId));

        List<HandlerBreakdownDto> out = new ArrayList<>(aggregates.size());
        for (HandlerOut h : aggregates) {
            out.add(new HandlerBreakdownDto(
                    h.handlerId(),
                    h.displayName(),
                    assetUrlBuilder.build(dataset, h.iconAssetPath(), null),
                    h.total(),
                    h.categories()));
        }
        return out;
    }

    public Optional<RecipeDto> loadRecipe(DatasetSummary dataset, String recipeId) {
        List<RecipeDto> results = loadRecipes(dataset, List.of(recipeId));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<RecipeDto> loadRecipes(DatasetSummary dataset, List<String> recipeIds) {
        if (recipeIds.isEmpty()) return List.of();

        Map<String, RecipeHeader> headers = loadHeaders(dataset, recipeIds);
        Map<String, List<RecipeSlotDto>> slotMap = loadSlots(dataset, recipeIds);
        Map<String, List<SlotLayoutDto>> layoutMap = loadLayouts(
                dataset, headers.values().stream().map(RecipeHeader::categoryId).distinct().toList());
        Map<String, CategoryLayoutMeta> categoryMeta = loadCategoryLayoutMeta(
                dataset, headers.values().stream().map(RecipeHeader::categoryId).distinct().toList());
        Map<String, GregTechRecipeDto> gtMap = loadGregTechInfo(dataset, recipeIds);

        List<RecipeDto> out = new ArrayList<>(recipeIds.size());
        for (String id : recipeIds) {
            RecipeHeader h = headers.get(id);
            if (h == null) continue;
            CategoryLayoutMeta meta = categoryMeta.getOrDefault(h.categoryId(),
                    new CategoryLayoutMeta(null, null, null));
            out.add(new RecipeDto(
                    id,
                    h.categoryId(),
                    h.categoryDisplayName(),
                    h.uiKind(),
                    h.uiTemplateId(),
                    h.sourcePlugin(),
                    h.sourceRef(),
                    h.description(),
                    slotMap.getOrDefault(id, List.of()),
                    layoutMap.getOrDefault(h.categoryId(), List.of()),
                    meta.canvasWidth(),
                    meta.canvasHeight(),
                    assetUrlBuilder.build(dataset, meta.backgroundAssetPath(), null),
                    gtMap.get(id)));
        }
        return out;
    }

    private Map<String, GregTechRecipeDto> loadGregTechInfo(
            DatasetSummary dataset, List<String> recipeIds) {
        if (recipeIds.isEmpty()) return Map.of();

        record GtRow(
                String recipeId,
                String voltageTier,
                long voltage,
                int amperage,
                int durationTicks,
                boolean requiresCleanroom,
                String additionalInfo) {}

        List<GtRow> rows = jdbc.sql("""
                        SELECT recipe_id, voltage_tier, voltage, amperage, duration_ticks,
                               requires_cleanroom, additional_info
                        FROM gregtech_recipe
                        WHERE dataset_id = :datasetId
                          AND recipe_id IN (:ids)
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new GtRow(
                        rs.getString("recipe_id"),
                        rs.getString("voltage_tier"),
                        rs.getLong("voltage"),
                        rs.getInt("amperage"),
                        rs.getInt("duration_ticks"),
                        rs.getBoolean("requires_cleanroom"),
                        rs.getString("additional_info")))
                .list();

        Map<String, List<GregTechSpecialItemDto>> specialItems =
                loadGregTechSpecialItems(dataset, recipeIds);

        Map<String, GregTechRecipeDto> map = new HashMap<>(rows.size());
        for (GtRow r : rows) {
            map.put(r.recipeId(), new GregTechRecipeDto(
                    r.voltageTier(),
                    r.voltage(),
                    r.amperage(),
                    r.durationTicks(),
                    r.requiresCleanroom(),
                    r.additionalInfo(),
                    specialItems.getOrDefault(r.recipeId(), List.of())));
        }
        return map;
    }

    private Map<String, List<GregTechSpecialItemDto>> loadGregTechSpecialItems(
            DatasetSummary dataset, List<String> recipeIds) {
        if (recipeIds.isEmpty()) return Map.of();

        record SpecialRow(
                String recipeId,
                int listIndex,
                String itemVariantId,
                String displayName,
                String modId,
                String assetPath,
                String assetSha) {}

        List<SpecialRow> rows = jdbc.sql("""
                        SELECT s.recipe_id, s.list_index, s.item_variant_id,
                               iv.display_name, iv.mod_id,
                               iv.asset_path, iv.asset_sha256
                        FROM gregtech_recipe_special_item s
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = s.dataset_id AND iv.item_variant_id = s.item_variant_id
                        WHERE s.dataset_id = :datasetId
                          AND s.recipe_id IN (:ids)
                        ORDER BY s.recipe_id, s.list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new SpecialRow(
                        rs.getString("recipe_id"),
                        rs.getInt("list_index"),
                        rs.getString("item_variant_id"),
                        rs.getString("display_name"),
                        rs.getString("mod_id"),
                        rs.getString("asset_path"),
                        rs.getString("asset_sha256")))
                .list();

        Map<String, List<GregTechSpecialItemDto>> map = new LinkedHashMap<>();
        for (SpecialRow r : rows) {
            map.computeIfAbsent(r.recipeId(), k -> new ArrayList<>())
                    .add(new GregTechSpecialItemDto(
                            r.itemVariantId(),
                            r.displayName(),
                            r.modId(),
                            assetUrlBuilder.build(dataset, r.assetPath(), r.assetSha())));
        }
        return map;
    }

    private Map<String, RecipeHeader> loadHeaders(DatasetSummary dataset, List<String> recipeIds) {
        List<RecipeHeader> rows = jdbc.sql("""
                        SELECT r.recipe_id, r.category_id, r.source_plugin, r.source_ref, r.description,
                               rc.display_name AS category_display_name, rc.ui_kind, rc.ui_template_id
                        FROM recipe r
                        JOIN recipe_category rc
                          ON rc.dataset_id = r.dataset_id AND rc.category_id = r.category_id
                        WHERE r.dataset_id = :datasetId
                          AND r.recipe_id IN (:ids)
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new RecipeHeader(
                        rs.getString("recipe_id"),
                        rs.getString("category_id"),
                        rs.getString("category_display_name"),
                        rs.getString("ui_kind"),
                        rs.getString("ui_template_id"),
                        rs.getString("source_plugin"),
                        rs.getString("source_ref"),
                        rs.getString("description")))
                .list();
        Map<String, RecipeHeader> map = new HashMap<>(rows.size());
        for (RecipeHeader h : rows) {
            map.put(h.recipeId(), h);
        }
        return map;
    }

    private Map<String, List<RecipeSlotDto>> loadSlots(DatasetSummary dataset, List<String> recipeIds) {
        List<SlotRow> rows = jdbc.sql("""
                        SELECT rs.recipe_id, rs.role, rs.slot_index, rs.group_id, rs.amount, rs.probability,
                               rs.item_variant_id AS item_direct,
                               rs.fluid_variant_id AS fluid_direct,
                               iv.display_name AS item_display_name,
                               iv.mod_id AS item_mod_id,
                               iv.asset_path AS item_asset_path,
                               fv.display_name AS fluid_display_name,
                               fv.mod_id AS fluid_mod_id,
                               fv.asset_path AS fluid_asset_path
                        FROM recipe_slot rs
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = rs.dataset_id AND iv.item_variant_id = rs.item_variant_id
                        LEFT JOIN v_fluid_variant_browser fv
                          ON fv.dataset_id = rs.dataset_id AND fv.fluid_variant_id = rs.fluid_variant_id
                        WHERE rs.dataset_id = :datasetId
                          AND rs.recipe_id IN (:ids)
                        ORDER BY rs.recipe_id, rs.role, rs.slot_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new SlotRow(
                        rs.getString("recipe_id"),
                        rs.getString("role"),
                        rs.getInt("slot_index"),
                        rs.getString("group_id"),
                        rs.getInt("amount"),
                        rs.getDouble("probability"),
                        rs.getString("item_direct"),
                        rs.getString("fluid_direct"),
                        rs.getString("item_display_name"),
                        rs.getString("item_mod_id"),
                        rs.getString("item_asset_path"),
                        rs.getString("fluid_display_name"),
                        rs.getString("fluid_mod_id"),
                        rs.getString("fluid_asset_path")))
                .list();

        // Collect group ids that need candidate lookups
        List<String> groupIds = rows.stream()
                .filter(r -> (r.itemDirect() == null || r.itemDirect().isEmpty())
                        && (r.fluidDirect() == null || r.fluidDirect().isEmpty()))
                .map(SlotRow::groupId)
                .filter(Objects::nonNull)
                .filter(g -> !g.isEmpty())
                .distinct()
                .toList();
        Map<String, List<RecipeSlotCandidateDto>> candidatesByGroup = loadCandidates(dataset, groupIds);

        Map<String, List<RecipeSlotDto>> byRecipe = new LinkedHashMap<>();
        for (SlotRow r : rows) {
            String assetUrl = null;
            String displayName = null;
            String modId = null;
            if (r.itemDirect() != null && !r.itemDirect().isEmpty()) {
                assetUrl = assetUrlBuilder.build(dataset, r.itemAssetPath(), null);
                displayName = r.itemDisplayName();
                modId = r.itemModId();
            } else if (r.fluidDirect() != null && !r.fluidDirect().isEmpty()) {
                assetUrl = assetUrlBuilder.build(dataset, r.fluidAssetPath(), null);
                displayName = r.fluidDisplayName();
                modId = r.fluidModId();
            }

            List<RecipeSlotCandidateDto> candidates =
                    (r.groupId() != null && !r.groupId().isEmpty())
                            ? candidatesByGroup.getOrDefault(r.groupId(), List.of())
                            : List.of();

            RecipeSlotDto slot = new RecipeSlotDto(
                    r.role(),
                    r.slotIndex(),
                    nullIfEmpty(r.itemDirect()),
                    nullIfEmpty(r.fluidDirect()),
                    r.amount(),
                    r.probability(),
                    nullIfEmpty(r.groupId()),
                    displayName,
                    modId,
                    assetUrl,
                    candidates);
            byRecipe.computeIfAbsent(r.recipeId(), k -> new ArrayList<>()).add(slot);
        }
        byRecipe.values().forEach(list -> list.sort(Comparator
                .comparing(RecipeSlotDto::role)
                .thenComparingInt(RecipeSlotDto::slotIndex)));
        return byRecipe;
    }

    private Map<String, List<RecipeSlotCandidateDto>> loadCandidates(
            DatasetSummary dataset, List<String> groupIds) {
        if (groupIds.isEmpty()) return Map.of();
        List<GroupEntry> entries = jdbc.sql("""
                        SELECT ie.group_id, ie.item_variant_id, ie.fluid_variant_id, ie.amount,
                               iv.display_name AS item_display_name,
                               iv.mod_id AS item_mod_id,
                               iv.asset_path AS item_asset_path,
                               fv.display_name AS fluid_display_name,
                               fv.mod_id AS fluid_mod_id,
                               fv.asset_path AS fluid_asset_path
                        FROM ingredient_entry ie
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = ie.dataset_id AND iv.item_variant_id = ie.item_variant_id
                        LEFT JOIN v_fluid_variant_browser fv
                          ON fv.dataset_id = ie.dataset_id AND fv.fluid_variant_id = ie.fluid_variant_id
                        WHERE ie.dataset_id = :datasetId
                          AND ie.group_id IN (:ids)
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", groupIds)
                .query((rs, n) -> new GroupEntry(
                        rs.getString("group_id"),
                        rs.getString("item_variant_id"),
                        rs.getString("fluid_variant_id"),
                        rs.getInt("amount"),
                        rs.getString("item_display_name"),
                        rs.getString("item_mod_id"),
                        rs.getString("item_asset_path"),
                        rs.getString("fluid_display_name"),
                        rs.getString("fluid_mod_id"),
                        rs.getString("fluid_asset_path")))
                .list();
        Map<String, List<RecipeSlotCandidateDto>> out = new HashMap<>();
        for (GroupEntry e : entries) {
            String item = nullIfEmpty(e.itemVariantId());
            String fluid = nullIfEmpty(e.fluidVariantId());
            if (item == null && fluid == null) continue;
            String displayName = item != null ? e.itemDisplayName() : e.fluidDisplayName();
            String modId = item != null ? e.itemModId() : e.fluidModId();
            String assetUrl = assetUrlBuilder.build(dataset,
                    item != null ? e.itemAssetPath() : e.fluidAssetPath(), null);
            out.computeIfAbsent(e.groupId(), k -> new ArrayList<>())
                    .add(new RecipeSlotCandidateDto(item, fluid, e.amount(), displayName, modId, assetUrl));
        }
        return out;
    }

    private Map<String, List<SlotLayoutDto>> loadLayouts(DatasetSummary dataset, List<String> categoryIds) {
        if (categoryIds.isEmpty()) return Map.of();
        List<LayoutRow> rows = jdbc.sql("""
                        SELECT category_id, role, slot_index, x, y, width, height, slot_style
                        FROM recipe_slot_layout
                        WHERE dataset_id = :datasetId
                          AND category_id IN (:ids)
                        ORDER BY category_id, role, slot_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", categoryIds)
                .query((rs, n) -> new LayoutRow(
                        rs.getString("category_id"),
                        new SlotLayoutDto(
                                rs.getString("role"),
                                rs.getInt("slot_index"),
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("width"),
                                rs.getInt("height"),
                                rs.getString("slot_style"))))
                .list();
        Map<String, List<SlotLayoutDto>> map = new LinkedHashMap<>();
        for (LayoutRow row : rows) {
            map.computeIfAbsent(row.categoryId(), k -> new ArrayList<>()).add(row.layout());
        }
        return map;
    }

    private Map<String, CategoryLayoutMeta> loadCategoryLayoutMeta(
            DatasetSummary dataset, List<String> categoryIds) {
        if (categoryIds.isEmpty()) return Map.of();
        List<Map.Entry<String, CategoryLayoutMeta>> rows = jdbc.sql("""
                        SELECT rcl.category_id, rcl.canvas_width, rcl.canvas_height, a.path AS background_asset_path
                        FROM recipe_category_layout rcl
                        LEFT JOIN asset a
                          ON a.dataset_id = rcl.dataset_id
                         AND a.asset_id = rcl.background_asset_id
                        WHERE rcl.dataset_id = :datasetId
                          AND rcl.category_id IN (:ids)
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", categoryIds)
                .query((rs, n) -> Map.entry(
                        rs.getString("category_id"),
                        new CategoryLayoutMeta(
                                rs.getInt("canvas_width"),
                                rs.getInt("canvas_height"),
                                rs.getString("background_asset_path"))))
                .list();
        Map<String, CategoryLayoutMeta> map = new HashMap<>(rows.size());
        for (Map.Entry<String, CategoryLayoutMeta> entry : rows) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    private static String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    public record LookupPage(List<String> recipeIds, long total) {}

    private record RecipeHeader(
            String recipeId,
            String categoryId,
            String categoryDisplayName,
            String uiKind,
            String uiTemplateId,
            String sourcePlugin,
            String sourceRef,
            String description) {}

    private record SlotRow(
            String recipeId,
            String role,
            int slotIndex,
            String groupId,
            int amount,
            double probability,
            String itemDirect,
            String fluidDirect,
            String itemDisplayName,
            String itemModId,
            String itemAssetPath,
            String fluidDisplayName,
            String fluidModId,
            String fluidAssetPath) {}

    private record GroupEntry(
            String groupId,
            String itemVariantId,
            String fluidVariantId,
            int amount,
            String itemDisplayName,
            String itemModId,
            String itemAssetPath,
            String fluidDisplayName,
            String fluidModId,
            String fluidAssetPath) {}

    private record LayoutRow(String categoryId, SlotLayoutDto layout) {}

    private record CategoryLayoutMeta(
            Integer canvasWidth,
            Integer canvasHeight,
            String backgroundAssetPath) {}
}
