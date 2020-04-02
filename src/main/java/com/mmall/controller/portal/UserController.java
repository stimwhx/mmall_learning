package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

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
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        //@ResponseBody这个注释是自动用MVC的jkens插件将我们的返回结果序列化成json
        //C:\Users\stimwhx\IdeaProjects\commmall\src\main\webapp\WEB-INF\dispatcher-servlet.xml在这个配置文件里配置的
        //现在要去这个目录下创建接口service--mybatis-->dao
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            //存session，第一个参数是值，第二个参数是返回值
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createrBySuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        //为用户防止通过接口调用我们的接口。我们在注册接口里写了判断是用户名不是email
        return iUserService.register(user);
    }

    //我们注册时，点击下一步调用下一个input框时要实时调用一个校验接口。校验是用户名还是email
    @RequestMapping(value = "checkValid.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        //str是用户名或email,type是常量。在const里定义的。用常量来判断传入的是什么
        return iUserService.checkValid(str, type);
    }

    //获取登录用户信息 /user/get_user_info.do
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createrBySuccess(user);
        }
        return ServerResponse.createrByErrorMsg("用户没有登录，无法获取用户登录信息");
    }

    //忘记密码获取密码的提示问题 /user/forget_get_question.do，返回类型是string,我们要把密码的提示问题返回给用户
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);

    }

    //检查忘记密码的答案是不是对的
    @RequestMapping(value = "forget_get_answer.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.chechAnswer(username, question, answer);
    }


}
