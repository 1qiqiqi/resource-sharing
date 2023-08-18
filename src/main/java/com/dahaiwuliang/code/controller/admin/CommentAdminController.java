package com.dahaiwuliang.code.controller.admin;

import com.dahaiwuliang.code.entity.Comment;
import com.dahaiwuliang.code.service.CommentService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-评论控制器
 */
@RestController
@RequestMapping(value = "/admin/comment")
public class CommentAdminController {

    @Autowired
    private CommentService commentService;

    /**
     * 根据条件分页查询评论信息
     */
    @RequestMapping(value = "/list")
    @RequiresPermissions(value = "分页查询评论信息")
    public Map<String,Object> list(Comment s_comment, @RequestParam(value = "page",required = false)Integer page,
                                   @RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("data",commentService.list(s_comment,page,pageSize, Sort.Direction.DESC,"commentDate").getContent());
        resultMap.put("total",commentService.getTotal(s_comment));
        resultMap.put("errorNo",0);
        return resultMap;
    }

    /**
     * 修改评论状态
     */
    @RequestMapping(value = "/updateState")
    @RequiresPermissions(value = "修改评论状态")
    public Map<String,Object> updateState(Integer commentId,boolean state){
        Comment comment = commentService.get(commentId);
        if(state){      //审核通过
            comment.setState(1);
        }else {         //审核不通过
            comment.setState(2);
        }
        commentService.save(comment);
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("success",true);
        return resultMap;
    }

    /**
     * 删除评论
     */
    @RequestMapping(value = "/delete")
    @RequiresPermissions(value = "删除评论")
    public Map<String,Object> delete(@RequestParam(value = "commentId") String ids){
        Map<String,Object> resultMap = new HashMap<>();
        String[] idsStr = ids.split(",");
        for(int i=0;i<idsStr.length;i++){
            commentService.delete(Integer.parseInt(idsStr[i]));
        }
        resultMap.put("errorNo",0);
        return resultMap;
    }

}
