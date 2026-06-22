package com.two.backend.mapper;

import com.two.backend.model.Companion;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
/**
 * 随行人员 Mapper，负责 companions 表的查询、插入和删除。
 */
public interface CompanionMapper {
    /**
     * 查询某条申请下的随行人员。
     */
    List<Companion> listByApplication(@Param("applicationId") Long applicationId);

    /**
     * 插入随行人员记录，并回填自增 ID。
     */
    int insert(Companion companion);

    /**
     * 删除某条申请下的全部随行人员。
     */
    int deleteByApplication(@Param("applicationId") Long applicationId);
}
