package com.dahaiwuliang.code.service.impl;

import com.dahaiwuliang.code.entity.Message;
import com.dahaiwuliang.code.repository.MessageRepository;
import com.dahaiwuliang.code.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * 用户消息Service实现类
 */
@Service("messageService")
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return messageRepository.findAll(new Specification<Message>() {
            @Override
            public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
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
        return messageRepository.count(new Specification<Message>() {
            @Override
            public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        });
    }

    @Override
    public void save(Message message) {
        messageRepository.save(message);
    }

    @Override
    public Integer getCountByUserId(Integer userId) {
        return messageRepository.getCountByUserId(userId);
    }

    @Override
    public void updateState(Integer userId) {
        messageRepository.updateState(userId);
    }
}
