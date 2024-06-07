package com.project.shopapp.services.token;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.ExpiredTokenException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService  implements ITokenService{
    private static final int MAX_TOKENS = 3;
    @Value("${jwt.expiration}")
    private int expiration;
    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    private final TokenRepository tokenRepository;
    private final JwtTokenUtils jwtTokenUtils;

    @Transactional
    @Override
    public Token addToken(User user, String token, boolean isMobileDevice) {
        List<Token> userTokens = tokenRepository.findByUserId(user);
        int tokenCount = userTokens.size();
        // số lượng token vượt quá limit thì  xóa một token cũ
        if(tokenCount >= MAX_TOKENS) {
            //kiểm tra xem trong danh sách userTokens có tổn tại ít nhất
            //một token không phải là thiết bị mobile (non-mobile)
            //ưu tiên xóa device không phải là mobile (nghĩa là xóa login trên web)
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if(hasNonMobileToken){
                tokenToDelete = userTokens.stream()
                        .filter(userToken -> !userToken.isMobile())
                        .findFirst()
                        .orElse(userTokens.get(0));
            }else {
                // tất cả các token đều là thiết bị mobile
                // chúng ta sẽ cóa token đầu tiên trong list
                tokenToDelete = userTokens.get(0);
            }
            tokenRepository.delete(tokenToDelete);
        }
        long expirationInSeconds = expiration;
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expirationInSeconds);
        // Tạo mới một token cho user
        Token newToken = Token.builder()
                .userId(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .isMobile(isMobileDevice)
                .build();

        newToken.setRefreshToken(UUID.randomUUID().toString());
        newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));

        tokenRepository.save(newToken);
        return newToken;
    }

    @Transactional
    @Override
    public Token refreshToken(String refreshToken, User user) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if(existingToken == null){
            throw new DataNotFoundException("Refresh token does not exist");
        }
        if(existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(existingToken);
            throw new ExpiredTokenException("Refresh token is expired");
        }
        String token = jwtTokenUtils.generateToken(user);
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setToken(token);
        existingToken.setRefreshToken(UUID.randomUUID().toString());
        existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        return existingToken;
    }
}
