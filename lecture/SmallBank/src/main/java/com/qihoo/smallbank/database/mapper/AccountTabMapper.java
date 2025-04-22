package com.qihoo.smallbank.database.mapper;

import com.qihoo.smallbank.database.entity.AccountTab;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * 账户信息表
 *
 * @author 
 * @since 2025-04-08 16:52:46
 *
 */
@Mapper
public interface AccountTabMapper {

    AccountTab findById(Long id);

    AccountTab findByAccountCodeForUpdate(String accountCode);

    int insert(AccountTab record);

    int update(AccountTab record);

    int updateCheckVersion(AccountTab record);

    int delete(Long id);

}
