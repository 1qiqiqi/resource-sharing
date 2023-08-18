package com.dahaiwuliang.code.service;

import com.dahaiwuliang.code.entity.User;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 用户Service接口
 */
public interface UserService {
    /**
     * 根据用户名查找用户实体
     */
    public User findByUserName(String userName);

    /**
     * 根据邮箱查找用户实体
     */
    public User findByEmail(String email);

    /**
     * 添加或修改用户信息
     */
    public void save(User user);

    /**
     * 根据id获取用户信息
     */
    public User getById(Integer id);

    /**
     * 根据条件获取用户总数
     * @param s_user                条件
     * @param s_blatelyLoginTime    最近登录时间开始
     * @param s_elatelyLoginTime    最近登陆时间结束
     * @return
     */
    public Long getCount(User s_user,String s_blatelyLoginTime,String s_elatelyLoginTime);

    /**
     * 今日注册用户数
     */
    public Integer todayRegister();

    /**
     * 今日登录用户数
     */
    public Integer todayLogin();

    /**
     * 根据条件查询用户列表
     * @param s_user                条件
     * @param s_blatelyLoginTime    最近登录时间开始
     * @param s_elatelyLoginTime    最近登陆时间结束
     * @param page                  当前页
     * @param pageSIze              每页记录数
     * @param direction             排序规则
     * @param properties            排序字段
     * @return
     */
    public List<User> list(User s_user, String s_blatelyLoginTime, String s_elatelyLoginTime, Integer page, Integer pageSIze, Sort.Direction direction,String... properties);

    /**
     * 根据open_id查找用户
     */
    public User findByOpenId(String openId);

    /**
     * 解除QQ绑定
     */
    public void removeBind(Integer userId);
}
