package moe.takochan.webnei.dataset.api;

import java.time.Instant;

public record DatasetSummary(
        String datasetId,
        String packSlug,
        String packVersion,
        String variant,
        String language,
        String displayName,
        String schemaVersion,
        String exporterVersion,
        Instant createdAt,
        String minecraftVersion) {}
