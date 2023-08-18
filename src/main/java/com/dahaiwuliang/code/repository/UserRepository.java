package com.dahaiwuliang.code.repository;

import com.dahaiwuliang.code.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

/**
 * 用户Repository接口
 */
public interface UserRepository extends JpaRepository<User,Integer>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户实体
     */
    @Query(value="select * from user where user_name=?1",nativeQuery = true)
    public User findByUserName(String userName);

    /**
     * 根据邮箱查找用户实体
     */
    @Query(value="select * from user where email=?1",nativeQuery = true)
    public User findByEmail(String email);

    /**
     * 今日注册用户数
     */
    @Query(value="select count(*) from user where TO_DAYS(registration_date) = TO_DAYS(NOW());",nativeQuery = true)
    public Integer todayRegister();

    /**
     * 今日登录用户数
     */
    @Query(value="select count(*) from user where TO_DAYS(lately_login_time) = TO_DAYS(NOW());",nativeQuery = true)
    public Integer todayLogin();

    /**
     * 根据open_id查找用户
     */
    @Query(value = "select * from user where open_id=?1 limit 1",nativeQuery = true)
    public User findByOpenId(String openId);

    /**
     * 解除QQ绑定
     */
    @Transactional
    @Query(value="update user set open_id=NULL where user_id=?1",nativeQuery = true)
    @Modifying
    public void removeBind(Integer userId);
}
