package com.project.shopapp.services.user;

import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidPasswordException;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password, Long roleId) throws Exception;
    User findUserById(Long userId) throws Exception;

    UserResponse getUserDetailsFromToken(String token) throws Exception;
    UserResponse getUserDetailsFromRefreshToken(String refreshToken) throws Exception;
    UserResponse updateUser(Long userId, UpdateUserDTO updateUserDTO) throws Exception;
    Page<UserResponse> findAll(String keyword, Pageable pageable) throws Exception;
    void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException;
    void blockOrEnable(Long userId, boolean active) throws DataNotFoundException;
}
