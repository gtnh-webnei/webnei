package moe.takochan.webnei.entity;

import java.time.Instant;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;

@Getter
@TableName("dataset")
public class DatasetEntity {

    @TableId("dataset_id")
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
