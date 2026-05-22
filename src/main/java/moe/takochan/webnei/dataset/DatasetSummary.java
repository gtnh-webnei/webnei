package moe.takochan.webnei.dataset;

import java.time.OffsetDateTime;
import java.util.List;

public record DatasetSummary(
        String datasetId,
        String packSlug,
        String packVersion,
        String variant,
        String language,
        String displayName,
        String schemaVersion,
        String exporterVersion,
        String minecraftVersion,
        OffsetDateTime createdAt,
        List<String> activePlugins) {
}
