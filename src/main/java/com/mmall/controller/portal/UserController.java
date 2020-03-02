package com.mmall.controller.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    /***
     * 登录
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public Object login(String username, String password, HttpSession session){
        //@ResponseBody这个注释是自动用MVC的jkens插件将我们的返回结果序列化成json
        //C:\Users\stimwhx\IdeaProjects\commmall\src\main\webapp\WEB-INF\dispatcher-servlet.xml在这个配置文件里配置的
        //现在要去这个目录下创建接口service--mybatis-->dao
        return null;
    }
}
