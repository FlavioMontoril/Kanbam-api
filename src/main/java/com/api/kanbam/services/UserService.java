package com.api.kanbam.services;

import com.api.kanbam.domain.dtos.user.UserRequestDTO;
import com.api.kanbam.domain.entities.User;
import com.api.kanbam.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void createUserService(UserRequestDTO data){
        userRepository.findByEmail(data.email()).ifPresent(user -> {throw new RuntimeException("Usuario já existe");
        });

        User user = User.builder()
                .name(data.name())
                .email(data.email())
                .password(data.password())
                .build();

        userRepository.save(user);
    }
}
