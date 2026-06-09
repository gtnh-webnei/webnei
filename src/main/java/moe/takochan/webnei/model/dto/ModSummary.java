package moe.takochan.webnei.model.dto;

public record ModSummary(
        String modId,
        String name,
        String version,
        String sourceType,
        String sourceFileName,
        String sourceSha256,
        boolean enabled) {
}
