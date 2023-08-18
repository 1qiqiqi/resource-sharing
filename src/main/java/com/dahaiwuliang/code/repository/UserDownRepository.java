package com.dahaiwuliang.code.repository;

import com.dahaiwuliang.code.entity.Article;
import com.dahaiwuliang.code.entity.UserDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 用户下载repository接口
 */
public interface UserDownRepository extends JpaRepository<UserDownload,Integer>, JpaSpecificationExecutor<UserDownload> {

    /**
     * 查询某个用户下载某个资源的次数
     */
    @Query(value = "select count(*) from user_download where user_id=?1 and article_id=?2",nativeQuery = true)
    public Integer getCountByUserIdAndArticleId(Integer userId,Integer articleId);
}
