package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.InvalidCreditRange;
import exceptions.NotExistentComment;
import exceptions.NotExistentCommodity;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.Baloot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@WebAppConfiguration
class CommentControllerTest {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File commentsJsonFile = new File("src/test/resources/comments.json").getAbsoluteFile();
    private static final File commentJsonFile = new File("src/test/resources/comment.json").getAbsoluteFile();
    private static ArrayList<Comment> initComments;
    private static Comment tempComment;

    private final String COMMENT_BASE_URL = "/comment/{id}/";
    private final String SUCCESSFULLY_LIKED_FEEDBACK = "The comment was successfully liked!";
    private final String SUCCESSFULLY_DISLIKED_FEEDBACK = "The comment was successfully disliked!";
    private final String NOT_EXISTENT_COMMENT_FEEDBACK = "Comment does not exist.";

    @InjectMocks
    private CommentController commentController;
    @Mock
    private Baloot baloot;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }


    @AfterEach
    public void tearDown() {
    }


    @BeforeAll
    public static void setup() {
        try {
            String initJsonComments = FileUtils.readFileToString(commentsJsonFile);
            initComments = gson.fromJson(initJsonComments, new TypeToken<ArrayList<Comment>>() {}.getType());
            String initJsonComment = FileUtils.readFileToString(commentJsonFile);
            tempComment = gson.fromJson(initJsonComment, (Type) Comment.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void teardown() {
    }

    @ParameterizedTest
    @MethodSource("populateUserNameVoteLikeDisLikeCount")
    void WHEN_users_vote_inverse_THEN_like_count_successfully_change(String username, String vote, Integer like, Integer dislike) throws Exception {
        //setup
        String expected = Objects.equals(vote, "like") ? SUCCESSFULLY_LIKED_FEEDBACK : SUCCESSFULLY_DISLIKED_FEEDBACK;
        Comment testingComment = initComments.get(0);
        Map<String, String> input = new HashMap<String, String>(){{
            put("username", username);
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        when(baloot.getCommentById(anyInt())).thenReturn(testingComment);

        //exercise
        testingComment.addUserVote(username, vote);

        String action = COMMENT_BASE_URL + vote;
        MvcResult result = mockMvc.perform(post(action, String.valueOf(testingComment.getId()))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        //verify
        assertEquals(testingComment.getLike(), like);
        assertEquals(testingComment.getDislike(), dislike);
        assertEquals(expected, content);
        //teardown
    }


    @Test
    void WHEN_comment_id_in_disliking_is_invalid_THEN_get_no_existent_comment() throws Exception {
        //setup
        String username = "ryhn";
        String vote = "dislike";
        Map<String, String> input = new HashMap<String, String>(){{
            put("username", username);
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doThrow(new NotExistentComment()).when(baloot).getCommentById(anyInt());

        //exercise
        String action = COMMENT_BASE_URL + vote;
        MvcResult result = mockMvc.perform(post(action, "-1")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        //verify
        assertTrue(content.contains(NOT_EXISTENT_COMMENT_FEEDBACK));
        //teardown
    }

    @Test
    void WHEN_comment_id_in_linking_is_invalid_THEN_get_no_existent_comment() throws Exception {
        //setup
        String username = "ryhn";
        String vote = "like";
        Map<String, String> input = new HashMap<String, String>(){{
            put("username", username);
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doThrow(new NotExistentComment()).when(baloot).getCommentById(anyInt());

        //exercise
        String action = COMMENT_BASE_URL + vote;
        MvcResult result = mockMvc.perform(post(action, "-1")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        //verify
        assertTrue(content.contains(NOT_EXISTENT_COMMENT_FEEDBACK));
        //teardown
    }

    private static Stream<Arguments> populateUserNameVoteLikeDisLikeCount(){
        tempComment.setUserVote(new HashMap<>());
        return Stream.of(
                Arguments.of("vote1", "like", 1, 0),
                Arguments.of("vote2", "like", 2, 0),
                Arguments.of("vote3", "dislike", 2, 1),
                Arguments.of("vote1", "dislike", 1, 2),
                Arguments.of("vote4", "dislike", 1, 3),
                Arguments.of("vote4", "like", 2, 2)
        );
    }
}