package com.klu.service;

import com.klu.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    User register(User user);

    Map<String, String> login(Map<String, String> credentials);

    List<User> getAllUsers();

    User getUserById(Long id);

    User updateUser(Long id, User user);

    void deleteUser(Long id);
}
