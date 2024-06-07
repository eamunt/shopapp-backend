package com.project.shopapp.services.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.responses.ProductListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductRedisService implements IProductRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    @Value("${spring.data.redis.use-redis-cache}")
    private boolean useRedisCache;
    private String getKeyFrom(String keyword,
                              Long categoryId,
                              PageRequest pageRequest) {
        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        Sort sort = pageRequest.getSort();
        String sortDirection = Objects.requireNonNull(sort.getOrderFor("id"))
                .getDirection() == Sort.Direction.ASC ? "asc": "desc";
        String key = String.format("all_products:%s:%d:%d:%d:%s",
                keyword, categoryId, pageNumber, pageSize, sortDirection);
        return key;
    }
    @Override
    public ProductListResponse getAllProducts(String keyword,
                                                Long categoryId,
                                                PageRequest pageRequest) throws JsonProcessingException {

        if(useRedisCache == false) {
            return null;
        }
        String key = this.getKeyFrom(keyword, categoryId, pageRequest);
        String json = (String) redisTemplate.opsForValue().get(key);
        ProductListResponse productListResponses =
                json != null ?
                        redisObjectMapper.readValue(json, new TypeReference<ProductListResponse>() {})
                        : null;
        return productListResponses;
    }
    @Override
    public void clear(){
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Override
    //save to Redis
    public void saveAllProducts(ProductListResponse productListResponses,
                                String keyword,
                                Long categoryId,
                                PageRequest pageRequest) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword, categoryId, pageRequest);
        String json = redisObjectMapper.writeValueAsString(productListResponses);
        redisTemplate.opsForValue().set(key, json);
    }
}
