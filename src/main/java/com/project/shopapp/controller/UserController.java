package com.project.shopapp.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.RefreshTokenDTO;
import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.*;
import com.project.shopapp.services.token.ITokenService;
import com.project.shopapp.services.token.TokenService;
import com.project.shopapp.services.user.UserRedisService;
import com.project.shopapp.services.user.UserService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService ITokenService;
    private final ModelMapper modelMapper;
    private final TokenService tokenService;
    private final UserRedisService userRedisService;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result) {
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
            }


            User newUser = userService.createUser(userDTO);

            return ResponseEntity.ok(newUser);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        return userAgent.toLowerCase().contains("mobile");
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO
            ) throws Exception {
        UserResponse userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());

        modelMapper.typeMap(UserResponse.class, User.class);
        User user = modelMapper.map(userDetail, User.class);

        Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), user);
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Refresh Token successfully")
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .refreshToken(jwtToken.getRefreshToken())
                .username(user.getUsername())
                .roles(user.getRoleId())
                .id(user.getId())
                .build();
        return ResponseEntity.ok().body(
                loginResponse
        );


    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request)
    {
        // Kiểm tra thông tin đăng nhập và sinh ra token
        try {
            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId()
            );
            String userAgent = request.getHeader("User-Agent");
            UserResponse userDetail = userService.getUserDetailsFromToken(token);

            modelMapper.typeMap(UserResponse.class, User.class);
            User user = modelMapper.map(userDetail, User.class);
            Token jwtToken = ITokenService.addToken(user, token, isMobileDevice(userAgent));

            return ResponseEntity.ok().body(LoginResponse.builder()
                            .tokenType(jwtToken.getTokenType())
                            .id(user.getId())
                            .username(user.getUsername())
                            .roles(user.getRoleId())
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(jwtToken.getToken())
                            .refreshToken(jwtToken.getRefreshToken())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                    .build()
            );
        }
        // Return token in response
    }

    @PostMapping ("/details")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<UserResponse> getUserDetails(@RequestHeader("Authorization") String authorizationHeader){
        try {
            String extractedToken = authorizationHeader.substring(7);
            UserResponse user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok().body(user);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/details/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        try{
            String extractedToken = authorizationHeader.substring(7);
            UserResponse user = userService.getUserDetailsFromToken(extractedToken);

            if(user.getId() != userId){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            UserResponse updatedUser = userService.updateUser(userId, updatedUserDTO);
            return ResponseEntity.ok().body(updatedUser);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception {
        // create Pageable từ thông tin page và limit
        // sort: newest on top.
        PageRequest pageRequest = PageRequest.of(page, limit,
//                Sort.by("createdAt").descending());
                Sort.by("id").ascending());



        // kiểm tra trong redis có tồn tại chưa
        UserListResponse userListResponse = userRedisService.getAllUser(keyword, pageRequest);
        int totalPages = 0;

        // nếu chưa tồn tại trong Redis
        if(userListResponse == null){
            Page<UserResponse> userPage = userService.findAll(keyword, pageRequest);
            // get total of pages
            totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();
            userListResponse = UserListResponse
                    .builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build();
            userRedisService.saveAllUser(
                    userListResponse,
                    keyword,
                    pageRequest
            );
        }
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Get user list successfully")
                .status(HttpStatus.OK)
                .data(userListResponse)
                .build());
    }

    @GetMapping("/clear")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> clearCache(){
        userRedisService.clear();
        return ResponseEntity.ok().body("clear successfully");
    }
}


