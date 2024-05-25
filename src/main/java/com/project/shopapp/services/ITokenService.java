package com.project.shopapp.services;

import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.UserResponse;
import org.springframework.stereotype.Service;

@Service
public interface ITokenService{
    Token addToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user) throws Exception;
}
