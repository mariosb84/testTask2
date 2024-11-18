package com.example.itemservice.service;

import com.example.itemservice.domain.dto.UserDto;
import com.example.itemservice.domain.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserServiceControllerMethods {

    List<User> findAll();

     ResponseEntity<User> findById(int id);

     ResponseEntity<User> create(User user);

     ResponseEntity<Boolean> update(UserDto person);

     ResponseEntity<Boolean> delete(int id);

     ResponseEntity<User> getCurrentUser(String username);

     ResponseEntity<List<User>> findUsersByUsernameContains(String userName);

}
