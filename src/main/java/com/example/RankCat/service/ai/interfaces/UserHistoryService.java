package com.example.RankCat.service.ai.interfaces;


import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;

public interface UserHistoryService {
    UserHistory save(User user, SaveHistoryRequest req);
    public UserHistoryResponse getUserHistories(User user);
}
