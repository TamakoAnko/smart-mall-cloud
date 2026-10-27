package com.xzj.message.file.entity.dto;

import cn.lili.common.utils.BeanUtil;
import com.xzj.message.file.entity.FileDirectory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class FileDirectoryDTO extends FileDirectory {

    @Schema(description = "文件目录列表")
    private List<FileDirectory> children = new ArrayList<>();

    public FileDirectoryDTO(FileDirectory fileDirectory) {
        BeanUtil.copyProperties(fileDirectory, this);
    }
}
