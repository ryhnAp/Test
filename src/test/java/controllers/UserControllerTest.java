
package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.InvalidCreditRange;
import exceptions.NotExistentCommodity;
import exceptions.NotExistentUser;
import model.Comment;
import model.Commodity;
import model.User;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.Baloot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@WebAppConfiguration
class UserControllerTest {

    @InjectMocks
    private UserController usersController;
    @Mock
    private Baloot baloot;
    @Mock
    private User balootUser;

    private MockMvc mockMvc;

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File usersJsonFile = new File("src/test/resources/users.json").getAbsoluteFile();

    private static ArrayList<User> initUsers;



    private final String USER_BASE_URL = "/users";
    private final String ID_PATH_VARIABLE_URL = "/{id}";
    private final String CREDIT_API_URL = "/credit";

    private final String CREDIT_ADD_SUCCESSFULLY_FEEDBACK = "credit added successfully!";
    private final String CREDIT_INVALID_NUMBER_FEEDBACK = "Please enter a valid number for the credit amount.";
    private final String CREDIT_INVALID_RANGE_FEEDBACK = "Credit value must be a positive float";
    private final String CREDIT_USER_NOT_EXIST_FEEDBACK = "User does not exist.";

    @BeforeAll
    public static void setup(){
        try {
            String initJsonUsers = FileUtils.readFileToString(usersJsonFile);
            initUsers = gson.fromJson(initJsonUsers, new TypeToken<ArrayList<User>>() {}.getType());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @AfterAll
    public static void teardown() {
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usersController).build();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    void GIVEN_incorrect_user_id_WHEN_get_user_THEN_get_no_existent_user() throws Exception {
        //setup
        String userId = "-1";
        doThrow(new NotExistentUser()).when(baloot).getUserById(any());

        //exercise
        String action = USER_BASE_URL +"{id}";
        MvcResult result = mockMvc.perform(get(action, userId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isNotFound())

                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertNull(usersController.getUser(userId).getBody());
        assertEquals(HttpStatus.NOT_FOUND, usersController.getUser(userId).getStatusCode());

        //teardown
    }

    @Test
    void GIVEN_json_users_WHEN_users_are_valid_THEN_get_user_successfully() throws Exception {
        for( User u : initUsers){
            //setup
            String userId = u.getUsername();

            when(baloot.getUserById(any())).thenReturn(u);

            //exercise
            String action = USER_BASE_URL + ID_PATH_VARIABLE_URL;
            MvcResult result = mockMvc.perform(get(action, userId)
                            .contentType(MediaType.APPLICATION_JSON))
    //                .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            User actual = objectMapper.readValue(content, new TypeReference<User>() {});

            //verify
            assertThat(actual)
                    .usingRecursiveComparison()
                    .isEqualTo(u);
            //teardown
        }
    }

    @Test
    void GIVEN_valid_credit_user_WHEN_add_credit_THEN_add_successfully() throws Exception {
        //setup
        String userId = "1";

        Map<String, String> input = new HashMap<String, String>(){{
            put("credit", "0");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doNothing().when(balootUser).addCredit(anyFloat());
        when(baloot.getUserById(any())).thenReturn(balootUser);

        //exercise
        String action = USER_BASE_URL + ID_PATH_VARIABLE_URL + CREDIT_API_URL;
        MvcResult result = mockMvc.perform(post(action, userId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertEquals(CREDIT_ADD_SUCCESSFULLY_FEEDBACK, usersController.addCredit(userId, input).getBody());
        assertEquals(HttpStatus.OK, usersController.addCredit(userId, input).getStatusCode());

        //teardown
    }

    @Test
    void GIVEN_invalid_credit_number_WHEN_add_credit_THEN_bad_request() throws Exception {
        //setup
        String userId = "1";

        Map<String, String> input = new HashMap<String, String>(){{
            put("credit", "0");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doNothing().when(balootUser).addCredit(anyFloat());
        doThrow(new NumberFormatException()).when(baloot).getUserById(any());

        //exercise
        String action = USER_BASE_URL + ID_PATH_VARIABLE_URL + CREDIT_API_URL;
        MvcResult result = mockMvc.perform(post(action, userId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertEquals(CREDIT_INVALID_NUMBER_FEEDBACK, usersController.addCredit(userId, input).getBody());
        assertEquals(HttpStatus.BAD_REQUEST, usersController.addCredit(userId, input).getStatusCode());

        //teardown
    }

    @Test
    void GIVEN_invalid_credit_range_WHEN_add_credit_THEN_bad_request() throws Exception {
        //setup
        String userId = "1";

        Map<String, String> input = new HashMap<String, String>(){{
            put("credit", "0");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doThrow(new InvalidCreditRange()).when(balootUser).addCredit(anyFloat());
        when(baloot.getUserById(any())).thenReturn(balootUser);

        //exercise
        String action = USER_BASE_URL + ID_PATH_VARIABLE_URL + CREDIT_API_URL;
        MvcResult result = mockMvc.perform(post(action, userId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertEquals(CREDIT_INVALID_RANGE_FEEDBACK, usersController.addCredit(userId, input).getBody());
        assertEquals(HttpStatus.BAD_REQUEST, usersController.addCredit(userId, input).getStatusCode());

        //teardown
    }

    @Test
    void GIVEN_invalid_user_WHEN_add_credit_THEN_user_not_found() throws Exception {
        //setup
        String userId = "1";

        Map<String, String> input = new HashMap<String, String>(){{
            put("credit", "0");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doNothing().when(balootUser).addCredit(anyFloat());
        doThrow(new NotExistentUser()).when(baloot).getUserById(any());

        //exercise
        String action = USER_BASE_URL + ID_PATH_VARIABLE_URL + CREDIT_API_URL;
        MvcResult result = mockMvc.perform(post(action, userId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertEquals(CREDIT_USER_NOT_EXIST_FEEDBACK, usersController.addCredit(userId, input).getBody());
        assertEquals(HttpStatus.NOT_FOUND, usersController.addCredit(userId, input).getStatusCode());

        //teardown
    }
}