package com.dahaiwuliang.code.controller;

import com.dahaiwuliang.code.entity.Article;
import com.dahaiwuliang.code.entity.Message;
import com.dahaiwuliang.code.entity.User;
import com.dahaiwuliang.code.entity.UserDownload;
import com.dahaiwuliang.code.lucene.ArticleIndex;
import com.dahaiwuliang.code.service.*;
import com.dahaiwuliang.code.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 用户控制器
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    UserService userService;

    @Resource
    private JavaMailSender mailSender;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private UserDownloadService userDownloadService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MessageService messageService;

    @Value("${imgFilePath}")
    private String imgFilePath;         //图片上传路径

    /**
     * 用户注册
     */
    @ResponseBody
    @PostMapping("/register")
    public Map<String,Object> register(@Valid User user, BindingResult bindingResult){
        Map<String,Object> map = new HashMap<>();
        if(bindingResult.hasErrors()){
            map.put("success",false);
            map.put("errorInfo",bindingResult.getFieldError().getDefaultMessage());
        }else if(userService.findByUserName(user.getUserName())!=null){
            map.put("success",false);
            map.put("errorInfo","用户名已存在，请更换！");
        }else if(userService.findByEmail(user.getEmail())!=null){
            map.put("success",false);
            map.put("errorInfo","邮箱已存在，请更换！");
        }else{
            user.setPassword(CryptographyUtil.md5(user.getPassword(),CryptographyUtil.SALT));
            user.setRegistrationDate(new Date());
            user.setLatelyLoginTime(new Date());
            user.setHeadPortrait("tou.jpg");
            userService.save(user);
            map.put("success",true);
        }
        return map;
    }

    /**
     * 用户登录
     */
    @ResponseBody
    @PostMapping("/login")
    public Map<String,Object> login(User user, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        if(StringUtil.isEmpty(user.getUserName())){
            map.put("success",false);
            map.put("errorInfo","请输入用户名！");
        }else if(StringUtil.isEmpty(user.getPassword())){
            map.put("success",false);
            map.put("errorInfo","请输入密码！");
        }else{
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(),CryptographyUtil.md5(user.getPassword(),CryptographyUtil.SALT));
            try {
                subject.login(token);       //登录验证
                String userName = (String) SecurityUtils.getSubject().getPrincipal();
                User currentUser = userService.findByUserName(userName);
                if (currentUser.isOff()) {
                    map.put("success", false);
                    map.put("errorInfo", "该用户已封禁，请联系管理员！");
                    subject.logout();
                } else {
                    currentUser.setLatelyLoginTime(new Date());
                    userService.save(currentUser);
                    //未读消息数放到session
                    Integer messageCount = messageService.getCountByUserId(currentUser.getUserId());
                    currentUser.setMessageCount(messageCount);
                    //失效资源数
                    Article s_article = new Article();
                    s_article.setUseful(false);
                    s_article.setUser(currentUser);
                    session.setAttribute(Consts.UN_USEFUL_ARTICLE_COUNT,articleService.getCount(s_article,null,null,null));
                    session.setAttribute(Consts.CURRENT_USER,currentUser);
                    map.put("success", true);
                }
            }catch (Exception e){
                e.printStackTrace();
                map.put("success", false);
                map.put("errorInfo", "用户名或密码错误！");
            }
        }
        return map;
    }

    /**
     * 发送邮件
     */
    @ResponseBody
    @PostMapping("/sendEmail")
    public Map<String,Object> sendEmail(String email, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        if(StringUtil.isEmpty(email)){
            map.put("success",false);
            map.put("errorInfo","邮箱不能为空！");
            return map;
        }
        //验证邮件是否存在
        User u = userService.findByEmail(email);
        if(u==null){
            map.put("success",false);
            map.put("errorInfo","邮箱不存在！");
            return map;
        }
        String mailCode = StringUtil.genSixRandom();
        //发邮件
        SimpleMailMessage message = new SimpleMailMessage();        //消息构造器
        message.setFrom("781505696@qq.com");                        //发件人
        message.setTo(email);                                       //收件人
        message.setSubject("Java资源分享网-用户找回密码");         //主题
        message.setText("您本次的验证码是：" +mailCode);            //正文内容
        mailSender.send(message);
        System.out.println(mailCode);
        //验证码存到session
        session.setAttribute(Consts.MAIL_CODE_NAME,mailCode);
        session.setAttribute(Consts.USER_ID_NAME,u.getUserId());

        map.put("success",true);
        return map;
    }

    /**
     * 邮件验证码判断
     */
    @ResponseBody
    @PostMapping("/checkYzm")
    public Map<String,Object> checkYzm(String yzm, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        if(StringUtil.isEmpty(yzm)){
            map.put("success",false);
            map.put("errorInfo","验证码不能为空！");
            return map;
        }
        String mailCode = (String) session.getAttribute(Consts.MAIL_CODE_NAME);
        Integer userId = (Integer) session.getAttribute(Consts.USER_ID_NAME);

        if(!yzm.equals(mailCode)){
            map.put("success",false);
            map.put("errorInfo","验证码错误！");
            return map;
        }

        //给用户重置密码为123456
        User user = userService.getById(userId);
        user.setPassword((CryptographyUtil.md5(Consts.PASSWORD,CryptographyUtil.SALT)));
        userService.save(user);
        map.put("success",true);
        return map;
    }

    /**
     * 资源管理
     */
    @GetMapping("/articleManage")
    public String articleManage(){
        return "user/articleManage";
    }

    /**
     * 根据条件分页查询资源信息列表（只显示当前登录用户的资源）
     * @param s_article
     * @param page
     * @param pageSize
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/articleList")
    public Map<String,Object> articleList(Article s_article,@RequestParam(value="page",required = false) Integer page,
                                          @RequestParam(value="limit",required = false) Integer pageSize,HttpSession session){
        Map<String,Object> map = new HashMap<>();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        s_article.setUser(currentUser);
        map.put("data",articleService.list(s_article,null,null,null,page,pageSize, Sort.Direction.DESC,"publishDate"));
        map.put("count",articleService.getCount(s_article,null,null,null));     //总计录数
        map.put("code",0);
        return map;
    }

    /**
     * 进入资源发布页面
     */
    @GetMapping("toAddArticle")
    public String toAddArticle(){
        return "user/addArticle";
    }

    /**
     * 添加或修改资源
     */
    @ResponseBody
    @PostMapping("/saveArticle")
    public Map<String,Object> saveArticle(Article article,HttpSession session) throws IOException {
        Map<String,Object> resultMap = new HashMap<>();
        if(article.getPoints()<0||article.getPoints()>10){
            resultMap.put("success",false);
            resultMap.put("erroInfo","积分超出正常区间！");
            return resultMap;
        }
        if(!CheckShareLinkEnableUtil.check(article.getDownload())){
            resultMap.put("success",false);
            resultMap.put("erroInfo","百度云分享链接已经失效，请重新发布！");
            return resultMap;
        }
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if(article.getArticleId()==null){               //添加资源
            article.setPublishDate(new Date());
            article.setUser(currentUser);
            if(article.getPoints()==0){         //积分为0时，设置为免费资源
                article.setFree(true);
            }
            article.setState(1);                //未审核状态
            article.setClick(new Random().nextInt(150)+50);         //设置点击数为50~200
            articleService.save(article);
            resultMap.put("success",true);
        }else{                              //修改资源
            Article oldArticle = articleService.getById(article.getArticleId());        //获取实体
            if(oldArticle.getUser().getUserId().intValue()==currentUser.getUserId().intValue()){        //只能修改自己的资源
                oldArticle.setName(article.getName());
                oldArticle.setArcType(article.getArcType());
                oldArticle.setDownload(article.getDownload());
                oldArticle.setPassword(article.getPassword());
                oldArticle.setKeywords(article.getKeywords());
                oldArticle.setDescription(article.getDescription());
                oldArticle.setContent(article.getContent());
                if(oldArticle.getState()==3){           //假如原先是未通过，用户点击修改，则重新审核，状态变成未审核
                    oldArticle.setState(1);
                }
                articleService.save(oldArticle);
                //更新时需要把审核已经通过的新资源信息放入lucene
                if(oldArticle.getState().intValue()==2){
                    articleIndex.updateIndex(oldArticle);     //更新索引
                }
                resultMap.put("success",true);
            }
        }

        return resultMap;
    }

    /**
     * 验证资源的发布者
     * @param articleId
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkArticleUser")
    public Map<String,Object> checkArticleUser(Integer articleId,HttpSession session){
        Map<String,Object> resultMap = new HashMap<>();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        Article article = articleService.getById(articleId);
        if(article.getUser().getUserId().intValue() == currentUser.getUserId().intValue()){
            resultMap.put("success",true);
        }else{
            resultMap.put("success",false);
            resultMap.put("erroInfo","您不是资源所有者，不能修改！");
        }
        return resultMap;
    }

    /**
     * 进入修改资源页面
     * @param articleId
     * @return
     */
    @GetMapping("/toEditArticle/{articleId}")
    public ModelAndView toEditArticle(@PathVariable(value="articleId",required = true) Integer articleId){
        ModelAndView mav = new ModelAndView();
        Article article = articleService.getById(articleId);
        mav.addObject("article",article);
        mav.setViewName("user/editArticle");
        return mav;
    }

    /**
     * 根据id删除一条资源
     * @param articleId
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/articleDelete")
    public Map<String,Object> articleDelete(Integer articleId,HttpSession session){
        Map<String,Object> resultMap = new HashMap<>();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        Article article = articleService.getById(articleId);
        if(article.getUser().getUserId().intValue() == currentUser.getUserId().intValue()){
            //需要先删除评论
            commentService.deleteByArticleId(articleId);
            articleService.delete(articleId);
            //需要把资源从lucene里面删掉
            articleIndex.deleteIndex(String.valueOf(articleId));
            resultMap.put("success",true);
        }else{
            resultMap.put("success",false);
            resultMap.put("erroInfo","您不是资源所有者，不能删除！");
        }
        return resultMap;
    }

    /**
     * 判断某资源是否被当前用户下载过
     */
    @ResponseBody
    @RequestMapping("/userDownloadExist")
    public boolean userDownloadExist(Integer articleId,HttpSession session){
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        Integer count = userDownloadService.getCountByUserIdAndArticleId(currentUser.getUserId(),articleId);
        if(count>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断用户积分是否足够下载本资源
     */
    @ResponseBody
    @RequestMapping("/userDownloadEnough")
    public boolean userDownloadEnough(Integer points,HttpSession session){
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if(currentUser.getPoints()>points){         //积分足够
            return true;
        }else{                                   // 积分不够
            return false;
        }
    }

    /**
     * 跳转到资源下载页面
     */
    @RequestMapping("/toDownloadPage/{articleId}")
    public ModelAndView toDownloadPage(@PathVariable("articleId")Integer articleId,HttpSession session){
        Article article = articleService.getById(articleId);
        //查不到或者没审核通过，直接返回
        if(article==null||article.getState().intValue()!=2){
            return null;
        }
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        //判断当前用户是否下载过
        int count = userDownloadService.getCountByUserIdAndArticleId(currentUser.getUserId(),articleId);
        if(count==0){           //没下载过
            if(!article.isFree()){      //非免费资源
                if(currentUser.getPoints()-article.getPoints()<0){      //积分不够
                    return null;
                }
                //扣除积分后保存到数据库
                currentUser.setPoints(currentUser.getPoints()-article.getPoints());
                userService.save(currentUser);
                //资源分享人加积分
                User articleUser=article.getUser();
                article.setPoints(articleUser.getPoints()+article.getPoints());
                userService.save(articleUser);
            }

            //保存用户下载
            UserDownload userDownload = new UserDownload();
            userDownload.setArticle(article);
            userDownload.setUser(currentUser);
            userDownload.setDownloadDate(new Date());
            userDownloadService.save(userDownload);
        }
        ModelAndView mav = new ModelAndView();
        mav.addObject("article",article);
        mav.setViewName("article/downloadPage");
        return mav;
    }

    /**
     * vip跳转到资源下载页面
     */
    @RequestMapping("/toVipDownloadPage/{articleId}")
    public ModelAndView toVipDownloadPage(@PathVariable("articleId")Integer articleId,HttpSession session){
        Article article = articleService.getById(articleId);
        //查不到或者没审核通过，直接返回
        if(article==null||article.getState().intValue()!=2){
            return null;
        }
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if(!currentUser.isVip()){
            return null;
        }
        //判断当前用户是否下载过
        int count = userDownloadService.getCountByUserIdAndArticleId(currentUser.getUserId(),articleId);
        if(count==0){           //没下载过
            //保存用户下载
            UserDownload userDownload = new UserDownload();
            userDownload.setArticle(article);
            userDownload.setUser(currentUser);
            userDownload.setDownloadDate(new Date());
            userDownloadService.save(userDownload);
        }
        ModelAndView mav = new ModelAndView();
        mav.addObject("article",article);
        mav.setViewName("article/downloadPage");
        return mav;
    }

    /**
     * 获取当前用户是否vip
     */
    @ResponseBody
    @PostMapping("/isVip")
    public boolean isVip(HttpSession session){
        User user = (User)session.getAttribute(Consts.CURRENT_USER);
        return user.isVip();
    }

    /**
     * 进入失效资源页面
     */
    @GetMapping("/toUnUsefulArticleManage")
    public String toUnUsefulArticleManage(HttpSession session){
        this.unUsefulArticleCount(session);
        return "user/unUsefulArticleManage";
    }

    /**
     * 用户失效资源数
     */
    private void unUsefulArticleCount(HttpSession session){
        User user = (User)session.getAttribute(Consts.CURRENT_USER);
        Article s_article = new Article();
        s_article.setUseful(false);
        s_article.setUser(user);
        session.setAttribute(Consts.UN_USEFUL_ARTICLE_COUNT,articleService.getCount(s_article,null,null,null));
    }

    /**
     * 修改百度云分享链接
     */
    @ResponseBody
    @RequestMapping("/modifyArticleShareLink")
    public Map<String,Object> modifyArticleShareLink(Article article,HttpSession session) throws IOException{
        Map<String,Object> resultMap = new HashMap<>();
        if(CheckShareLinkEnableUtil.check(article.getDownload())){
            Article oldArticle = articleService.getById(article.getArticleId());
            User user = (User)session.getAttribute(Consts.CURRENT_USER);
            if(oldArticle.getUser().getUserId().intValue() == user.getUserId().intValue()){
                oldArticle.setDownload(article.getDownload());
                oldArticle.setPassword(article.getPassword());
                oldArticle.setUseful(true);
                articleService.save(oldArticle);
                resultMap.put("success",true);
                this.unUsefulArticleCount(session);
            }else{
                resultMap.put("success",false);
                resultMap.put("errorInfo","你不是该资源的所有者！");
            }
        }else {
            resultMap.put("success",false);
            resultMap.put("errorInfo","百度云分享链接已经失效，请重新发布！");
        }
        return resultMap;
    }

    /**
     * 进入已下载资源页面
     */
    @GetMapping("/toHaveDownloaded/{currentPage}")
    public ModelAndView toHaveDownloaded(@PathVariable(value = "currentPage",required = false)Integer currentPage, HttpSession session){
        this.unUsefulArticleCount(session);
        ModelAndView mav = new ModelAndView();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        //已下载资源的列表
        Page<UserDownload> page = userDownloadService.list(currentUser.getUserId(),currentPage,Consts.PAGE_SIZE, Sort.Direction.DESC,"downloadDate");
        mav.addObject("userDownloadList",page.getContent());
        //分页html代码
        mav.addObject("pageStr", HTMLUtil.getPagation("/user/toHaveDownloaded",page.getTotalPages(),currentPage,"没有下载任何资源。。。"));
        mav.setViewName("user/haveDownloaded");
        return mav;
    }

    /**
     * 我的消息
     */
    @GetMapping("/userMessage/{currentPage}")
    public ModelAndView userMessage(@PathVariable(value = "currentPage",required = false)Integer currentPage, HttpSession session){
        this.unUsefulArticleCount(session);
        ModelAndView mav = new ModelAndView();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        //进入页面就认为用户阅读了所有消息
        if(currentUser.getMessageCount()==null||currentUser.getMessageCount()>0){
            messageService.updateState(currentUser.getUserId());
            currentUser.setMessageCount(0);                 //设置当前用户未读消息数为0
        }
        session.setAttribute(Consts.CURRENT_USER,currentUser);
        //所有当前用户的消息列表
        Page<Message> page = messageService.list(currentUser.getUserId(),currentPage,Consts.PAGE_SIZE, Sort.Direction.DESC,"publishDate");
        mav.addObject("messageList",page.getContent());
        //分页html代码
        mav.addObject("pageStr", HTMLUtil.getPagation("/user/userMessage",page.getTotalPages(),currentPage,"没有任何系统消息。。。"));
        mav.setViewName("user/userMessage");
        return mav;
    }

    /**
     * 我的主页
     */
    @GetMapping("/home")
    public ModelAndView home(HttpSession session){
        ModelAndView mav = new ModelAndView();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        Page<UserDownload> userDownloadPage = userDownloadService.list(currentUser.getUserId(),1,Consts.PAGE_SIZE, Sort.Direction.DESC,"downloadDate");
        if(userDownloadPage.getTotalElements()>0){
            mav.addObject("userDownloadList",userDownloadPage.getContent());                //用户最近下载的n条资源帖子
        }

        Page<Message> messagePage = messageService.list(currentUser.getUserId(),1,Consts.PAGE_SIZE, Sort.Direction.DESC,"publishDate");
        if(messagePage.getTotalElements()>0){
            mav.addObject("messageList",messagePage.getContent());                          //用户最近的n条系统消息
        }
        mav.setViewName("user/home");
        return mav;
    }

    /**
     * 上传头像
     */
    @ResponseBody
    @RequestMapping("/updateHeadPortrait")
    public Map<String,Object> updateHeadPortrait(MultipartFile file,HttpSession session) throws Exception {
        Map<String,Object> map = new HashMap<>();
        if(!file.isEmpty()){
            //获取文件名
            String fileName = file.getOriginalFilename();
            //获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            //新文件名
            String newFileName = DateUtil.getCurrentDateStr()+suffixName;
            FileUtils.copyInputStreamToFile(file.getInputStream(),new File(imgFilePath+newFileName));
            map.put("success",true);
            map.put("imgName",newFileName);
            //把头像放到session和数据库
            User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
            currentUser.setHeadPortrait(newFileName);
            userService.save(currentUser);
            session.setAttribute(Consts.CURRENT_USER,currentUser);
        }
        return map;
    }

    /**
     * 用户中心
     */
    @GetMapping("/userCenter")
    public ModelAndView userCenter(HttpSession session){
        ModelAndView mav = new ModelAndView();
        this.unUsefulArticleCount(session);
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        mav.addObject("currentUser",currentUser);
        mav.setViewName("user/userCenter");
        return mav;
    }

    /**
     * 修改基本信息
     */
    @ResponseBody
    @RequestMapping("/userUpdate")
    private Map<String,Object> userUpdate(User user,HttpSession session){
        Map<String,Object> resultMap = new HashMap<>();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        currentUser.setNickname(user.getNickname());
        currentUser.setSex(user.getSex());
        userService.save(currentUser);
        session.setAttribute(Consts.CURRENT_USER,currentUser);
        resultMap.put("success",true);
        return resultMap;
    }

    /**
     * 修改密码
     */
    @ResponseBody
    @PostMapping("/updatePassword")
    public Map<String,Object> updatePassword(String oldPassword,String newPassword,HttpSession session){
        Map<String,Object> resultMap = new HashMap<>();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if(currentUser.getPassword().equals(CryptographyUtil.md5(oldPassword,CryptographyUtil.SALT))){
            User oldUser = userService.getById(currentUser.getUserId());
            oldUser.setPassword(CryptographyUtil.md5(newPassword,CryptographyUtil.SALT));
            userService.save(oldUser);
            session.setAttribute(Consts.CURRENT_USER,oldUser);
            resultMap.put("success",true);
        }else{
            resultMap.put("errorInfo","原密码错误！");
        }
        return resultMap;
    }

    /**
     * 绑定邮箱（第一次使用QQ登录）
     */
    @ResponseBody
    @RequestMapping("/bindEmail")
    public Map<String,Object> bindEmail(String userName,String email,HttpSession session){
        Map<String,Object> resultMap = new HashMap<>();
        if(StringUtil.isEmpty(userName)){
            resultMap.put("success",false);
            resultMap.put("errorInfo","用户名不能为空！");
            return resultMap;
        }
        if(StringUtil.isEmpty(email)){
            resultMap.put("success",false);
            resultMap.put("errorInfo","邮箱不能为空！");
            return resultMap;
        }
        if(userService.findByUserName(userName)!=null){
            resultMap.put("success",false);
            resultMap.put("errorInfo","用户名已存在，请更换！");
            return resultMap;
        }
        if(userService.findByEmail(email)!=null){
            resultMap.put("success",false);
            resultMap.put("errorInfo","邮箱已存在，请更换！");
            return resultMap;
        }
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if(StringUtil.isNotEmpty(currentUser.getUserName())||StringUtil.isNotEmpty(currentUser.getEmail())){
            resultMap.put("success",false);
            resultMap.put("errorInfo","非法请求！");
            return resultMap;
        }
        currentUser.setUserName(userName);
        currentUser.setEmail(email);
        userService.save(currentUser);
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(currentUser.getUserName(),currentUser.getPassword());
        subject.login(token);               //登录验证
        Integer messageCount = messageService.getCountByUserId(currentUser.getUserId());
        currentUser.setMessageCount(messageCount);
        Article s_article = new Article();
        s_article.setUseful(false);
        s_article.setUser(currentUser);
        session.setAttribute(Consts.UN_USEFUL_ARTICLE_COUNT,articleService.getCount(s_article,null,null,null));
        session.setAttribute(Consts.CURRENT_USER,currentUser);
        resultMap.put("success",true);
        return  resultMap;
    }

}

















