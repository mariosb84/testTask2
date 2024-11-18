package com.example.itemservice.service;

import com.example.itemservice.domain.dto.ItemDto;
import com.example.itemservice.domain.model.Item;
import com.example.itemservice.domain.model.Status;
import com.example.itemservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceDataControllerMethods implements ItemServiceControllerMethods {

    private final ItemService items;

    private final UserService persons;

    /*МЕТОДЫ USER-а:_______________________________________________________________________________*/

    /*Просмотреть список заявок  user-а с возможностью сортировки по дате создания в оба
   направления (как от самой старой к самой новой, так и наоборот) и пагинацией
   по 5 элементов, фильтрация по статусу ("hasRole('USER')")*/
    @Override
    public ResponseEntity<Page<Item>> findSortPageItemsByUser(Integer sortDirection) {
        User currentUser = persons.getCurrentUser();
        return findSortByConditionPageItemsIncludeUsers(0, 5,
                sortDirection == 0 ? "asc" : "desc",
                Status.Draft,
                List.of(persons.findUserByUsername(currentUser.getUsername())));
    }

    /*СОЗДАТЬ ЗАЯВКУ ("hasRole('USER')")*/
    @Override
    public ResponseEntity<Item> create(ItemDto itemDto) {
        Item item = items.addItemDto(itemDto);
        var result = this.items.add(item);
        return new ResponseEntity<>(
                result.orElse(new Item()),
                result.isPresent() ? HttpStatus.CREATED : HttpStatus.CONFLICT
        );
    }

    /*МЕТОД : ОТПРАВИТЬ ЗАЯВКУ ОПЕРАТОРУ НА РАССМОТРЕНИЕ ("hasRole('USER')")*/
    @Override
    public ResponseEntity<Item> sendItem(int id) {
        Item item = items.findById(id).orElseThrow();
        User currentUser = persons.getCurrentUser();
        if (items.itemContains(item, Status.Draft, currentUser.getUsername())) {
            item.setStatus(Status.Sent);
            if (items.update(item)) {
                return ResponseEntity.ok().build();
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Статус заявки не изменен("
                + "возможно - неверный статус заявки"
                + "(не \"черновик\")/либо заявка создана другим пользователем)!");
    }

    /* МЕТОД РЕДАКТИРОВАНИЯ  ЗАЯВОК В СТАТУСЕ "ЧЕРНОВИК", СОЗДАННЫХ ПОЛЬЗОВАТЕЛЕМ ("hasRole('USER')")*/
    @Override
    public ResponseEntity<Item> editUserItem(int id,  ItemDto itemDto) {
        Item item = items.findById(id).orElseThrow();
        User currentUser = persons.getCurrentUser();
        if (items.itemContains(item, Status.Draft, currentUser.getUsername())) {
            item = items.editItemDto(itemDto, id).orElseThrow();
            if (items.update(item)) {
                return ResponseEntity.ok().build();
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не обновлена("
                + "возможно - неверный статус заявки"
                + "(не \"черновик\")/либо заявка создана другим пользователем)!");
    }

    /*МЕТОДЫ OPERATOR-а:_______________________________________________________________________*/

    /*Просмотреть список всех отправленных на рассмотрение заявок с возможностью
    сортировки по дате создания в оба
    направления (как от самой старой к самой новой, так и наоборот) и пагинацией
    по 5 элементов, фильтрация по статусу. Должна быть фильтрация по имени.
    Просматривать отправленные заявки только конкретного пользователя по его
    имени/части имени (у пользователя, соответственно, должно быть поле name) ("hasRole('OPERATOR')*/
    @Override
    public ResponseEntity<Page<Item>> findSortPageItemsByOperator(Integer sortDirection, String userName) {
        if (userName != null) {
            return findSortByConditionPageItemsIncludeUsers(0, 5,
                    sortDirection == 0 ? "asc" : "desc",
                    Status.Sent,
                    persons.findUserByUsernameContains(userName));
        }
        return findSortByConditionPageItems(0, 5,
                sortDirection == 0 ? "asc" : "desc",
                Status.Sent);
    }

    /*МЕТОД : НАЙТИ ПО ID  ЗАЯВКУ ("hasRole('OPERATOR')*/
    @Override
    public ResponseEntity<Item> findItem(int id) {
        Item item = items.findById(id).orElseThrow();
        if (items.itemContains(item, Status.Sent, null)) {
            return new ResponseEntity<>(
                    item,
                    HttpStatus.OK
            );
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена("
                + "возможно - неверный статус заявки"
                + "(не \"отправлено\")!");
    }

    /*МЕТОД : ПРИНЯТЬ ЗАЯВКУ ("hasRole('OPERATOR')*/
    @Override
    public ResponseEntity<Item> acceptItem(int id) {
        Item item = items.findById(id).orElseThrow();
        if (items.itemContains(item, Status.Sent, null)) {
            item.setStatus(Status.Accepted);
            if (items.update(item)) {
                return ResponseEntity.ok().build();
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена("
                + "возможно - неверный статус заявки"
                + "(не \"отправлено\")!");
    }

    /*МЕТОД : ОТКЛОНИТЬ ЗАЯВКУ ("hasRole('OPERATOR')*/
    @Override
    public ResponseEntity<Item> rejectItem(int id) {
        Item item = items.findById(id).orElseThrow();
        if (items.itemContains(item, Status.Sent, null)) {
            item.setStatus(Status.Rejected);
            if (items.update(item)) {
                return ResponseEntity.ok().build();
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена("
                + "возможно - неверный статус заявки"
                + "(не \"отправлено\")!");
    }

    /*МЕТОДЫ ADMIN-а:___________________________________________________________________________*/

    /* Просмотреть список заявок в любом статусе с возможностью сортировки по дате создания
   в оба направления и пагинацией по 5 элементов, фильтрация по статусу ("hasRole('ADMIN')") */
    @Override
    public ResponseEntity<Page<Item>> findSortPageItemsByAdmin(Integer sortDirection, Integer status, String userName) {
        Status inputStatus = switch (status) {
            case 0 -> Status.Sent;
            case 1 -> Status.Accepted;
            default -> Status.Rejected;
        };
        String sortOrder = (sortDirection == 0) ? "asc" : "desc";
        return (userName != null)
                ? findSortByConditionPageItemsIncludeUsers(0, 5, sortOrder, inputStatus, persons.findUserByUsernameContains(userName))
                : findSortByConditionPageItems(0, 5, sortOrder, inputStatus);
    }

    /*смотреть список пользователей ("hasRole('ADMIN')*/
    @Override
    public List<User> findAllUsersList() {
        return this.persons.findAll();
    }

    /* Назначать пользователям права оператора ("hasRole('ADMIN')") */
    @Override
    public ResponseEntity<Boolean> setRoleOperator(int id) {
        User user = persons.setRoleOperator(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (!persons.update(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Роль оператора не назначена, объект не обновлен!");
        }

        return ResponseEntity.ok().build();
    }


    /*ОБЩИЕ МЕТОДЫ:___________________________________________________________________________________*/

    /*НАЙТИ ВСЕ ЗАЯВКИ*/
    @Override
    public List<Item> findAll() {
        return this.items.findAll();
    }

    /*НАЙТИ ЗАЯВКУ ПО ID*/
    @Override
    public ResponseEntity<Item> findById(int id) {
        var item = this.items.findById(id);
        if (item.isPresent()) {
            return new ResponseEntity<>(
                    item.orElse(new Item()),
                    HttpStatus.OK
            );
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена!");
    }

    /*ОБНОВИТЬ ЗАЯВКУ*/
    @Override
    public ResponseEntity<Boolean> update(Item item) {
        if ((this.items.update(item))) {
            return ResponseEntity.ok().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не обновлена!");
    }

    /*УДАЛИТЬ ЗАЯВКУ*/
    @Override
    public ResponseEntity<Boolean> delete(int id) {
        Item item = new Item();
        item.setId(id);
        if ((this.items.delete(item))) {
            return ResponseEntity.ok().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не удалена!");
    }

    /*универсальный метод сортировки, включая USER-s List
    Просмотреть список заявок с возможностью сортировки по дате создания в оба
    направления (как от самой старой к самой новой, так и наоборот) и пагинацией
    по 5 элементов, фильтрация по статусу*/
    @Override
    public ResponseEntity<Page<Item>> findSortByConditionPageItemsIncludeUsers(
            Integer offset, Integer limit, String direction,
            Status status, List<User> users
    ) {
        return new ResponseEntity<>(items.findAllItemsByStatusAndUsers(
                PageRequest.of(offset, limit,
                        Sort.by((direction.equals("asc") ? Sort.Order.asc("created")
                                : Sort.Order.desc("created")))),
                status,
                users),
                HttpStatus.OK);
    }

    /*универсальный метод сортировки
   Просмотреть список заявок с возможностью сортировки по дате создания в оба
   направления (как от самой старой к самой новой, так и наоборот) и пагинацией
   по 5 элементов, фильтрация по статусу*/
    @Override
    public ResponseEntity<Page<Item>> findSortByConditionPageItems(
            Integer offset, Integer limit, String direction, Status status) {
        return new ResponseEntity<>(items.findAllItemsByStatus(
                PageRequest.of(offset, limit,
                        Sort.by((direction.equals("asc") ? Sort.Order.asc("created")
                                : Sort.Order.desc("created")))),
                status
        ),
                HttpStatus.OK);
    }

}
