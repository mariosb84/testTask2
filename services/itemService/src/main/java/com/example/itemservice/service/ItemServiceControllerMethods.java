package com.example.itemservice.service;

import com.example.itemservice.domain.dto.ItemDto;
import com.example.itemservice.domain.model.Item;
import com.example.itemservice.domain.model.Status;
import com.example.itemservice.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ItemServiceControllerMethods {


     ResponseEntity<Page<Item>> findSortPageItemsByUser(Integer sortDirection);


     ResponseEntity<Item> create(ItemDto itemDto);


     ResponseEntity<Item> sendItem(int id);


     ResponseEntity<Item> editUserItem(int id,  ItemDto itemDto);

     ResponseEntity<Page<Item>> findSortPageItemsByOperator(Integer sortDirection, String userName);

     ResponseEntity<Item> findItem(int id);

    ResponseEntity<Item> acceptItem(int id);

     ResponseEntity<Item> rejectItem(int id);

     ResponseEntity<Page<Item>> findSortPageItemsByAdmin(Integer sortDirection, Integer status, String userName);

     List<User> findAllUsersList();

     ResponseEntity<Boolean> setRoleOperator(int id);

     List<Item> findAll();

    ResponseEntity<Item> findById(int id);

    ResponseEntity<Boolean> update(Item item);

    ResponseEntity<Boolean> delete(int id);


    ResponseEntity<Page<Item>> findSortByConditionPageItemsIncludeUsers(
            Integer offset, Integer limit, String direction,
            Status status, List<User> users
    );

    ResponseEntity<Page<Item>> findSortByConditionPageItems(
            Integer offset, Integer limit, String direction, Status status);

}
