package com.mmall.controller.portal;

import com.google.gson.JsonObject;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/***
 * Controller是spring注解
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    IUserService iUserService;

    /***
     * 登录
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST,consumes = "application/json")
    @ResponseBody
    public ServerResponse<User> login(@RequestBody  String username,  HttpSession session, HttpServletRequest request, HttpServletResponse httpServletResponse) {

        // 取cookie
        String token = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                token = cookie.getValue();
                break;
            }
        }
        System.out.println("token: " + token);

        //@ResponseBody这个注释是自动用MVC的jkens插件将我们的返回结果序列化成json
        //C:\Users\stimwhx\IdeaProjects\commmall\src\main\webapp\WEB-INF\dispatcher-servlet.xml在这个配置文件里配置的
        //现在要去这个目录下创建接口service--mybatis-->dao

        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            //存session，第一个参数是值，第二个参数是返回值
            session.setAttribute(Const.CURRENT_USER, response.getData());

            // token 放到响应体里
            response.token = prefix(1) + UUID.randomUUID().toString();

            // 添加cookie
            httpServletResponse.addCookie(new Cookie("token", UUID.randomUUID().toString()));

        }
        return response;
    }

    private String prefix(int type) {
        if (type == 1) { // 学生
            return "002";
        } else if (type == 2) { // 老师
            return "003";
        }
        return "001";
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createrBySuccessMsg("退出成功");
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        //为用户防止通过接口调用我们的接口。我们在注册接口里写了判断是用户名不是email
        return iUserService.register(user);
    }

    //我们注册时，点击下一步调用下一个input框时要实时调用一个校验接口。校验是用户名还是email
    @RequestMapping(value = "checkValid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        //str是用户名或email,type是常量。在const里定义的。用常量来判断传入的是什么
        return iUserService.checkValid(str, type);
    }

    //获取登录用户信息 /user/get_user_info.do
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createrBySuccess(user);
        }
        return ServerResponse.createrByErrorMsg("用户没有登录，无法获取用户登录信息");
    }

    //忘记密码获取密码的提示问题 /user/forget_get_question.do，返回类型是string,我们要把密码的提示问题返回给用户
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);

    }

    //检查忘记密码的答案是不是对的
    @RequestMapping(value = "forget_get_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.chechAnswer(username, question, answer);
    }
//重置密码
@RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
@ResponseBody
public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
    return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
}
//登录状态的重置密码
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createrByErrorMsg("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

//更新用户个人信息
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createrByErrorMsg("用户未登录");
        }
        user.setId(currentUser.getId());//用户的id和username不能被改变。所以我们把user里的id和name放的是session当前的用户的id和那么
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createrByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
