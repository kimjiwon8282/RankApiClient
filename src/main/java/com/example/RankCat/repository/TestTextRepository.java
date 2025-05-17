package com.example.RankCat.repository;

import com.example.RankCat.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestTextRepository extends JpaRepository<TestEntity, Long> {
}
