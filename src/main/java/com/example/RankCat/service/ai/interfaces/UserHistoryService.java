package com.example.RankCat.service.ai.interfaces;


import com.example.RankCat.dto.SaveHistoryRequest;
import com.example.RankCat.dto.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;

import java.util.List;

public interface UserHistoryService {
    UserHistory save(User user, SaveHistoryRequest req);
    public UserHistoryResponse getUserHistories(User user);
}
