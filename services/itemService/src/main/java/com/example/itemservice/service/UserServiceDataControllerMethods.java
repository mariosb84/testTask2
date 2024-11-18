package com.example.itemservice.service;

import com.example.itemservice.domain.dto.UserDto;
import com.example.itemservice.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@Service
public class UserServiceDataControllerMethods implements UserServiceControllerMethods {

    private final UserService persons;

    private final UserServiceData personsData;

    private final BCryptPasswordEncoder encoder;

    /*НАЙТИ ВСЕХ USER*/
    @Override
    public List<User> findAll() {
        return this.persons.findAll();
    }

    public ResponseEntity<User> findById(int id) {
        var person = this.persons.findById(id);
        if (person.isPresent()) {
            return new ResponseEntity<User>(
                    person.orElse(new User()),
                    HttpStatus.OK
            );
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден!");
    }

    /*СОЗДАТЬ USER*/
    @Override
    public ResponseEntity<User> create(User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new NullPointerException("Login and password mustn't be empty");
        }
        if (user.getPassword().isEmpty()
                || user.getPassword().isBlank()
                || user.getPassword().length() < 3) {
            throw new IllegalArgumentException(
                    "Invalid password. Password length must be more than 3 characters.");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        var result = this.persons.add(user);
        return new ResponseEntity<User>(
                result.orElse(new User()),
                result.isPresent() ? HttpStatus.CREATED : HttpStatus.CONFLICT
        );
    }

    /*ОБНОВИТЬ USER*/
    @Override
    public ResponseEntity<Boolean> update(UserDto person) {
        if ((this.persons.updatePatch(person))) {
            return ResponseEntity.ok().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не обновлен!");
    }

    /*УДАЛИТЬ USER*/
    @Override
    public ResponseEntity<Boolean> delete(int id) {
        User user = new User();
        user.setId(id);
        if ((this.persons.delete(user))) {
            return ResponseEntity.ok().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не удален!");
    }

    /*ПОЛУЧИТЬ ТЕКУЩЕГО USER*/
    @Override
    public ResponseEntity<User> getCurrentUser(String username) {
        var person = personsData.findUserByUsername(username);
        if (person != null) {
            return new ResponseEntity<>(
                    person,
                    HttpStatus.OK
            );
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден!");
    }

    /*НАЙТИ ПО ИМЕНИ USER*/
    @Override
    public ResponseEntity<List<User>> findUsersByUsernameContains(String userName) {
        var personsList = this.persons.findUserByUsernameContains(userName);
        if (!personsList.isEmpty()) {
            return new ResponseEntity<>(
                    personsList,
                    HttpStatus.OK
            );
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден!");
    }

}
