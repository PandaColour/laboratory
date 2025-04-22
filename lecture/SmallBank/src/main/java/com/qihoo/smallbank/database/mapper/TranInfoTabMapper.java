package com.qihoo.smallbank.database.mapper;

import com.qihoo.smallbank.database.entity.TranInfoTab;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * 资金变动流水表
 *
 * @author 
 * @since 2025-04-08 17:10:25
 *
 */
@Mapper
public interface TranInfoTabMapper {

    TranInfoTab findById(Long id);

    TranInfoTab findBySeqNo(String seqNo);

    int insert(TranInfoTab record);

    int update(TranInfoTab record);

    int updateBySeqNo(TranInfoTab record);

    int delete(Long id);

}
