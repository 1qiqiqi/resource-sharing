package com.dahaiwuliang.code.controller;

import com.dahaiwuliang.code.entity.Article;
import com.dahaiwuliang.code.entity.User;
import com.dahaiwuliang.code.service.ArcTypeService;
import com.dahaiwuliang.code.service.ArticleService;
import com.dahaiwuliang.code.service.MessageService;
import com.dahaiwuliang.code.service.UserService;
import com.dahaiwuliang.code.util.*;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 根路径及其他请求处理
 */
@Controller
public class IndexController {

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Value("${imgFilePath}")
    private String imgFilePath;         //图片上传路径

    /**
     * 首页
     */
    @RequestMapping("/")
    public ModelAndView index(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("index");
        //类型的html代码
        List arcTypleList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
        mav.addObject("arcTypeStr", HTMLUtil.getArcTypeStr("all",arcTypleList));
        //资源列表
        Map<String,Object> map = articleService.list("all",1, Consts.PAGE_SIZE);
        mav.addObject("articleList",map.get("data"));
        //分页html代码
        mav.addObject("pageStr",HTMLUtil.getPagation("/article/all",Integer.parseInt(String.valueOf(map.get("count"))),1,"该分类还没有数据..."));
        return mav;
    }

    /**
     * QQ登录回调
     */
    @RequestMapping("/connect")
    public String qqCallback(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws QQConnectException {
        response.setContentType("text/html;charset=utf-8");
        AccessToken accessTokenObj = new Oauth().getAccessTokenByRequest(request);
        String accessToken = null;
        String openId = null;
        String state = request.getParameter("state");
        String session_state = (String) session.getAttribute("qq_connect_state");
        if(StringUtil.isEmpty(session_state)||!session_state.equals(state)){
            System.out.println("非法请求");
            return "redirect:/";
        }
        accessToken = accessTokenObj.getAccessToken();
        if(StringUtil.isEmpty(accessToken)){
            System.out.println("没有获取到响应参数");
            return "redirect:/";
        }
        session.setAttribute("accessToken",accessToken);
        OpenID openIDObj = new OpenID(accessToken);
        openId = openIDObj.getUserOpenID();
        UserInfo qzoneUserInfo = new UserInfo(accessToken,openId);
        UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
        if(userInfoBean==null||userInfoBean.getRet()!=0||StringUtil.isNotEmpty(userInfoBean.getMsg())){
            System.out.println("没有对应的qq信息");
            return "redirect:/";
        }
        //获取用户成功
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if(currentUser!=null&&StringUtil.isNotEmpty(currentUser.getUserName())&&StringUtil.isNotEmpty(currentUser.getEmail())&&StringUtil.isEmpty(currentUser.getOpenId())){
            currentUser.setOpenId(openId);
            userService.save(currentUser);
            session.setAttribute(Consts.CURRENT_USER,currentUser);
            return "redirect:/";
        }

        User user = userService.findByOpenId(openId);
        if(user==null){             //该用户是第一次登录，先注册
            user = new User();
            user.setOpenId(openId);
            user.setNickname(userInfoBean.getNickname());
            String imgName = DateUtil.getCurrentDateStr()+".jpg";
            downloadPicture(userInfoBean.getAvatar().getAvatarURL100(),imgFilePath+imgName);
            user.setHeadPortrait(imgName);
            user.setSex(userInfoBean.getGender());
            user.setPassword(CryptographyUtil.md5("123456",CryptographyUtil.SALT));
            user.setRegistrationDate(new Date());
            user.setLatelyLoginTime(new Date());
            //userService.save(user);
            session.setAttribute(Consts.CURRENT_USER,user);
        }else{                  //已经注册过，更新用户信息，直接将信息存入session 然后跳转
            if(!user.isOff()){     //非封号状态
                user.setNickname(userInfoBean.getNickname());
                user.setSex(userInfoBean.getGender());
                user.setLatelyLoginTime(new Date());
                userService.save(user);
                Subject subject = SecurityUtils.getSubject();
                UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(),user.getPassword());
                subject.login(token);               //登录验证
                Integer messageCount = messageService.getCountByUserId(user.getUserId());
                user.setMessageCount(messageCount);
                Article s_article = new Article();
                s_article.setUseful(false);
                s_article.setUser(user);
                session.setAttribute(Consts.UN_USEFUL_ARTICLE_COUNT,articleService.getCount(s_article,null,null,null));
                session.setAttribute(Consts.CURRENT_USER,user);
            }
        }
        return "redirect:/";
    }

    /**
     * 通过链接下载图片保存到头像文件夹
     */
    private void downloadPicture(String urlString,String path){
        URL url = null;
        DataInputStream dataInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            url = new URL(urlString);
            dataInputStream = new DataInputStream(url.openStream());
            fileOutputStream = new FileOutputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer))>0){
                output.write(buffer,0,length);
            }
            fileOutputStream.write(output.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                dataInputStream.close();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 购买VIP
     */
    @RequestMapping("/buyVIP")
    public String buyVIP(){
        return "buyVIP";
    }

    /**
     * 发布资源赚积分
     */
    @RequestMapping("/fbzyzjf")
    public String fbzyzjf(){
        return "fbzyzjf";
    }
}





















