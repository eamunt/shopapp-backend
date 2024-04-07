package com.project.shopapp.services;

import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.UserResponse;

import java.util.Map;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password, Long roleId) throws Exception;
    User findUserById(Long userId) throws Exception;

    UserResponse getUserDetailsFromToken(String token) throws Exception;

    UserResponse updateUser(Long userId, UpdateUserDTO updateUserDTO) throws Exception;

}
