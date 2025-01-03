package com.example.policy.service;



import com.example.policy.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    List<User> getAllUsers();
    User getUserById(long userId);
    User getUserByEmail(String email);
}

