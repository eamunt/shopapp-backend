package com.project.shopapp.services.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.responses.user.UserListResponse;
import org.springframework.data.domain.PageRequest;

public interface IUserRedisService {

    void clear();
    UserListResponse getAllUser(
            String keyword,
            PageRequest pageRequest
    ) throws JsonProcessingException;
    void saveAllUser(UserListResponse productListResponse,
                         String keyword,
                         PageRequest pageRequest) throws JsonProcessingException;
}
