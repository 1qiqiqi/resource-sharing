package com.dahaiwuliang.code.controller.admin;

import com.dahaiwuliang.code.service.ArticleService;
import com.dahaiwuliang.code.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 管理员 - 首页或者跳转url控制器
 */
@Controller
public class IndexAdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    /**
     * 跳转到管理员主页面
     */
    @RequiresPermissions(value="进入管理员主页")
    @RequestMapping("/toAdminUserCenterPage")
    public String toAdminUserCenterPage(){
        return "admin/index";
    }

    /**
     * 跳转到管理员主页面
     */
    @RequiresPermissions(value="进入管理员主页")
    @RequestMapping("/defaultIndex")
    public ModelAndView defaultIndex(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("userNum",userService.getCount(null,null,null));
        mav.addObject("todayRegister",userService.todayRegister());
        mav.addObject("todayLogin",userService.todayLogin());
        mav.addObject("todayPublish",articleService.todayPublish());
        mav.addObject("noAudit",articleService.noAudit());
        mav.setViewName("admin/default");
        return mav;
    }
}
