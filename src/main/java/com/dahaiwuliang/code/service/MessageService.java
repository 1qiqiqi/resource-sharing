package com.dahaiwuliang.code.service;

import com.dahaiwuliang.code.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 用户消息Service接口
 */
public interface MessageService {

    /**
     * 根据条件分页查询消息列表
     * @param userId            用户id
     * @param page              当前页
     * @param pageSize          每页记录数
     * @param direction         排序规则
     * @param properties        排序字段
     * @return
     */
    public Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction,String... properties);

    /**
     * 根据条件获取总记录数
     * @param userId            用户id
     * @return
     */
    public Long getCount(Integer userId);

    /**
     * 添加或修改消息
     */
    public void save(Message message);

    /**
     * 查询某个用户下的所有消息总数（未查看）
     */
    public Integer getCountByUserId(Integer userId);

    /**
     * 看所有消息
     */
    public void updateState(Integer userId);
}
