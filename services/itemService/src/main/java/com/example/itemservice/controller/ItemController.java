package com.example.itemservice.controller;

import com.example.itemservice.domain.dto.ItemDto;
import com.example.itemservice.domain.model.Item;
import com.example.itemservice.domain.model.User;
import com.example.itemservice.handlers.Operation;
import com.example.itemservice.service.ItemServiceControllerMethods;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@AllArgsConstructor
@RestController
public class ItemController {

    private final ItemServiceControllerMethods items;

    /*МЕТОДЫ USER-а:_______________________________________________________________________________*/

    /*Просмотреть список заявок  user-а с возможностью сортировки по дате создания в оба
   направления (как от самой старой к самой новой, так и наоборот) и пагинацией
   по 5 элементов, фильтрация по статусу ("hasRole('USER')")*/
    @GetMapping("/item/sortItemsByUser")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Item>> findSortPageItemsByUser(
            @RequestParam(value = "sortDirection",
                    defaultValue = "0") @Min(0) @Max(1) Integer sortDirection
    ) {
        return items.findSortPageItemsByUser(sortDirection);
    }

    /*СОЗДАТЬ ЗАЯВКУ ("hasRole('USER')")*/
    @PostMapping("/item/createItem")
    @Validated(Operation.OnCreate.class)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Item> create(@Valid @RequestBody ItemDto itemDto) {
        return items.create(itemDto);
    }

    /*МЕТОД : ОТПРАВИТЬ ЗАЯВКУ ОПЕРАТОРУ НА РАССМОТРЕНИЕ ("hasRole('USER')")*/
    @PutMapping("/item/sendItem/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Item> sendItem(@PathVariable int id) {
       return items.sendItem(id);
    }

    /* МЕТОД РЕДАКТИРОВАНИЯ  ЗАЯВОК В СТАТУСЕ "ЧЕРНОВИК", СОЗДАННЫХ ПОЛЬЗОВАТЕЛЕМ ("hasRole('USER')")*/

    @PutMapping("/item/editUserItem/{id}")
    @Validated(Operation.OnUpdate.class)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Item> editUserItem(
            @PathVariable int id, @Valid @RequestBody ItemDto itemDto) {
        return items.editUserItem(id, itemDto);
    }

    /*МЕТОДЫ OPERATOR-а:_______________________________________________________________________*/

    /*Просмотреть список всех отправленных на рассмотрение заявок с возможностью
    сортировки по дате создания в оба
    направления (как от самой старой к самой новой, так и наоборот) и пагинацией
    по 5 элементов, фильтрация по статусу. Должна быть фильтрация по имени.
    Просматривать отправленные заявки только конкретного пользователя по его
    имени/части имени (у пользователя, соответственно, должно быть поле name) ("hasRole('OPERATOR')*/
    @GetMapping("/item/sortItemsByOperator")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Page<Item>> findSortPageItemsByOperator(
            @RequestParam(value = "sortDirection",
                    defaultValue = "0") @Min(0) @Max(1) Integer sortDirection,
            @RequestParam(value = "userName", defaultValue = "") String userName
    ) {
        return items.findSortPageItemsByOperator(sortDirection, userName);
    }

    /*МЕТОД : НАЙТИ ПО ID  ЗАЯВКУ ("hasRole('OPERATOR')*/
    @GetMapping("/item/findItem/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Item> findItem(
            @PathVariable int id) {
        return items.findItem(id);
    }

    /*МЕТОД : ПРИНЯТЬ ЗАЯВКУ ("hasRole('OPERATOR')*/
    @PutMapping("/item/acceptItem/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Item> acceptItem(
            @PathVariable int id) {
        return items.acceptItem(id);
    }

    /*МЕТОД : ОТКЛОНИТЬ ЗАЯВКУ ("hasRole('OPERATOR')*/
    @PutMapping("/item/rejectItem/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Item> rejectItem(
            @PathVariable int id) {
        return items.rejectItem(id);
    }

    /*МЕТОДЫ ADMIN-а:___________________________________________________________________________*/

    /*Просмотреть список заявок в любом статусе с возможностью сортировки по дате создания в оба
   направления (как от самой старой к самой новой, так и наоборот) и пагинацией
   по 5 элементов, фильтрация по статусу ("hasRole('ADMIN')*/
    @GetMapping("/item/sortItemsByAdmin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Item>> findSortPageItemsByAdmin(
            @RequestParam(value = "sortDirection", defaultValue = "0") @Min(0) @Max(1) Integer sortDirection,
            @RequestParam(value = "status", defaultValue = "0") @Min(0) @Max(2) Integer status,
            @RequestParam(value = "userName", defaultValue = "") String userName
    ) {
        return items.findSortPageItemsByAdmin(sortDirection, status, userName);
    }

    /*смотреть список пользователей ("hasRole('ADMIN')*/
    @GetMapping("/item/findAllUsersList")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> findAllUsersList() {
        return items.findAllUsersList();
    }

    /* назначать пользователям права оператора ("hasRole('ADMIN')*/

    @PutMapping("/item/setRoleOperator/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> setRoleOperator(
            @PathVariable int id) {
       return items.setRoleOperator(id);
    }

    /*ОБЩИЕ МЕТОДЫ:___________________________________________________________________________________*/

    /*НАЙТИ ВСЕ ЗАЯВКИ*/
    @GetMapping("/item/findAll")
    public List<Item> findAll() {
        return items.findAll();
    }

    /*НАЙТИ ЗАЯВКУ ПО ID*/
    @GetMapping("/item/{id}")
    public ResponseEntity<Item> findById(@PathVariable int id) {
        return items.findById(id);
    }

    /*ОБНОВИТЬ ЗАЯВКУ*/
    @PutMapping("/item/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Boolean> update(@RequestBody Item item) {
       return items.update(item);
    }

    /*УДАЛИТЬ ЗАЯВКУ*/
    @DeleteMapping("/item/{id}")
    @Validated(Operation.OnDelete.class)
    public ResponseEntity<Boolean> delete(@Valid @PathVariable int id) {
        return items.delete(id);
    }

}
