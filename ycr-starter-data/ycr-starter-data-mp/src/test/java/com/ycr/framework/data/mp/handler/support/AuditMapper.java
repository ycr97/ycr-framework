package com.ycr.framework.data.mp.handler.support;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计字段集成测试 Mapper。
 *
 * @author ycr
 */
@Mapper
public interface AuditMapper extends BaseMapper<AuditEntity> {
}
