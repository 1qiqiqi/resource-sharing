package com.dahaiwuliang.code.service;

import com.dahaiwuliang.code.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 评论Service
 */
public interface CommentService {
    /**
     * 保存评论
     * @param comment
     */
    public void save(Comment comment);
    /**
     * 根据条件分页查询评论信息
     */
    public Page<Comment> list(Comment s_comment, Integer page, Integer pageSize, Sort.Direction direction,String... properties);

    /**
     * 根据条件获取总计录数
     */
    public Long getTotal(Comment s_comment);

    /**
     * 根据id获取评论对象
     */
    public Comment get(Integer id);

    /**
     * 删除一条评论
     */
    public void delete(Integer id);

    /**
     * 删除指定资源下的所有评论
     */
    public void deleteByArticleId(Integer articleId);

}
