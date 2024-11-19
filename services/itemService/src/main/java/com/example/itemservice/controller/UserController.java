package com.example.itemservice.controller;

import com.example.itemservice.domain.model.User;
import com.example.itemservice.domain.dto.UserDto;
import com.example.itemservice.handlers.Operation;
import com.example.itemservice.service.UserServiceControllerMethods;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
public class UserController {

    private final UserServiceControllerMethods persons;

    /*НАЙТИ ВСЕХ USER*/
    @GetMapping("/person/")
    public List<User> findAll() {
        return persons.findAll();
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<User> findById(@PathVariable int id) {
      return persons.findById(id);
    }

    /*СОЗДАТЬ USER*/
    @PostMapping("/person/")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
       return persons.create(user);
    }

    /*ОБНОВИТЬ USER*/
    @PutMapping("/person/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Boolean> update(@Valid @RequestBody User person) {
       return persons.update(person);
    }

    /*УДАЛИТЬ USER*/
    @DeleteMapping("/person/{id}")
    @Validated(Operation.OnDelete.class)
    public ResponseEntity<Boolean> delete(@Valid @PathVariable int id) {
        return persons.delete(id);
    }

    /*ПОЛУЧИТЬ ТЕКУЩЕГО USER*/
    @GetMapping("/person/getCurrentUser")
    public ResponseEntity<User> getCurrentUser(@CurrentSecurityContext(expression = "authentication?.name")
                                               String username) {
        return persons.getCurrentUser(username);
    }

    /*НАЙТИ ПО ИМЕНИ USER*/
    @GetMapping("/person/findByUserName")
    public ResponseEntity<List<User>> findUsersByUsernameContains(@RequestParam(value = "userName") String userName) {
       return persons.findUsersByUsernameContains(userName);
    }

}
