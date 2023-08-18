package com.dahaiwuliang.code.service;

import com.dahaiwuliang.code.entity.UserDownload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 用户下载Service接口
 */
public interface UserDownloadService {

    /**
     * 查询某个用户下载某个资源的次数
     */
    public Integer getCountByUserIdAndArticleId(Integer userId,Integer articleId);

    /**
     * 分页查询某个用户下载的所有资源
     */
    public Page<UserDownload> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction,String... properties);

    /**
     * 统计某个用户下载的资源数
     */
    public Long getCount(Integer userId);

    /**
     * 添加或修改某用户的下载信息
     */
    public void save(UserDownload userDownload);
}
