package com.dahaiwuliang.code.service.impl;

import com.dahaiwuliang.code.entity.UserDownload;
import com.dahaiwuliang.code.repository.UserDownRepository;
import com.dahaiwuliang.code.service.UserDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * 用户下载Service实现类
 */
@Service("userDownloadService")
public class UserDownLoadServiceImpl implements UserDownloadService {

    @Autowired
    private UserDownRepository userDownRepository;

    @Override
    public Integer getCountByUserIdAndArticleId(Integer userId, Integer articleId) {
        return userDownRepository.getCountByUserIdAndArticleId(userId,articleId);
    }

    @Override
    public Page<UserDownload> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return userDownRepository.findAll(new Specification<UserDownload>() {
            @Override
            public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
    }

    @Override
    public Long getCount(Integer userId) {
        return userDownRepository.count(new Specification<UserDownload>() {
            @Override
            public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        });
    }

    @Override
    public void save(UserDownload userDownload) {
        userDownRepository.save(userDownload);
    }
}
