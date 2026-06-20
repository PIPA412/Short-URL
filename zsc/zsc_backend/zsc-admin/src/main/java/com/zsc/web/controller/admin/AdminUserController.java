package com.zsc.web.controller.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.zsc.common.annotation.Log;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.common.enums.BusinessType;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.common.utils.StringUtils;
import com.zsc.system.service.ISysUserService;

/**
 * 管理员 - 用户管理
 *
 * @author zsc
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    /**
     * 用户列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(null, user);
        return getDataTable(list);
    }

    /**
     * 用户详情
     */
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId)
    {
        return success(userService.selectUserById(userId));
    }

    /**
     * 新增用户
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysUser user)
    {
        if (!userService.checkUserNameUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        if (StringUtils.isNotNull(user.getUserId()) && SecurityUtils.isAdmin(user.getUserId()))
        {
            return error("不允许修改超级管理员");
        }
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        if (userIds != null && userIds.length > 0)
        {
            userService.deleteUserByIds(userIds);
        }
        return success();
    }

    /**
     * 重置密码
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        if (StringUtils.isNotNull(user.getUserId()) && SecurityUtils.isAdmin(user.getUserId()))
        {
            return error("不允许修改超级管理员");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改（启用/禁用）
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        if (StringUtils.isNotNull(user.getUserId()) && SecurityUtils.isAdmin(user.getUserId()))
        {
            return error("不允许修改超级管理员状态");
        }
        return toAjax(userService.updateUserStatus(user));
    }
}
