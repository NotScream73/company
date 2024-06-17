package ru.example.company.news.service;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.example.company.news.model.News;
import ru.example.company.news.model.dto.NewsFilterDto;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class NewsSpecification implements Specification<News> {
    private final NewsFilterDto filter;

    @Override
    public Predicate toPredicate(Root<News> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder cb) {
        List<Predicate> predicatesList = new ArrayList<>();

        if (filter.getTitle() != null && filter.getTitle().length() > 4)
            predicatesList.add(cb.like(cb.lower(root.get("title")), "%" + filter.getTitle() + "%" ));

        if (filter.getCategory() != null)
            predicatesList.add(cb.equal(root.get("category"), filter.getCategory()));

        if (filter.getIsArchived() != null)
            predicatesList.add(cb.equal(root.get("isArchived"), filter.getIsArchived()));

        if (filter.getCreatedAtFrom() != null)
            predicatesList.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));

        if (filter.getCreatedAtTo() != null)
            predicatesList.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));

        if (filter.getExpiresAt() != null)
            predicatesList.add(cb.greaterThanOrEqualTo(root.get("expiresAt"), filter.getExpiresAt()));

        return cb.and(predicatesList.toArray(new Predicate[0]));
    }
}
