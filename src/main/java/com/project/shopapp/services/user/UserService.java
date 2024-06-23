package com.project.shopapp.services.user;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidPasswordException;
import com.project.shopapp.exceptions.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.user.UserResponse;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;
    private final ModelMapper modelMapper;
    private final TokenRepository tokenRepository;
    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();

        // check existing.
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));
        if(role.getName().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You can not register an admin account");
        }
        Role role1 = roleRepository.findById(userDTO.getRoleId())
                .orElse(roleRepository.findById(1L).orElse(null));
        // convert from UserDTO to =>  User
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .active(true)
                .password(userDTO.getPassword())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .roleId(role1)
                .build();

        // nếu có accountId => không yêu cầu password
        if(userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0){
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);

    }

    @Override
    public String login(String phoneNumber, String password, Long roleId) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);

        if(optionalUser.isEmpty()){
            throw new DataNotFoundException("Invalid phone number / password");
        }
        Long userId = optionalUser.get().getId();
        User existingUser = optionalUser.get();
        // check password
        if(existingUser.getFacebookAccountId() == 0 && existingUser.getGoogleAccountId() == 0) {
            if(!passwordEncoder.matches(password, existingUser.getPassword())){

                throw new BadCredentialsException(MessageKeys.WRONG_PHONE_OR_PASSWORD);
            }
        }

        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if(optionalRole.isEmpty() || !roleId.equals(existingUser.getRoleId().getId())){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_NOT_EXIST));
        }

        if(!existingUser.isActive()){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }

        // authenticate with Java Spting security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            phoneNumber, password,
                existingUser.getAuthorities()
        );
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User findUserById(Long userId) throws Exception {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User: " + userId + " found"));
    }

    @Override
    public UserResponse getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)){
            throw new Exception("Token is expired");
        }
        String numberPhone = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(numberPhone);
        if(user.isPresent()){
            modelMapper.typeMap(User.class, UserResponse.class);
            return modelMapper.map(user, UserResponse.class);
        }else{
            throw new Exception("User not found");
        }
    }

    @Override
    public UserResponse getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        return getUserDetailsFromToken(existingToken.getToken());
    }

    @Transactional
    @Override
    public UserResponse updateUser(Long userId, UpdateUserDTO updateUserDTO) throws Exception {
        // Find the user by ID
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Check if the provided old password matches the existing password
        String oldPassword = updateUserDTO.getOldPassword();
        String existingPassword = existingUser.getPassword();
        if (!passwordEncoder.matches(oldPassword, existingPassword)) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.OLD_PASSWORD_NOT_MATCHING));

        }

        // Update user fields if the new value is not null
        if (!updateUserDTO.getFullName().isEmpty()) {
            existingUser.setFullName(updateUserDTO.getFullName());
        }
        if (!updateUserDTO.getAddress().isEmpty()) {
            existingUser.setAddress(updateUserDTO.getAddress());
        }
        if (updateUserDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updateUserDTO.getDateOfBirth());
        }
        if (updateUserDTO.getFacebookAccountId() > 0 ) {
            existingUser.setFacebookAccountId(updateUserDTO.getFacebookAccountId());
        }
        if (updateUserDTO.getGoogleAccountId() > 0) {
            existingUser.setGoogleAccountId(updateUserDTO.getGoogleAccountId());
        }

        // Set password if account is not linked to Facebook or Google
        if (updateUserDTO.getFacebookAccountId() == 0 && updateUserDTO.getGoogleAccountId() == 0 && !updateUserDTO.getPassword().isEmpty()) {
            String newPassword = updateUserDTO.getPassword();
            String retypePassword = updateUserDTO.getRetypePassword();
            if(!newPassword.equals(retypePassword)){
                throw new DataNotFoundException("Password and retype password not the same");
            }

            String encodedPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodedPassword);

        }

        modelMapper.typeMap(User.class, UserResponse.class);
        userRepository.save(existingUser);

        return modelMapper.map(existingUser, UserResponse.class);
    }

    @Override
    public Page<UserResponse> findAll(String keyword, Pageable pageable) throws Exception {
        Page<User> usersPage;
        try {
            usersPage = userRepository.findAll(keyword, pageable);

            // sử dụng modelMapper
            modelMapper.typeMap(User.class, UserResponse.class);
            return usersPage.map(f -> modelMapper.map(f, UserResponse.class));
            // hoặc
            // return productsPage.map(ProductResponse::fromProduct);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        // when reset password then clear token
        List<Token> tokens = tokenRepository.findByUserId(existingUser);
        tokenRepository.deleteAll(tokens);
    }

    @Override
    @Transactional
    public void blockOrEnable(Long userId, boolean active) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()-> new DataNotFoundException("User not found"));
        existingUser.setActive(active);
        userRepository.save(existingUser);
    }
}
