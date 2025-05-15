package com.example.RankCat.repository;

import com.example.RankCat.model.SearchAdKeywordResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchAdKeywordRepository extends MongoRepository<SearchAdKeywordResult, String> {
}
