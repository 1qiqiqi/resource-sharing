package com.dahaiwuliang.code.controller;

import com.dahaiwuliang.code.entity.User;
import com.dahaiwuliang.code.service.ArticleService;
import com.dahaiwuliang.code.service.MessageService;
import com.dahaiwuliang.code.service.UserService;
import com.dahaiwuliang.code.util.Consts;
import com.qq.connect.QQConnectException;
import com.qq.connect.oauth.Oauth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * qq登录请求处理
 */
@Controller
@RequestMapping(value = "/QQ")
public class QQController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ArticleService articleService;

    @Value("${imgFilePath}")
    private String imgFilePath;         //图片上传路径

    /**
     * qq登录页面跳转
     */
    @RequestMapping("/qqLogin")
    public void qqLogin(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/html;charset=utf-8");
        try {
            response.sendRedirect(new Oauth().getAuthorizeURL(request));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (QQConnectException e) {
            new QQConnectException("跳转到qq登录页面异常");
        }
    }

    /**
     * 解除绑定
     */
    @ResponseBody
    @PostMapping("/removeBind")
    public Map<String,Object> removeBind(HttpSession session){
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        userService.removeBind(currentUser.getUserId());
        currentUser.setOpenId(null);

        Map<String,Object> map = new HashMap<>();
        map.put("success",true);
        return map;
    }
}














