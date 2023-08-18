package com.dahaiwuliang.code.controller;

import com.dahaiwuliang.code.entity.Comment;
import com.dahaiwuliang.code.entity.User;
import com.dahaiwuliang.code.service.CommentService;
import com.dahaiwuliang.code.util.Consts;
import com.dahaiwuliang.code.util.HTMLUtil;
import com.dahaiwuliang.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论Controller
 */
@Controller
@RequestMapping(value = "/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 前端提交保存评论信息
     * @param comment
     * @param session
     * @return
     */
    @ResponseBody
    @PostMapping("/add")
    public Map<String,Object> add(Comment comment, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        comment.setContent(StringUtil.esc(comment.getContent()));
        comment.setCommentDate(new Date());
        comment.setState(0);
        comment.setUser((User)session.getAttribute(Consts.CURRENT_USER));
        commentService.save(comment);
        map.put("success",true);
        return map;
    }

    /**
     * 分页查询某个资源的评论信息
     */
    @ResponseBody
    @RequestMapping(value = "/list")
    public Map<String,Object> list(Comment s_comment, @RequestParam(value = "page",required = false)Integer page){
        s_comment.setState(1);
        Page<Comment> commentPage = commentService.list(s_comment,page,5, Sort.Direction.DESC,"commentDate");
        Map<String,Object> map = new HashMap<>();
        map.put("data", HTMLUtil.getCommentPageStr(commentPage.getContent()));          //评论的HTML代码
        map.put("total",commentPage.getTotalPages());                                   //总页数
        return map;
    }
}
