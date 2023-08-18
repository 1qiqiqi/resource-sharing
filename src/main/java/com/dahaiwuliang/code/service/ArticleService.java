package com.dahaiwuliang.code.service;

import com.dahaiwuliang.code.entity.Article;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.Map;

public interface ArticleService {
    /**
     * 根据分页条件查询资源信息列表
     * @param s_article 条件
     * @param nickname  用户昵称
     * @param s_bpublishDate    发布开始时间
     * @param s_epublishDate    发布结束时间
     * @param page              当前页
     * @param pageSize          每页记录数
     * @param direction         排序规则
     * @param properties        排序字段
     * @return
     */
    public List<Article> list(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate, Integer page, Integer pageSize, Direction direction, String... properties);

    /**
     * 根据条件获取总记录数
     * @param s_article 条件
     * @param nickname  用户昵称
     * @param s_bpublishDate    发布开始时间
     * @param s_epublishDate    发布结束时间
     * @return
     */
    public Long getCount(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate);

    /**
     * 增加或修改资源
     * @param article   资源实体
     */
    public void save(Article article);

    /**
     * 根据主键id删除资源
     * @param id    资源id
     */
    public void delete(Integer id);

    /**
     * 根据主键id获取资源信息
     * @param id   资源id
     * @return
     */
    public Article getById(Integer id);

    /**
     * 根据条件分页查询资源信息（前台）
     * @param type      类型id
     * @param page      当前页
     * @param pageSize  每页记录数
     * @return
     */
    public Map<String,Object> list(String type,Integer page,Integer pageSize);

    /**
     * 查询所有审核通过的资源信息列表
     */
    public List<Article> listStatePass();

    /**
     * 点击+1
     */
    public void updateClick(Integer articleId);

    /**
     * 今日发布资源总数
     */
    public Integer todayPublish();

    /**
     * 未审核资源总数
     */
    public Integer noAudit();

    //10条最新资源
    public List<Article> getNewArticle(Integer n);

    //10条热门资源(按点击排序)
    public List<Article> getClickArticle(Integer n);

    //10条随机资源（热搜推荐）
    public List<Article> getRandomArticle(Integer n);
}
