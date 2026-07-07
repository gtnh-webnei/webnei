package moe.takochan.webnei.dto.dataset;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatasetListEntry {

    private String datasetId;
    private String packSlug;
    private String packVersion;
    private String variant;
    private String language;
    private String displayName;
    private String schemaVersion;
    private String exporterVersion;
    private Instant createdAt;
    private String minecraftVersion;
}
