package com.dahaiwuliang.code.repository;

import com.dahaiwuliang.code.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 友情链接Repository接口
 */
public interface LinkRepository extends JpaRepository<Link,Integer>, JpaSpecificationExecutor<Link> {
}
