package com.qihoo.smallbank.database.mapper;

import com.qihoo.smallbank.database.entity.TranHistoryTab;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * 资金变动流水表
 *
 * @author 
 * @since 2025-04-08 17:07:56
 *
 */
@Mapper
public interface TranHistoryTabMapper {

    TranHistoryTab findById(Long id);

    int insert(TranHistoryTab record);

    int update(TranHistoryTab record);

    int delete(Long id);

}
