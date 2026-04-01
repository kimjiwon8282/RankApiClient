package com.example.RankCat.repository;

import com.example.RankCat.model.CollectorJobExecution;
import com.example.RankCat.model.CollectorJobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectorJobExecutionRepository
        extends MongoRepository<CollectorJobExecution, String> {
    List<CollectorJobExecution> findAllByOrderByStartedAtDesc(Pageable pageable);

    Optional<CollectorJobExecution> findFirstByJobNameOrderByStartedAtDesc(String jobName);

    Optional<CollectorJobExecution> findFirstByJobNameAndStatusOrderByFinishedAtDesc(
            String jobName, CollectorJobStatus status);
}
