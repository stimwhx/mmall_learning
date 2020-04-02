package com.mmall.common;

public class Const {
    //这个是常量的类
    public static final String CURRENT_USER="currentuser";
    //验证是email还是用户名用的常量
    public static final String EMAIL="email";
    public static final String username="username";
    //通过一个内部的一个接口类定义我们的用户角色
    public interface Role{
        int ROLE_CUSTOME=0;//普通用户
        int ROLE_ADMIN=1;//管理员
    }
}
