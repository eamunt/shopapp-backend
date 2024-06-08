package com.project.shopapp.services.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.UserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserRedisService implements IUserRedisService{
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    @Value("${spring.data.redis.use-redis-cache}")
    private boolean useRedisCache;
    private String getKeyFrom(String keyword,
                              PageRequest pageRequest){
        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        Sort sort = pageRequest.getSort();
        String sortDirection = Objects.requireNonNull(sort.getOrderFor("id"))
                .getDirection() == Sort.Direction.ASC ? "asc" : "desc";
        String key = String.format("all_users:%s:%d:%d:%s",
                keyword, pageNumber, pageSize, sortDirection);
        return key;
    }


    @Override
    public UserListResponse getAllUser(String keyword, PageRequest pageRequest) throws JsonProcessingException {
        if(useRedisCache == false){
            return null;
        }
        String key = this.getKeyFrom(keyword, pageRequest);
        String json = (String)redisTemplate.opsForValue().get(key);
        UserListResponse userListResponse =
                json != null ?
                        redisObjectMapper.readValue(json, new TypeReference<UserListResponse>() {})
                        : null;
        return userListResponse;
    }

    @Override
    public void clear() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Override
    public void saveAllUser(UserListResponse productListResponse,
                            String keyword,
                            PageRequest pageRequest) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword, pageRequest);
        String json = redisObjectMapper.writeValueAsString(productListResponse);
        redisTemplate.opsForValue().set(key, json);
    }
}
