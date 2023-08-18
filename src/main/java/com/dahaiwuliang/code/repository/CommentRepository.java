package com.dahaiwuliang.code.repository;

import com.dahaiwuliang.code.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 评论Repository接口
 */
public interface CommentRepository extends JpaRepository<Comment,Integer>, JpaSpecificationExecutor<Comment> {

    /**
     * 删除指定资源下的所有评论
     */
    @Query(value = "delete from comment where article_id=?1",nativeQuery = true)
    @Modifying
    public void deleteByArticleId(Integer articleId);
}
