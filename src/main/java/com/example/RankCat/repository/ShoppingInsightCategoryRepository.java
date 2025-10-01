package com.example.RankCat.repository;

import com.example.RankCat.model.ShoppingInsightCategoryResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingInsightCategoryRepository extends MongoRepository<ShoppingInsightCategoryResult, String> {
    Optional<ShoppingInsightCategoryResult> findByCategoryName(String categoryName);
}