package com.project.shopapp.repositories;

import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUserId(User user);
    Token findByToken(String token);
}
