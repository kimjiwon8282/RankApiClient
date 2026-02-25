package com.example.RankCat.repository;

import com.example.RankCat.model.ShoppingInsightCategoryResult;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingInsightCategoryRepository
        extends MongoRepository<ShoppingInsightCategoryResult, String> {
    Optional<ShoppingInsightCategoryResult> findByCategoryName(String categoryName);
}
