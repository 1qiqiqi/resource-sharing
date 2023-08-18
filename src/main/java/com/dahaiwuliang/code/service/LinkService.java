package com.dahaiwuliang.code.service;

import com.dahaiwuliang.code.entity.Link;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 友情链接Service接口
 */
public interface LinkService {

    /**
     * 分页查询友情链接
     */
    public List<Link> list(Integer page, Integer pageSize, Sort.Direction direction,String... properties);

    /**
     * 获取总计录数
     */
    public Long getCount();

    /**
     * 添加或修改友情链接
     */
    public void save(Link link);

    /**
     * 根据id删除友情链接
     */
    public void delete(Integer id);

    /**
     * 查询所有友情链接
     */
    public List<Link> listAll(Sort.Direction direction, String... properties);

    /**
     * 根据id获取友情链接实体
     */
    public Link getById(Integer id);
}
