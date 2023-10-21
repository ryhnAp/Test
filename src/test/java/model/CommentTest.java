package model;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File commentsJsonFile = new File("src/test/resources/comments.json").getAbsoluteFile();
    private static final File commentJsonFile = new File("src/test/resources/comment.json").getAbsoluteFile();
    private static ArrayList<Comment> initComments;
    private static Comment tempComment;

    @BeforeEach
    public void setUp() {


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

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void WHEN_dates_are_created_and_called_THEN_duration_less_than_1sec_successful(){
        //setup
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date expectedDate = new Date();

        //exercise
        Date actualDate = expectedDate;
        try {
            Thread.sleep(0);
            String actualDateString = tempComment.getCurrentDate();
            actualDate = dateFormat.parse(actualDateString);
        } catch (ParseException | InterruptedException e) {
            e.printStackTrace();
        }
        long timeDifference = (actualDate.getTime())/1000 - (expectedDate.getTime())/1000;

        //verify
        // Assert that the time difference is 1 seconds or less (1000 milliseconds)
        assertTrue(timeDifference <= 1000);
        assertTrue(timeDifference >= 0);

        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateUserNameVoteLikeDisLikeCount")
    void WHEN_users_vote_inverse_THEN_like_count_successfully_change(String username, String vote, Integer like, Integer dislike) {
        //setup
        //exercise
        tempComment.addUserVote(username, vote);

        //verify
        assertEquals(tempComment.getLike(), like);
        assertEquals(tempComment.getDislike(), dislike);

        //teardown

    }

    @Test
    void WHEN_constructor_created_comment_THEN_specified_id_return_successfully() {
        //setup
        Integer expected = 494;
        Comment c = new Comment(expected, "userEmail@gmail.com", "username", 8101, "text");
        //exercise
        Integer actual = c.getId();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_specified_userEmail_return_successfully() {
        //setup
        String expected = "userEmail@gmail.com";
        Comment c = new Comment(1, expected, "username", 8101, "text");
        //exercise
        String actual = c.getUserEmail();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_specified_username_return_successfully() {
        //setup
        String expected = "username";
        Comment c = new Comment(1, "userEmail@gmail.com", expected, 8101, "text");
        //exercise
        String actual = c.getUsername();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_specified_commodityId_return_successfully() {
        //setup
        Integer expected = 8101;
        Comment c = new Comment(1, "userEmail@gmail.com", "username", expected, "text");
        //exercise
        Integer actual = c.getCommodityId();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_specified_text_return_successfully() {
        //setup
        String expected = "text";
        Comment c = new Comment(1, "userEmail@gmail.com", "username", 8101, expected);
        //exercise
        String actual = c.getText();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void WHEN_constructor_created_comment_THEN_specified_date_return_successfully() {
        //setup
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date expectedDate = new Date();
        Date actualDate = expectedDate;
        Comment c = new Comment(1, "userEmail@gmail.com", "username", 8101, "text");

        //exercise
        try {
            String actualDateString = c.getDate();
            actualDate = dateFormat.parse(actualDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timeDifference = (actualDate.getTime())/1000 - (expectedDate.getTime())/1000;

        //verify
        assertEquals(timeDifference, 0);

        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_default_like_count_return_successfully() {
        //setup
        int expected = 0;
        Comment c = new Comment(1, "userEmail@gmail.com", "username", 8101, "text");
        //exercise
        int actual = c.getLike();
        //verify
        assertEquals(expected, actual);

        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_default_dislike_count_return_successfully() {
        //setup
        int expected = 0;
        Comment c = new Comment(1, "userEmail@gmail.com", "username", 8101, "text");

        //exercise
        int actual = c.getLike();

        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_comment_THEN_default_empty_vote_list_return_successfully() {
        //setup
        Map<String, String> expected = new HashMap<>();
        Comment c = new Comment(1, "userEmail@gmail.com", "username", 8101, "text");

        //exercise
        Map<String, String> actual = c.getUserVote();
        //verify
        assertEquals(expected, actual);

        //teardown
    }

    @Test
    void WHEN_comment_list_ids_change_THEN_ids_must_be_different() {
        for (Comment c : initComments){
            //setup
            int replace = 494;
            //exercise
            int unexpected = c.getId();
            c.setId(replace);
            int actual = c.getId();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setId(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_emails_change_THEN_userEmails_must_be_different() {
        for (Comment c : initComments){
            //setup
            String replace = "tempEmail@gmail.com";
            //exercise
            String unexpected = c.getUserEmail();
            c.setUserEmail(replace);
            String actual = c.getUserEmail();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setUserEmail(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_names_change_THEN_usernames_must_be_different() {
        for (Comment c : initComments){
            //setup
            String replace = "tempUsername";
            //exercise
            String unexpected = c.getUsername();
            c.setUsername(replace);
            String actual = c.getUsername();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setUsername(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_comIds_change_THEN_comIds_must_be_different() {
        for (Comment c : initComments){
            //setup
            int replace = 8101;
            //exercise
            int unexpected = c.getCommodityId();
            c.setCommodityId(replace);
            int actual = c.getCommodityId();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setCommodityId(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_texts_change_THEN_texts_must_be_different() {
        for (Comment c : initComments){
            //setup
            String replace = "tempText";
            //exercise
            String unexpected = c.getText();
            c.setText(replace);
            String actual = c.getText();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setText(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_dates_change_THEN_dates_must_be_different() {
        for (Comment c : initComments){
            //setup
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date actualDate = new Date();
            Date unexpectedDate = actualDate;
            //exercise
            String unexpected = c.getDate();
            c.setDate(dateFormat.format(actualDate));

            //verify
            assertNotEquals(unexpected, c.getDate());

            //teardown
            c.setDate(dateFormat.format(unexpectedDate));
        }

    }

    @Test
    void WHEN_comment_list_likes_change_THEN_likes_must_be_different() {
        for (Comment c : initComments){
            //setup
            int replace = 5;
            //exercise
            int unexpected = c.getLike();
            c.setLike(replace);
            int actual = c.getLike();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setLike(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_dislikes_change_THEN_dislikes_must_be_different() {
        for (Comment c : initComments){
            //setup
            int replace = 5;
            //exercise
            int unexpected = c.getDislike();
            c.setDislike(replace);
            int actual = c.getDislike();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setDislike(unexpected);
        }
    }

    @Test
    void WHEN_comment_list_userVotes_change_THEN_userVotes_must_be_different() {
        for (Comment c : initComments){
            //setup
            Map<String, String> replace =  new HashMap<>(){{
                put("userVote1", "like");
                put("userVote2", "like");
                put("userVote3", "like");
                put("userVote4", "dislike");
            }};
            //exercise
            Map<String, String> unexpected = c.getUserVote();
            c.setUserVote(replace);
            Map<String, String> actual = c.getUserVote();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setUserVote(unexpected);
        }
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