package com.project.shopapp.services;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.models.User;

import java.util.Map;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    Map<String, String> login(String phoneNumber, String password, Long roleId) throws Exception;
    User findUserById(Long userId) throws Exception;
}
