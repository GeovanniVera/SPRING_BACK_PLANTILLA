package com.krouser.backend.users.controller;

import com.krouser.backend.shared.dto.ApiResponse;
import com.krouser.backend.users.dto.AssignRolesRequest;
import com.krouser.backend.users.dto.CreateUserRequest;
import com.krouser.backend.users.dto.EnableUserRequest;
import com.krouser.backend.users.dto.UpdateUserRequest;
import com.krouser.backend.users.dto.UserResponse;
import com.krouser.backend.users.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USERS_READ_SELF')")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication,
            HttpServletRequest request) {
        String username = authentication.getName();
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Profile retrieved successfully", user,
                request.getRequestURI()));
    }

    @GetMapping("/{idPublic}")
    @PreAuthorize("hasAuthority('USERS_READ_ALL')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByIdPublic(@PathVariable UUID idPublic,
            HttpServletRequest request) {
        UserResponse user = userService.getUserByIdPublic(idPublic);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User retrieved successfully", user,
                request.getRequestURI()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USERS_READ_ALL')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(HttpServletRequest request) {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Users retrieved successfully", users,
                request.getRequestURI()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest createRequest,
            HttpServletRequest request) {
        UserResponse newUser = userService.createUser(createRequest);
        // Using idPublic for the location URI
        URI location = URI.create(request.getRequestURI() + "/" + newUser.getIdPublic());
        return ResponseEntity.created(location)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User created successfully", newUser,
                        request.getRequestURI()));
    }

    @PutMapping("/{idPublic}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USERS_UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID idPublic,
            @Valid @RequestBody UpdateUserRequest updateRequest,
            HttpServletRequest request) {
        UserResponse updatedUser = userService.updateUser(idPublic, updateRequest);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User updated successfully", updatedUser,
                request.getRequestURI()));
    }

    @PatchMapping("/{idPublic}/enabled")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USERS_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(@PathVariable UUID idPublic,
            @Valid @RequestBody EnableUserRequest enableRequest) {
        userService.changeUserStatus(idPublic, enableRequest.isEnabled());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{idPublic}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USERS_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(@PathVariable UUID idPublic,
            @Valid @RequestBody AssignRolesRequest assignRequest,
            HttpServletRequest request) {
        userService.assignRolesToUser(idPublic, assignRequest.getRoles());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
