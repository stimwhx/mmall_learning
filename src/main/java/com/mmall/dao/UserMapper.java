package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
//检查用户是否存在的方法
    int checkUserName(String userName);
    //用户一但登录，我们要把用户的信息返回给前端
    //- *?///不要用select * 要什么字段查什么字段 我们这里返回的类型要求是user类型的 我们在mapper的最上边以已定义了user类
    User selectLogin(@Param("username") String username,@Param("password") String password);
    //检查email是否存在
    int checkEmail(String email);
    //取出忘记密码的问题
    String selectQuestion(String username);
    //查有没有这个问题的答案
    int chechAnswer(@Param("username")String username,@Param("question")String question,@Param("answer")String answer);
}