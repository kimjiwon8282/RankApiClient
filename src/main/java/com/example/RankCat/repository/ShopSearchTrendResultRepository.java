package com.example.RankCat.repository;

import com.example.RankCat.model.ShopSearchTrendResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopSearchTrendResultRepository extends MongoRepository<ShopSearchTrendResult, String> {
    // query == id가 PK!
}
