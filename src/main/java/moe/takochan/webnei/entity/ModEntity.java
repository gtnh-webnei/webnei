package moe.takochan.webnei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("mod")
public class ModEntity {

    private String datasetId;

    @TableId(value = "mod_id", type = IdType.INPUT)
    private String modId;

    private String name;
    private String version;
    private String sourceType;
    private String sourceFileName;
    private String sourceSha256;
    private boolean enabled;
}
