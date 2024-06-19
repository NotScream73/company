package ru.example.company.news.service;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.example.company.news.model.News;
import ru.example.company.news.model.dto.NewsFilterDto;
import ru.example.company.user.model.UserNews;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class NewsSpecification implements Specification<News> {
    private final NewsFilterDto filter;

    @Override
    public Predicate toPredicate(Root<News> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder cb) {
        // Create main query
        CriteriaBuilder criteriaBuilder = cb;
        CriteriaQuery<?> criteriaQuery = query;

        // Subquery
        Subquery<UUID> subquery = criteriaQuery.subquery(UUID.class);
        Root<UserNews> userNewsRoot = subquery.from(UserNews.class);

        subquery.select(userNewsRoot.get("id").get("newsId"));
        subquery.where(
                cb.or(
                        cb.isTrue(userNewsRoot.get("isHidden")),
                        cb.isNull(userNewsRoot.get("isHidden"))
                ),
                cb.equal(userNewsRoot.get("user").get("id"), filter.getUserId())
        );

        // Main query predicate
        List<Predicate> predicatesList = new ArrayList<>();

        if (filter.getTitle() != null && filter.getTitle().length() > 4)
            predicatesList.add(cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%" ));

        if (filter.getCategory() != null)
            predicatesList.add(cb.equal(root.get("category"), filter.getCategory()));

        if (filter.getIsArchived() == null || !filter.getIsArchived()){
            predicatesList.add(cb.equal(root.get("isArchived"), false));
        }else{
            predicatesList.add(cb.equal(root.get("isArchived"), true));
        }

        if (filter.getCreatedAtFrom() != null)
            predicatesList.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));

        if (filter.getCreatedAtTo() != null)
            predicatesList.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));

        if (filter.getExpiresAt() != null)
            predicatesList.add(cb.greaterThanOrEqualTo(root.get("expiresAt"), filter.getExpiresAt()));

        // Exclude news IDs from subquery
        predicatesList.add(cb.not(root.get("id").in(subquery)));

        // Set distinct
        criteriaQuery.distinct(true);

        return cb.and(predicatesList.toArray(new Predicate[0]));
    }
}
