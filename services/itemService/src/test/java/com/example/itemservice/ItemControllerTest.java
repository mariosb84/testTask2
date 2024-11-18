package com.example.itemservice;

import com.example.itemservice.controller.ItemController;
import com.example.itemservice.domain.dto.ItemDto;
import com.example.itemservice.domain.model.Item;
import com.example.itemservice.domain.model.Role;
import com.example.itemservice.domain.model.Status;
import com.example.itemservice.domain.model.User;
import com.example.itemservice.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemServiceData items;

    @MockBean
    private ItemServiceDataControllerMethods itemServiceDataControllerMethods;

    @MockBean
    private UserServiceData userService;

    @MockBean
    private TokenBlackListServiceData tokenBlackListService;

    @MockBean
    private JwtServiceData jwtService;

    @MockBean
    private SpringLiquibase liquibase;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /* Вспомогательные методы для настройки мока*/

    private String setRoles(int SetRole) {
        String role;
        if(SetRole == 0) {
            role = "testUser";
        } else if (SetRole == 1) {
            role = "testOper";
        } else {
            role = "testAdmin";
        }
        return role;
    }

    private void setUpJwtMocks(String testJwt, int SetRole) {
        when(jwtService.isTokenValid(eq(testJwt), any(UserDetails.class))).thenReturn(true);
        when(jwtService.extractUserName(eq(testJwt))).thenReturn(setRoles(SetRole));
        when(tokenBlackListService.findByToken(eq(testJwt))).thenReturn(Optional.empty());
    }

    /*ТЕСТЫ НА МЕТОДЫ USER-а:_______________________________________________________________________________*/

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testCreateIteByUser() throws Exception {
        String testJwt = "testToken";
        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 0);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Draft);

        when(items.addItemDto(any(ItemDto.class))).thenReturn(item);
        when(items.add(any(Item.class))).thenReturn(Optional.of(item));

        mockMvc.perform(post("/item/createItem")
                        .header("Authorization", "Bearer " + testJwt)
                        /* Добавляем CSRF-токен*/
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Item New\",\"itemText\":\"Test Item New Text\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testCreateItem_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 0);

        ItemDto itemDto = new ItemDto("TestName 1", "TestText 1");
        Item item = new Item();
        when(items.addItemDto(any(ItemDto.class))).thenReturn(item);
        when(items.add(any(Item.class))).thenReturn(Optional.of(item));

        mockMvc.perform(post("/item/createItem")
                        .header("Authorization", "Bearer " + testJwt)
                        /* Добавляем CSRF-токен*/
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isCreated());
    }

 @Test
 @WithMockUser(username = "testUser", roles = "USER")
    public void testCreateItem_Conflict() throws Exception {
     String testJwt = "testToken";

     /* Используем общий метод для настройки*/
     setUpJwtMocks(testJwt, 0);

     ItemDto itemDto = new ItemDto("TestName 1", "TestText 1");
        Item item = new Item();
        when(items.addItemDto(itemDto)).thenReturn(item);
        when(items.add(item)).thenReturn(Optional.empty());

     mockMvc.perform(post("/item/createItem")
                     .header("Authorization", "Bearer " + testJwt)
                     /* Добавляем CSRF-токен*/
                     .with(csrf())
                     .contentType("application/json")
                     .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isConflict());
    }


    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testSendItem_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 0);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Draft);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testUser");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Draft, "testUser")).thenReturn(true);
        when(items.update(item)).thenReturn(true);

        mockMvc.perform(put("/item/sendItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testSendItem_NotFound() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 0);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Sent);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testUser");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(new Item(), Status.Draft, "testUser")).thenReturn(false);
        when(items.update(item)).thenReturn(false);

        mockMvc.perform(put("/item/sendItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testEditItem_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 0);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Draft);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testUser");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Draft, "testUser")).thenReturn(true);
        when(items.editItemDto(itemDto, 1)).thenReturn(Optional.of(item));
        when(items.update(item)).thenReturn(true);


        mockMvc.perform(put("/item/editUserItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testEditItem_NotFound() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 0);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Sent);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testUser");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(new Item(), Status.Draft, "testUser")).thenReturn(false);
        when(items.editItemDto(itemDto, 1)).thenReturn(Optional.of(item));
        when(items.update(item)).thenReturn(false);

        mockMvc.perform(put("/item/editUserItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    /*ТЕСТЫ НА МЕТОДЫ OPERATOR-а:_______________________________________________________________________________*/

    @Test
    @WithMockUser(username = "testOper", roles = "OPERATOR")
    public void testFindItem_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 1);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Sent);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testOper");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Sent, null)).thenReturn(true);

        mockMvc.perform(get("/item/findItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testOper", roles = "OPERATOR")
    public void testFindItem_NotFound() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 1);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Draft);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testOper");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Sent, null)).thenReturn(false);

        mockMvc.perform(get("/item/findItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testOper", roles = "OPERATOR")
    public void testAcceptItem_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 1);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Sent);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testOper");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Sent, null)).thenReturn(true);
        when(items.update(item)).thenReturn(true);

        mockMvc.perform(put("/item/acceptItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testOper", roles = "OPERATOR")
    public void testAcceptItem_NotFound() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 1);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Draft);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testOper");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Sent, null)).thenReturn(false);
        when(items.update(item)).thenReturn(false);

        mockMvc.perform(put("/item/acceptItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testOper", roles = "OPERATOR")
    public void testRejectItem_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 1);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Sent);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testOper");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Sent, null)).thenReturn(true);
        when(items.update(item)).thenReturn(true);

        mockMvc.perform(put("/item/rejectItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testOper", roles = "OPERATOR")
    public void testRejectItem_NotFound() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 1);

        ItemDto itemDto = new ItemDto("Test Item New", "Test Item New Text");
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setItemText(itemDto.getItemText());
        item.setStatus(Status.Draft);

        when(items.findById(1)).thenReturn(Optional.of(item));
        User user = new User();
        user.setUsername("testOper");
        when(userService.getCurrentUser()).thenReturn(user);
        when(items.itemContains(item, Status.Sent, null)).thenReturn(false);
        when(items.update(item)).thenReturn(false);

        mockMvc.perform(put("/item/rejectItem/{id}", 1)
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    /*ТЕСТЫ НА МЕТОДЫ ADMIN-а:_______________________________________________________________________________*/

    /*смотреть список пользователей*/

    @Test
    @WithMockUser(username = "testAdmin", roles = "ADMIN")
    public void testFindAkkUsers() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 2);

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        user1.setUsername("User1");
        user2.setUsername("User2");
        user3.setUsername("User3");
        user1.setRoles(List.of(Role.ROLE_USER));
        user2.setRoles(List.of(Role.ROLE_OPERATOR));
        user3.setRoles(List.of(Role.ROLE_USER));
        List<User> users = Arrays.asList(user1, user2, user3);
        /* Настройка мок-сервиса*/
        when(userService.findAll()).thenReturn(users);
        mockMvc.perform(get("/item/findAllUsersList")
                        .header("Authorization", "Bearer " + testJwt)
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(content().json(new ObjectMapper().writeValueAsString(users)));
    }

    /* назначать пользователям права оператора*/

    @Test
    @WithMockUser(username = "testAdmin", roles = "ADMIN")
    public void testSetRoleOperator_Success() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 2);

        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setUsername("User");
        user.setRoles(List.of(Role.ROLE_USER));

        /* Настройка мок-сервиса*/
        when(userService.setRoleOperator(userId)).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenReturn(true);

        mockMvc.perform(put("/item/setRoleOperator/{id}", userId)
                        .header("Authorization", "Bearer" + testJwt)
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = "ADMIN")
    public void testSetRoleOperatorUserNotFound() throws Exception {
        String testJwt = "testToken";

        /* Используем общий метод для настройки*/
        setUpJwtMocks(testJwt, 2);

        int userId = 1;
        int userIdTest = 2;
        User user = new User();
        user.setId(userIdTest);
        user.setUsername("User");
        user.setRoles(List.of(Role.ROLE_USER));

        /* Настройка мок-сервиса*/
        when(userService.setRoleOperator(userId)).thenReturn(Optional.empty());
        when(userService.update(any(User.class))).thenReturn(false);

        mockMvc.perform(put("/item/setRoleOperator/{id}", userId)
                        .header("Authorization", "Bearer" + testJwt)
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

}







