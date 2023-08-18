package com.dahaiwuliang.code.repository;

import com.dahaiwuliang.code.entity.ArcType;
import com.dahaiwuliang.code.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 资源类型Repository
 */
public interface ArcTypeRepository extends JpaRepository<ArcType,Integer>, JpaSpecificationExecutor<ArcType> {
}
