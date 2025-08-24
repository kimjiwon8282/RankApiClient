package com.example.RankCat.config.schedule;

import com.mongodb.client.MongoDatabase;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M") // 기본 최대 락 시간(개별 메서드에서 override 가능)
public class ShedLockConfig {
    @Bean
    public LockProvider lockProvider(MongoTemplate mongoTemplate) {
        MongoDatabase db = mongoTemplate.getDb();
        return new MongoLockProvider(db); // 컬렉션명: shedLock (기본)
    }
}
