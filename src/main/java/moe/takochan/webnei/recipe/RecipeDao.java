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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.recipe.dto.CategoryBreakdownDto;
import moe.takochan.webnei.recipe.dto.CategoryMachineDto;
import moe.takochan.webnei.recipe.dto.CategoryVoltageTierDto;
import moe.takochan.webnei.recipe.dto.GregTechRecipeDto;
import moe.takochan.webnei.recipe.dto.GregTechSpecialItemDto;
import moe.takochan.webnei.recipe.dto.HandlerBreakdownDto;
import moe.takochan.webnei.recipe.dto.MetadataValueDto;
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
    private final ObjectMapper objectMapper;

    public RecipeDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
        this.objectMapper = objectMapper;
    }

    public List<RecipeCategoryDto> listCategories(DatasetSummary dataset) {
        return queryCategories(dataset, null, null, false, null);
    }

    public PageResponse<RecipeCategoryDto> listCategoriesPage(
            DatasetSummary dataset, String query, String modId, boolean hideEmpty, PageRequest page) {
        String filter = (query == null || query.isBlank()) ? null : "%" + query.toLowerCase() + "%";
        String modIdParam = (modId == null || modId.isBlank()) ? null : modId;

        long total = jdbc.sql("""
                        SELECT COUNT(*) FROM v_recipe_category_browser
                        WHERE dataset_id = :datasetId
                          AND (CAST(:modId AS TEXT) IS NULL OR mod_id = CAST(:modId AS TEXT))
                          AND (CAST(:hideEmpty AS BOOLEAN) IS NULL
                               OR CAST(:hideEmpty AS BOOLEAN) = false
                               OR recipe_count > 0)
                          AND (CAST(:filter AS TEXT) IS NULL
                               OR lower(display_name) LIKE CAST(:filter AS TEXT)
                               OR lower(category_id) LIKE CAST(:filter AS TEXT)
                               OR lower(handler_id) LIKE CAST(:filter AS TEXT))
                        """)
                .param("datasetId", dataset.datasetId())
                .param("modId", modIdParam)
                .param("hideEmpty", hideEmpty)
                .param("filter", filter)
                .query(Long.class)
                .single();

        List<RecipeCategoryDto> items = queryCategories(dataset, filter, modIdParam, hideEmpty, page);
        return PageResponse.of(items, page, total);
    }

    public List<ModOptionDto> listCategoryMods(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT used.mod_id, COALESCE(m.name, used.mod_id) AS name
                        FROM (
                            SELECT DISTINCT mod_id
                            FROM v_recipe_category_browser
                            WHERE dataset_id = :datasetId AND mod_id <> ''
                        ) used
                        LEFT JOIN mod m
                          ON m.dataset_id = :datasetId AND m.mod_id = used.mod_id
                        ORDER BY name, used.mod_id
                        """)
                .param("datasetId", dataset.datasetId())
                .query((rs, n) -> new ModOptionDto(rs.getString("mod_id"), rs.getString("name")))
                .list();
    }

    private List<RecipeCategoryDto> queryCategories(
            DatasetSummary dataset, String filter, String modId, boolean hideEmpty, PageRequest page) {
        StringBuilder sql = new StringBuilder("""
                SELECT v.category_id, v.plugin, v.handler_id, v.display_name,
                       v.shapeless, v.icon_item_variant_id, v.icon_display_name,
                       COALESCE(v.icon_asset_path, nei.exported_path) AS icon_asset_path,
                       v.icon_info, v.item_input_width, v.item_input_height,
                       v.fluid_input_width, v.fluid_input_height,
                       v.item_output_width, v.item_output_height,
                       v.fluid_output_width, v.fluid_output_height,
                       v.supports_recipe_lookup, v.supports_usage_lookup, v.display_order,
                       v.canvas_width, v.canvas_height, v.background_asset_path,
                       v.recipe_count, v.machine_count,
                       v.mod_id, v.mod_name, v.handler_class,
                       v.handler_canvas_width, v.handler_canvas_height, v.handler_y_shift,
                       v.handler_multiple_widgets_allowed,
                       v.icon_image_resource, v.icon_image_x, v.icon_image_y,
                       v.icon_image_width, v.icon_image_height,
                       v.icon_image_texture_width, v.icon_image_texture_height
                FROM v_recipe_category_browser v
                LEFT JOIN nei_texture_export nei
                  ON nei.dataset_id = v.dataset_id
                 AND nei.resource = v.icon_image_resource
                 AND v.icon_asset_path IS NULL
                WHERE v.dataset_id = :datasetId
                  AND (CAST(:modId AS TEXT) IS NULL OR v.mod_id = CAST(:modId AS TEXT))
                  AND (CAST(:hideEmpty AS BOOLEAN) IS NULL
                       OR CAST(:hideEmpty AS BOOLEAN) = false
                       OR v.recipe_count > 0)
                  AND (CAST(:filter AS TEXT) IS NULL
                       OR lower(v.display_name) LIKE CAST(:filter AS TEXT)
                       OR lower(v.category_id) LIKE CAST(:filter AS TEXT)
                       OR lower(v.handler_id) LIKE CAST(:filter AS TEXT))
                ORDER BY v.display_order, v.category_id
                """);
        if (page != null) {
            sql.append("OFFSET :offset LIMIT :limit\n");
        }
        var spec = jdbc.sql(sql.toString())
                .param("datasetId", dataset.datasetId())
                .param("modId", modId)
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
                        rs.getLong("machine_count"),
                        rs.getString("mod_id"),
                        rs.getString("mod_name"),
                        rs.getString("handler_class"),
                        rs.getInt("handler_canvas_width"),
                        rs.getInt("handler_canvas_height"),
                        rs.getInt("handler_y_shift"),
                        rs.getBoolean("handler_multiple_widgets_allowed"),
                        rs.getString("icon_image_resource"),
                        rs.getInt("icon_image_x"),
                        rs.getInt("icon_image_y"),
                        rs.getInt("icon_image_width"),
                        rs.getInt("icon_image_height"),
                        rs.getInt("icon_image_texture_width"),
                        rs.getInt("icon_image_texture_height")))
                .list();
    }

    public LookupPage listRecipeIdsByCategory(
            DatasetSummary dataset, String categoryId, String query, String voltageTier, PageRequest page) {
        String q = (query == null) ? "" : query.trim();
        boolean hasVoltage = voltageTier != null && !voltageTier.isBlank();
        String voltageJoin = hasVoltage
                ? "JOIN gregtech_recipe g ON g.recipe_id = recipe.recipe_id AND g.dataset_id = recipe.dataset_id\n"
                : "";
        String voltageWhere = hasVoltage ? "  AND g.voltage_tier = :voltageTier\n" : "";

        if (q.isEmpty()) {
            var totalSpec = jdbc.sql(
                            "SELECT COUNT(*) FROM recipe\n"
                            + voltageJoin
                            + "WHERE recipe.dataset_id = :datasetId AND recipe.category_id = :categoryId\n"
                            + voltageWhere)
                    .param("datasetId", dataset.datasetId())
                    .param("categoryId", categoryId);
            if (hasVoltage) totalSpec = totalSpec.param("voltageTier", voltageTier);
            long total = totalSpec.query(Long.class).single();

            var idsSpec = jdbc.sql(
                            "SELECT recipe.recipe_id FROM recipe\n"
                            + voltageJoin
                            + "WHERE recipe.dataset_id = :datasetId AND recipe.category_id = :categoryId\n"
                            + voltageWhere
                            + "ORDER BY recipe.recipe_id\n"
                            + "OFFSET :offset LIMIT :limit\n")
                    .param("datasetId", dataset.datasetId())
                    .param("categoryId", categoryId)
                    .param("offset", page.offset())
                    .param("limit", page.size());
            if (hasVoltage) idsSpec = idsSpec.param("voltageTier", voltageTier);
            List<String> ids = idsSpec.query(String.class).list();
            return new LookupPage(ids, total);
        }

        String pattern = "%" + q.toLowerCase() + "%";
        // Match recipes by item/fluid display name or id, optionally narrowed by voltage tier.
        // The matched-id subquery is unioned across item and fluid slot views; the voltage filter
        // is applied as an outer join because gregtech_recipe is GT-only.
        String matchInner = """
                SELECT DISTINCT m.recipe_id FROM (
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
        String matchSql;
        if (hasVoltage) {
            matchSql = matchInner
                    + "JOIN gregtech_recipe g\n"
                    + "  ON g.recipe_id = m.recipe_id AND g.dataset_id = :datasetId\n"
                    + "WHERE g.voltage_tier = :voltageTier\n";
        } else {
            matchSql = matchInner;
        }

        var totalSpec = jdbc.sql("SELECT COUNT(*) FROM (" + matchSql + ") c")
                .param("datasetId", dataset.datasetId())
                .param("categoryId", categoryId)
                .param("pattern", pattern);
        if (hasVoltage) totalSpec = totalSpec.param("voltageTier", voltageTier);
        long total = totalSpec.query(Long.class).single();

        var idsSpec = jdbc.sql(matchSql + " ORDER BY recipe_id OFFSET :offset LIMIT :limit")
                .param("datasetId", dataset.datasetId())
                .param("categoryId", categoryId)
                .param("pattern", pattern)
                .param("offset", page.offset())
                .param("limit", page.size());
        if (hasVoltage) idsSpec = idsSpec.param("voltageTier", voltageTier);
        List<String> ids = idsSpec.query(String.class).list();
        return new LookupPage(ids, total);
    }

    public LookupPage lookupRecipeIds(
            DatasetSummary dataset, RecipeLookupQuery query, PageRequest page) {
        String kind = query.isUsage() ? "usage" : "recipe";
        String handlerId = query.hasHandlerFilter() ? query.handlerId() : null;
        String categoryId = query.hasCategoryFilter() ? query.categoryId() : null;
        String voltageTier = query.hasVoltageTierFilter() ? query.voltageTier() : null;
        boolean needsCategoryJoin = handlerId != null;
        boolean needsVoltageJoin = voltageTier != null;

        StringBuilder baseFrom = new StringBuilder("FROM recipe_lookup_index rli\n");
        if (needsCategoryJoin) {
            baseFrom.append("""
                    JOIN recipe_category rc
                      ON rc.dataset_id = rli.dataset_id AND rc.category_id = rli.category_id
                    """);
        }
        if (needsVoltageJoin) {
            baseFrom.append("""
                    JOIN gregtech_recipe g
                      ON g.recipe_id = rli.recipe_id AND g.dataset_id = rli.dataset_id
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
        if (voltageTier != null) {
            filters.append("  AND g.voltage_tier = :voltageTier\n");
        }

        var totalSpec = jdbc.sql("SELECT COUNT(DISTINCT rli.recipe_id) " + baseFrom + filters)
                .param("datasetId", dataset.datasetId())
                .param("target", query.target())
                .param("kind", kind);
        if (handlerId != null) totalSpec = totalSpec.param("handlerId", handlerId);
        if (categoryId != null) totalSpec = totalSpec.param("categoryId", categoryId);
        if (voltageTier != null) totalSpec = totalSpec.param("voltageTier", voltageTier);
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
        if (voltageTier != null) idsSpec = idsSpec.param("voltageTier", voltageTier);
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
                               COALESCE(vc.icon_asset_path, nei.exported_path) AS icon_asset_path,
                               vc.display_order,
                               COUNT(DISTINCT rli.recipe_id) AS cnt
                        FROM recipe_lookup_index rli
                        JOIN v_recipe_category_browser vc
                          ON vc.dataset_id = rli.dataset_id AND vc.category_id = rli.category_id
                        LEFT JOIN nei_texture_export nei
                          ON nei.dataset_id = vc.dataset_id
                         AND nei.resource = vc.icon_image_resource
                         AND vc.icon_asset_path IS NULL
                        WHERE rli.dataset_id = :datasetId
                          AND rli.target_id = :target
                          AND rli.lookup_kind = :kind
                        GROUP BY vc.handler_id, rli.category_id, vc.display_name,
                                 COALESCE(vc.icon_asset_path, nei.exported_path),
                                 vc.display_order
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
        Map<String, GregTechRecipeDto> gtMap = loadGregTechInfo(dataset, headers);

        List<RecipeDto> out = new ArrayList<>(recipeIds.size());
        for (String id : recipeIds) {
            RecipeHeader h = headers.get(id);
            if (h == null) continue;
            CategoryLayoutMeta meta = categoryMeta.getOrDefault(h.categoryId(),
                    new CategoryLayoutMeta(null, null, null));

            // Merge placement from layout into slots
            List<RecipeSlotDto> slots = slotMap.getOrDefault(id, List.of());
            List<SlotLayoutDto> layouts = layoutMap.getOrDefault(h.categoryId(), List.of());
            List<RecipeSlotDto> slotsWithPlacement = mergeSlotPlacement(slots, layouts);

            out.add(new RecipeDto(
                    id,
                    h.categoryId(),
                    h.categoryDisplayName(),
                    h.sourcePlugin(),
                    h.sourceRef(),
                    h.description(),
                    slotsWithPlacement,
                    layouts,
                    meta.canvasWidth(),
                    meta.canvasHeight(),
                    assetUrlBuilder.build(dataset, meta.backgroundAssetPath(), null),
                    gtMap.get(id)));
        }
        return out;
    }

    private List<RecipeSlotDto> mergeSlotPlacement(List<RecipeSlotDto> slots, List<SlotLayoutDto> layouts) {
        // Build a map of (role, slotIndex) -> placement
        Map<String, String> placementMap = new HashMap<>();
        for (SlotLayoutDto layout : layouts) {
            String key = layout.role() + ":" + layout.slotIndex();
            if (layout.placement() != null) {
                placementMap.put(key, layout.placement());
            }
        }

        // Create new slots with placement
        List<RecipeSlotDto> result = new ArrayList<>(slots.size());
        for (RecipeSlotDto slot : slots) {
            String key = slot.role() + ":" + slot.slotIndex();
            String placement = placementMap.get(key);
            result.add(new RecipeSlotDto(
                    slot.role(),
                    slot.slotIndex(),
                    slot.itemVariantId(),
                    slot.fluidVariantId(),
                    slot.amount(),
                    slot.probability(),
                    slot.groupId(),
                    slot.displayName(),
                    slot.modId(),
                    slot.assetUrl(),
                    slot.candidates(),
                    placement));
        }
        return result;
    }

    private Map<String, GregTechRecipeDto> loadGregTechInfo(
            DatasetSummary dataset, Map<String, RecipeHeader> headers) {
        if (headers.isEmpty()) return Map.of();
        List<String> recipeIds = new ArrayList<>(headers.keySet());

        record GtRow(
                String recipeId,
                String recipeKind,
                boolean visibleInNei,
                String voltageTier,
                Integer voltage,
                Integer amperage,
                int durationTicks,
                Integer specialValue) {}

        List<GtRow> rows = jdbc.sql("""
                        SELECT recipe_id, recipe_kind, visible_in_nei,
                               voltage_tier, voltage, amperage, duration_ticks,
                               special_value
                        FROM gregtech_recipe
                        WHERE dataset_id = :datasetId
                          AND recipe_id IN (:ids)
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new GtRow(
                        rs.getString("recipe_id"),
                        rs.getString("recipe_kind"),
                        rs.getBoolean("visible_in_nei"),
                        rs.getString("voltage_tier"),
                        rs.getObject("voltage", Integer.class),
                        rs.getObject("amperage", Integer.class),
                        rs.getInt("duration_ticks"),
                        rs.getObject("special_value", Integer.class)))
                .list();

        Map<String, List<GregTechSpecialItemDto>> specialItems =
                loadGregTechSpecialItems(dataset, recipeIds);
        Map<String, Map<String, MetadataValueDto>> metadataByRecipe =
                loadGregTechMetadata(dataset, recipeIds);

        Map<String, GregTechRecipeDto> map = new HashMap<>(rows.size());
        for (GtRow r : rows) {
            map.put(r.recipeId(), new GregTechRecipeDto(
                    r.recipeKind(),
                    r.visibleInNei(),
                    r.voltageTier(),
                    r.voltage(),
                    r.amperage(),
                    r.durationTicks(),
                    r.specialValue(),
                    specialItems.getOrDefault(r.recipeId(), List.of()),
                    metadataByRecipe.getOrDefault(r.recipeId(), Map.of())));
        }
        return map;
    }

    private Map<String, Map<String, MetadataValueDto>> loadGregTechMetadata(
            DatasetSummary dataset, List<String> recipeIds) {
        if (recipeIds.isEmpty()) return Map.of();

        record MetaRow(
                String recipeId,
                String key,
                String valueType,
                String valueText,
                String valueJson) {}

        List<MetaRow> rows = jdbc.sql("""
                        SELECT recipe_id, metadata_key, value_type, value_text, value_json
                        FROM gregtech_recipe_metadata
                        WHERE dataset_id = :datasetId
                          AND recipe_id IN (:ids)
                        ORDER BY recipe_id, metadata_key
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new MetaRow(
                        rs.getString("recipe_id"),
                        rs.getString("metadata_key"),
                        rs.getString("value_type"),
                        rs.getString("value_text"),
                        rs.getString("value_json")))
                .list();

        Map<String, Map<String, MetadataValueDto>> map = new LinkedHashMap<>();
        for (MetaRow r : rows) {
            JsonNode parsed = parseMetadataJson(r.valueJson(), r.recipeId(), r.key());
            map.computeIfAbsent(r.recipeId(), k -> new LinkedHashMap<>())
                    .put(r.key(), new MetadataValueDto(r.valueType(), r.valueText(), parsed));
        }
        return map;
    }

    private JsonNode parseMetadataJson(String raw, String recipeId, String key) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return objectMapper.readTree(raw);
        } catch (JacksonException ex) {
            throw new IllegalStateException(
                    "Failed to parse gregtech_recipe_metadata.value_json for recipe="
                            + recipeId + ", key=" + key, ex);
        }
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
                               rc.display_name AS category_display_name
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
                    candidates,
                    null);  // placement will be merged later in loadRecipes
            byRecipe.computeIfAbsent(r.recipeId(), k -> new ArrayList<>()).add(slot);
        }

        // Load GregTech special items
        List<SpecialItemRow> specialRows = jdbc.sql("""
                        SELECT si.recipe_id, si.list_index, si.item_variant_id,
                               iv.display_name, iv.mod_id, iv.asset_path
                        FROM gregtech_recipe_special_item si
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = si.dataset_id AND iv.item_variant_id = si.item_variant_id
                        WHERE si.dataset_id = :datasetId
                          AND si.recipe_id IN (:ids)
                        ORDER BY si.recipe_id, si.list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("ids", recipeIds)
                .query((rs, n) -> new SpecialItemRow(
                        rs.getString("recipe_id"),
                        rs.getInt("list_index"),
                        rs.getString("item_variant_id"),
                        rs.getString("display_name"),
                        rs.getString("mod_id"),
                        rs.getString("asset_path")))
                .list();

        for (SpecialItemRow si : specialRows) {
            String assetUrl = assetUrlBuilder.build(dataset, si.assetPath(), null);
            RecipeSlotDto slot = new RecipeSlotDto(
                    "special_item",
                    si.listIndex(),
                    si.itemVariantId(),
                    null,
                    1,
                    1.0,
                    null,
                    si.displayName(),
                    si.modId(),
                    assetUrl,
                    List.of(),
                    null);  // placement will be merged later in loadRecipes
            byRecipe.computeIfAbsent(si.recipeId(), k -> new ArrayList<>()).add(slot);
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
                        SELECT category_id, role, slot_index, x, y, width, height, slot_style, placement
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
                                rs.getString("slot_style"),
                                rs.getString("placement"))))
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

    public List<CategoryMachineDto> listCategoryMachines(DatasetSummary dataset, String categoryId) {
        return jdbc.sql("""
                        SELECT m.item_variant_id,
                               iv.display_name,
                               iv.asset_path,
                               m.role,
                               m.display_order
                        FROM recipe_category_machine m
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.item_variant_id = m.item_variant_id
                         AND iv.dataset_id = m.dataset_id
                        WHERE m.dataset_id = :datasetId
                          AND m.category_id = :categoryId
                        ORDER BY m.display_order, m.item_variant_id
                        """)
                .param("datasetId", dataset.datasetId())
                .param("categoryId", categoryId)
                .query((rs, n) -> new CategoryMachineDto(
                        rs.getString("item_variant_id"),
                        rs.getString("display_name"),
                        assetUrlBuilder.build(dataset, rs.getString("asset_path"), null),
                        rs.getString("role"),
                        rs.getInt("display_order")))
                .list();
    }

    public List<CategoryVoltageTierDto> listCategoryVoltageTiers(
            DatasetSummary dataset, String categoryId, String target, String kind) {
        boolean hasLookup = target != null && !target.isBlank() && kind != null && !kind.isBlank();
        String lookupJoin = hasLookup
                ? "JOIN recipe_lookup_index rli\n"
                + "  ON rli.recipe_id = r.recipe_id AND rli.dataset_id = r.dataset_id\n"
                + " AND rli.target_id = :target AND rli.lookup_kind = :kind\n"
                : "";
        String countExpr = hasLookup ? "COUNT(DISTINCT r.recipe_id)" : "COUNT(*)";

        var spec = jdbc.sql("""
                        SELECT g.voltage_tier AS tier,
                               %s AS cnt
                        FROM recipe r
                        JOIN gregtech_recipe g
                          ON g.recipe_id = r.recipe_id AND g.dataset_id = r.dataset_id
                        %s
                        WHERE r.dataset_id = :datasetId
                          AND r.category_id = :categoryId
                          AND g.voltage_tier IS NOT NULL
                        GROUP BY g.voltage_tier
                        ORDER BY MIN(g.voltage) NULLS LAST
                        """.formatted(countExpr, lookupJoin))
                .param("datasetId", dataset.datasetId())
                .param("categoryId", categoryId);
        if (hasLookup) {
            spec = spec.param("target", target).param("kind", kind);
        }
        return spec
                .query((rs, n) -> new CategoryVoltageTierDto(
                        rs.getString("tier"),
                        rs.getLong("cnt")))
                .list();
    }

    public record LookupPage(List<String> recipeIds, long total) {}

    private record RecipeHeader(
            String recipeId,
            String categoryId,
            String categoryDisplayName,
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

    private record SpecialItemRow(
            String recipeId,
            int listIndex,
            String itemVariantId,
            String displayName,
            String modId,
            String assetPath) {}

    private record LayoutRow(String categoryId, SlotLayoutDto layout) {}

    private record CategoryLayoutMeta(
            Integer canvasWidth,
            Integer canvasHeight,
            String backgroundAssetPath) {}
}
