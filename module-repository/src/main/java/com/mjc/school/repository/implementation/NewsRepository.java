package com.mjc.school.repository.implementation;

import com.mjc.school.repository.NewsRepInterface;
import com.mjc.school.repository.model.impl.AuthorModel;
import com.mjc.school.repository.model.impl.CommentModel;
import com.mjc.school.repository.model.impl.NewsModel;
import com.mjc.school.repository.model.impl.TagModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class NewsRepository implements NewsRepInterface {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public NewsRepository() {

    }

    @Override
    public List<NewsModel> readAll(Integer page, Integer limit, String sortBy) {
        String request = "select a from NewsModel a";
        if(sortBy!=null){
            request +=" order by " +sortBy;
        }
        return entityManager.createQuery(request, NewsModel.class).setFirstResult(page-1).setMaxResults((page-1)*limit).getResultList();
    }

    @Override
    public Optional<NewsModel> readById(Long id) {
        return Optional.ofNullable(entityManager.find(NewsModel.class, id));
    }

    @Override
    public NewsModel create(NewsModel entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public NewsModel update(NewsModel entity) {
        entityManager.merge(entity);
        return entity;
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            entityManager.remove(entityManager.find(NewsModel.class, id));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public List<NewsModel> readNewsByParams(Optional<List<Long>> tagsIds,Optional<List<String>>tagsNames,Optional<String> authorName, Optional<String> title,Optional<String> content){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NewsModel> criteriaQuery = criteriaBuilder.createQuery(NewsModel.class);
        Root<NewsModel> root = criteriaQuery.from(NewsModel.class);
        List<Predicate> pr = new ArrayList<>();
        if(tagsIds.isPresent()){
            Join<TagModel,NewsModel> nt= root.join("tags");
            pr.add(nt.get("id").in(tagsIds.get()));
        }
        if(tagsNames.isPresent()){
            Join<TagModel,NewsModel> nt = root.join("tags");
            pr.add(nt.get("name").in(tagsNames.get()));
        }
        if(authorName.isPresent()){
            Join<AuthorModel,NewsModel> na = root.join("authors");
            pr.add(na.get("name").in("authors"));
        }
        if(title.isPresent()){
            pr.add(criteriaBuilder.like(root.get("title"),"%"+title.get()+"%"));
        }
        if (content.isPresent()){
            pr.add(criteriaBuilder.like(root.get("content"),"%"+content.get()+"%"));
        }
        criteriaQuery.select(root).distinct(true).where(pr.toArray(new Predicate[0]));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public boolean existById(Long id) {
        return readById(id).isPresent();
    }
}
