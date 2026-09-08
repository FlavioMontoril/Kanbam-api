package com.api.kanbam.controller;

import com.api.kanbam.domain.dtos.user.UserRequestDTO;
import com.api.kanbam.domain.dtos.user.UserResponseDTO;
import com.api.kanbam.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody UserRequestDTO data){
        userService.createUserService(data);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created Succesfulle");
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> users = userService.findAllUsersService();
        return ResponseEntity.ok(users);
    }
}
