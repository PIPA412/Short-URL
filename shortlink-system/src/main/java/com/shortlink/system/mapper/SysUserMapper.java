package com.shortlink.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * SysUser mapper — extends MyBatis-Plus BaseMapper for standard CRUD.
 *
 * @author ShortLink
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // Inherited methods from BaseMapper provide all standard CRUD operations.
    // Custom queries can be added here or in the corresponding XML file.
}
