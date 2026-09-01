package com.api.kanbam.controller;

import com.api.kanbam.domain.dtos.user.UserRequestDTO;
import com.api.kanbam.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
