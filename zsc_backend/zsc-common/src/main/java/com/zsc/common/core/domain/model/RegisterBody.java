package com.zsc.common.core.domain.model;

/**
 * 用户注册对象
 *
 * @author zsc
 */
public class RegisterBody extends LoginBody
{
    /**
     * 邮箱
     */
    private String email;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
