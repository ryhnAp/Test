package controllers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.*;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.Baloot;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@WebAppConfiguration
public class AuthenticationControllerTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File AuthsJsonFile = new File("src/test/resources/authentications.json").getAbsoluteFile();
    private static final File AuthJsonFile = new File("src/test/resources/authentication.json").getAbsoluteFile();
    private static ArrayList<User> initUsers;
    private static User user;


    @InjectMocks
    private static AuthenticationController authenticationController;
    @Mock
    private Baloot baloot;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @AfterEach
    public void tearDown() {
    }

    @BeforeAll
    public static void setup() {
        try {
            String initJsonAuths = FileUtils.readFileToString(AuthsJsonFile);
            initUsers = gson.fromJson(initJsonAuths, new TypeToken<ArrayList<User>>() {
            }.getType());
            String initJsonComment = FileUtils.readFileToString(AuthJsonFile);
            user = gson.fromJson(initJsonComment, (Type) Comment.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @AfterAll
    public static void teardown() {
    }

    @Test
    public void WHEN_login_THEN_success() throws NotExistentUser, IncorrectPassword, Exception {
        Map<String, String> input = new HashMap<>();
        input.put("username", "mahya");
        input.put("password", "1234");

        doNothing().when(baloot).login("mahya", "1234");
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(input)))
                .andExpect(status().isOk())
                .andReturn();

        // Verify
        String responseBody = result.getResponse().getContentAsString();
        assertEquals("login successfully!", responseBody);

        verify(baloot, times(1)).login("mahya", "1234");

    }

    @Test
    public void WHEN_login_THEN_user_does_not_exist() throws NotExistentUser, IncorrectPassword, Exception {
        Map<String, String> input = new HashMap<>();
        input.put("username", "nonExistent");
        input.put("password", "none");

        doThrow(new NotExistentUser()).when(baloot).login("nonExistent", "none");
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(input)))
                .andExpect(status().isNotFound())
                .andReturn();

        // Verify
        String responseBody = result.getResponse().getContentAsString();
        assertEquals("User does not exist.", responseBody);
        verify(baloot, times(1)).login("nonExistent", "none");

    }

    @Test
    public void WHEN_login_THEN_password_was_incorrect() throws NotExistentUser, IncorrectPassword, Exception {
        Map<String, String> input = new HashMap<>();
        input.put("username", "user");
        input.put("password", "incorrect");

        doThrow(new IncorrectPassword()).when(baloot).login("user", "incorrect");

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(input)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Verify
        String responseBody = result.getResponse().getContentAsString();

        assertEquals("Incorrect password.", responseBody);
        verify(baloot, times(1)).login("user", "incorrect");
    }

    @Test
    public void WHWN_signup_THEN_success() throws UsernameAlreadyTaken, Exception {
        Map<String, String> input = new HashMap<>();
        input.put("address", "testAddress");
        input.put("birthDate", "2001-05-01");
        input.put("email", "mahyan@yahoo.com");
        input.put("username", "mahya");
        input.put("password", "1234");

        doNothing().when(baloot).addUser(any(User.class));
        MvcResult result = mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(input)))
                .andExpect(status().isOk())
                .andReturn();

        // Verify
        String responseBody = result.getResponse().getContentAsString();
        assertEquals("signup successfully!", responseBody);

        verify(baloot, times(1)).addUser(any(User.class));

    }

    @Test
    public void WHEN_signup_THEN_username_already_exists() throws UsernameAlreadyTaken, Exception {
        Map<String, String> input = new HashMap<>();
        input.put("address", "testAddress");
        input.put("birthDate", "2001-05-01");
        input.put("email", "mahyan@yahoo.com");
        input.put("username", "mahya");
        input.put("password", "1234");

        doThrow(new UsernameAlreadyTaken()).when(baloot).addUser(any(User.class));
        MvcResult result = mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(input)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertEquals("The username is already taken.", responseBody);

        verify(baloot, times(1)).addUser(any(User.class));
    }
}