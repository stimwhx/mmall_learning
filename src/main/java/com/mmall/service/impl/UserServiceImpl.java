package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

//把servers注入到controller上供controller调用，这个注解就是注入到controller上的
@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    UserMapper userMapper ;
    //接口的实现
    @Override
    public ServerResponse<User> login(String username, String password) {
        //登录首先判断用户是否存在
        int resultCount = userMapper.checkUserName(username);
        if(resultCount==0){
            return ServerResponse.createrByErrorMsg("用户不存在");
        }
        //todo MD5 加密
        //注册时是md5加密的，存入了数据库。登录时也要把这个加密后再传入
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createrByErrorMsg("密码错误");
        }
        //到这里都没有return,我们要处理返回值的密码了。为什么把密码设置为空呀？
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createrBySuccess("登录成功",user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //判断用户名是否存在
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.username);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //如果不存在，我们给设置角色，是普通用户还是管理员
        user.setRole(Const.Role.ROLE_CUSTOME);
        //md5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
       int resultCount=userMapper.insert(user);//将这个用户插入数据库
        if(resultCount==0){
            //说明插入失败
            return ServerResponse.createrByErrorMsg("注册失败");
        }
        return ServerResponse.createrBySuccessMsg("注册成功");
    }
public ServerResponse<String>checkValid(String str,String type){
        if(StringUtils.isNotBlank(type)){
            //isNotBlank（）是把空字符判断为false，isNotEmplay是把空判为真。我们先判断用户名是不是空。不是空判断类型
            if(Const.username.equals(type)){
                int resultCount = userMapper.checkUserName(str);
                if(resultCount>0){
                    return  ServerResponse.createrByErrorMsg("用户已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount>0){
                    return  ServerResponse.createrByErrorMsg("email已存在");
                }
            }

        }else{
         return   ServerResponse.createrBySuccessMsg("参数错误");
        }
        return ServerResponse.createrBySuccessMsg("校验成功");
}

public ServerResponse<String> selectQuestion(String username){
    ServerResponse validResponse = this.checkValid(username,Const.username);
    if(validResponse.isSuccess()){
        //因为这个checkvalid是校验用户存在不存在的，不存在返回校验成功。用户不存在没有忘记密码这一条。
        //用户不存在
        return ServerResponse.createrByErrorMsg("用户不存在");
    }
    //上边没有执行说明用户已存在返回密码的修改问题
    String retQuestion=userMapper.selectQuestion(username);
    if(StringUtils.isNotBlank(retQuestion)){
        return ServerResponse.createrBySuccess(retQuestion);
    }
    return ServerResponse.createrByErrorMsg("找回密码的问题是空的");
}

//校验输入 问题的答案是不是正确的
    public ServerResponse<String> chechAnswer(String username,String question,String answer){
        //去数据库里查，answer的数量，如果 大于1说明存在
        int retCount = userMapper.chechAnswer(username,question,answer);
        if(retCount>0){
            //说明问题和答案对上了，是这个用户的。那么我们就生成token。用java自带的UUID
            String forgetToken = UUID.randomUUID().toString();
            //我们把token放到本地的cach，去common里创建一个公共类
            TokenCache.setToken(TokenCache.TOKEN_PREFIX+username,forgetToken);//这就把token放在缓存里了
            return ServerResponse.createrBySuccess(forgetToken);//答案对了我们就把token返回去


        }
        return ServerResponse.createrByErrorMsg("答案错误");
    }
//重置密码
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){
            return ServerResponse.createrByErrorMsg("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username,Const.username);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createrByErrorMsg("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        //用户这个isBlank更安全，即使传入的是null返回的也是空
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){
            return ServerResponse.createrByErrorMsg("token无效或者过期");
        }

        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);

            if(rowCount > 0){
                return ServerResponse.createrBySuccessMsg("修改密码成功");
            }
        }else{
            return ServerResponse.createrByErrorMsg("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createrByErrorMsg("修改密码失败");
    }
//登录状态下的重置密码
public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
    //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
    int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
    if(resultCount == 0){
        return ServerResponse.createrByErrorMsg("旧密码错误");
    }

    user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
    //updateByPrimaryKeySelective，这个在usermapper执行sql语句 时，就是字段那个不为空我更新那个。他下边updateByPrimaryKey的那个更新，是更新全部
    int updateCount = userMapper.updateByPrimaryKeySelective(user);
    if(updateCount > 0){
        return ServerResponse.createrBySuccessMsg("密码更新成功");
    }
    return ServerResponse.createrByErrorMsg("密码更新失败");
}

//更新用户信息
public ServerResponse<User> updateInformation(User user){
    //username是不能被更新的
    //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
    int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
    if(resultCount > 0){
        return ServerResponse.createrByErrorMsg("email已存在,请更换email再尝试更新");
    }
    User updateUser = new User();
    updateUser.setId(user.getId());
    updateUser.setEmail(user.getEmail());
    updateUser.setPhone(user.getPhone());
    updateUser.setQuestion(user.getQuestion());
    updateUser.setAnswer(user.getAnswer());

    int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
    if(updateCount > 0){
        return ServerResponse.createrBySuccess("更新个人信息成功",updateUser);
    }
    return ServerResponse.createrByErrorMsg("更新个人信息失败");
}
//获取用户信息
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);//用户主键去查是不是有这个用户
        if(user == null){
            return ServerResponse.createrByErrorMsg("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createrBySuccess(user);

    }
    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createrBySuccess();
        }
        return ServerResponse.createrByError();
    }

}
