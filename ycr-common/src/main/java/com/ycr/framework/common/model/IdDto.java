package com.ycr.framework.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 仅含主键 ID 的通用入参 DTO
 *
 * @author ycr
 */
@Data
public class IdDto {

    /** 主键 ID */
    @NotNull(message = "id 不能为空")
    private Long id;
}
