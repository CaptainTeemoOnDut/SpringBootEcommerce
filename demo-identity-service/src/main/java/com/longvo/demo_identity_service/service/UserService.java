package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.constant.PredefinedRole;
import com.longvo.demo_identity_service.dto.request.ChangePasswordRequest;
import com.longvo.demo_identity_service.dto.request.CreatePasswordRequest;
import com.longvo.demo_identity_service.dto.request.UserCreationRequest;
import com.longvo.demo_identity_service.dto.request.UserUpdateRequest;
import com.longvo.demo_identity_service.dto.response.UserProfileResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.entity.Role;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.UserMapper;
import com.longvo.demo_identity_service.repository.RoleRepository;
import com.longvo.demo_identity_service.repository.UserRepository;
import com.longvo.demo_identity_service.repository.httpclient.ChatClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    ChatClient chatClient;
    private final UserRepository userRepository;

    public UserProfileResponse getProfile(String userName) {
        User userProfile =
                userRepository.findByUsername(userName).orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return UserProfileResponse.builder()
                .id(String.valueOf(userProfile.getId()))
                .userId(String.valueOf(userProfile.getId()))
                .username(userProfile.getUsername())
                .avatar(userProfile.getAvatarUrl())
                .email(userProfile.getEmail())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .dob(userProfile.getDob())
                .city(userProfile.getAddress().toString())
                .build();
    }

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUse(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        List<String> ids = new ArrayList<>();
        ids.add("Vo Long");
        ids.add("New Vo Long");
        /*ConversationRequest requestConversation = new ConversationRequest();
        requestConversation.setType("haha");
        requestConversation.setParticipantIds(ids);
        chatClient.createConversation(requestConversation);*/

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        try {
            user =  userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    private User getCurrentUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public void createPassword(CreatePasswordRequest request){

        User user = getCurrentUser();

        if (StringUtils.hasText(user.getPassword())){
            throw new AppException(ErrorCode.PASSWORD_EXISTED);
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request){

        User user = getCurrentUser();

        if (!StringUtils.hasText(user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_EXISTED);
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public User findOrCreateByGoogle(String email, String name, String picture) {
        // TODO: kiểm tra DB theo email, nếu không tồn tại thì tạo user mới
        User user;
        user = userRepository.findByEmail(email);
        if (user == null) {
            user.setUsername(email);
            user.setFirstName(name);
            user.setLastName(name);
            user.setEmail(email);
            user.setRoles(new HashSet<>());
            userRepository.save(user);
        }
        return user;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var userResponse = userMapper.toUserResponse(user);
        userResponse.setNoPassword(!StringUtils.hasText(user.getPassword()));
        return userResponse;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var role = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(role));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse suspendUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setIsActive(false);
        user.setSuspendedAt(LocalDateTime.now());
        user.setSuspendedReason(reason);

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setIsActive(true);
        user.setSuspendedAt(null);
        user.setSuspendedReason(null);

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

}
