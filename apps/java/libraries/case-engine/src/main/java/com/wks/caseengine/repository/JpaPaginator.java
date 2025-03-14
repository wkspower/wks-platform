package com.wks.caseengine.repository;

import org.springframework.stereotype.Component;

import jakarta.persistence.TypedQuery;

@Component
public class JpaPaginator {

    private int page = 0;
    private int offset = 5;

    public <T> TypedQuery<T> apply(TypedQuery<T> query) {
        return query.setFirstResult(page > 0 ? ((page - 1) * offset) : 0).setMaxResults(offset);
    }
}