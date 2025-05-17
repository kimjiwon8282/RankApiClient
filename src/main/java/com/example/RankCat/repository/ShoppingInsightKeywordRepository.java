package com.example.RankCat.repository;

import com.example.RankCat.model.ShoppingInsightKeywordResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingInsightKeywordRepository extends MongoRepository<ShoppingInsightKeywordResult, String> { }