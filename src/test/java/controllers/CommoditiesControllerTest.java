package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import exceptions.NotExistentComment;
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
import org.mockito.Mockito;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.junit.jupiter.api.Assertions.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@WebAppConfiguration
class CommoditiesControllerTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File commoditiesJsonFile = new File("src/test/resources/commodities.json").getAbsoluteFile();
    private static final File ratingCommodityJsonFile = new File("src/test/resources/ratingCommodity.json").getAbsoluteFile();
    private static ArrayList<Commodity> initCommodities;
    private static Commodity ratingCommodity;
    private static String initJsonCommodities;

    private final String COMMODITIES_BASE_URL = "/commodities";
    private final String ID_PATH_VARIABLE_URL = "/{id}";
    private final String RATE_API_URL = "/rate";
    private final String COMMENT_API_URL = "/comment";
    private final String SEARCH_API_URL = "/search";
    private final String SUGGESTED_API_URL = "/suggested";
    private final String COMMENT_ADD_SUCCESSFULLY_FEEDBACK = "comment added successfully!";

    @InjectMocks
    private CommoditiesController commoditiesController;
    @Mock
    private Baloot baloot;

    private MockMvc mockMvc;

    @BeforeAll
    public static void setup(){
        try {
            initJsonCommodities = FileUtils.readFileToString(commoditiesJsonFile);
            initCommodities = gson.fromJson(initJsonCommodities, new TypeToken<ArrayList<Commodity>>() {}.getType());
            String initRatingJsonComment = FileUtils.readFileToString(ratingCommodityJsonFile);
            ratingCommodity = gson.fromJson(initRatingJsonComment, (Type) Commodity.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @AfterAll
    public static void teardown() {
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commoditiesController).build();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    void WHEN_get_commodities_THEN_give_same_as_json_object() throws Exception {
        //setup
        when(baloot.getCommodities()).thenReturn(initCommodities);

        //exercise
        MvcResult result = mockMvc.perform(get(COMMODITIES_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ArrayList<Commodity> actual = gson.fromJson(content, new TypeToken<ArrayList<Commodity>>() {}.getType());

        //verify
        Assertions.assertEquals(initCommodities.size(), actual.size());
        for (int i = 0; i < initCommodities.size(); i++)
            assertThat(actual.get(i))
                    .usingRecursiveComparison()
                    .isEqualTo(initCommodities.get(i));

        //teardown
    }

    @Test
    void WHEN_get_single_commodity_THEN_give_same_as_json_object() throws Exception {
        //setup
        String commodityId = "1";
        when(baloot.getCommodityById(any())).thenReturn(initCommodities.get(0));

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL;
        MvcResult result = mockMvc.perform(get(action, commodityId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Commodity actual = gson.fromJson(content, Commodity.class);
        //verify
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(initCommodities.get(0));

        //teardown
    }

    @Test
    void WHEN_get_single_commodity_id_is_invalid_THEN_get_no_existent_commodity() throws Exception {
        //setup
        String commodityId = "1";
        doThrow(new NotExistentCommodity()).when(baloot).getCommodityById(any());

        //exercise
        String action = COMMODITIES_BASE_URL +"{id}";
        MvcResult result = mockMvc.perform(get(action, commodityId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                                .andExpect(status().isNotFound())

                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertNull(commoditiesController.getCommodity(commodityId).getBody());
        assertEquals(HttpStatus.NOT_FOUND, commoditiesController.getCommodity(commodityId).getStatusCode());

        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateCommodityRatingScenario")
    void WHEN_new_or_prev_user_vote_THEN_respectively_add_change_rating(Commodity c, String username, int newVote) throws Exception {
        //setup
        int ratingCount = c.getUserRate().size() +1;
        float sumBeforeVote = c.getRating() * ratingCount;
        float expectedRate = 0;

        Map<String, String> input = new HashMap<String, String>(){{
           put("rate", String.valueOf(newVote));
           put("username", username);
        }};

        when(baloot.getCommodityById(any())).thenReturn(c);

        //exercise
        if (c.getUserRate().containsKey(username))
            expectedRate = (sumBeforeVote - c.getUserRate().get(username) + newVote)/(ratingCount);
        else
            expectedRate = (sumBeforeVote + newVote)/(ratingCount+1);

        c.addRate(username, newVote);
        float actualRate = c.getRating();


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        MvcResult result = mockMvc.perform(post("/commodities/{id}/rate", "1")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertEquals(expectedRate, actualRate);
        assertEquals("rate added successfully!", content);
        //teardown

    }

    @Test
    void WHEN_voting_on_invalid_commodity_id_THEN_get_no_existent_commodity() throws Exception {
        //setup
        String commodityId = "-1";
        Map<String, String> input = new HashMap<String, String>(){{
            put("rate", "0");
            put("username", "ryhn");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doThrow(new NotExistentCommodity()).when(baloot).getCommodityById(any());

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + RATE_API_URL;
        MvcResult result = mockMvc.perform(post(action, commodityId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        //verify

        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateCommodityRating")
    void WHEN_voting_wrong_on_commodity_THEN_get_number_format_exception(String vote) throws Exception {
        //setup
        String commodityId = "-1";
        Map<String, String> input = new HashMap<String, String>(){{
            put("rate", vote);
            put("username", "ryhn");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(input);

        doThrow(new NumberFormatException()).when(baloot).getCommodityById(any());

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + RATE_API_URL;
        MvcResult result = mockMvc.perform(post(action, commodityId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        //verify

        //teardown
    }

    @Test
    void WHEN_user_add_comment_THEN_commodity_comments_changes() throws Exception {
        //setup
        String commodityId = "1";
        String username = "ryhn";
        Map<String, String> input = new HashMap<String, String>() {{
            put("username", username);
            put("comment", "perfect for sure!");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(input);

        User u = new User(username, "1234", "ryhn@gmail.com", "2001-08-29", "teh");
        when(baloot.getUserById(any())).thenReturn(u);

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + COMMENT_API_URL;
        MvcResult result = mockMvc.perform(post(action, commodityId)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        //verify
        assertEquals(COMMENT_ADD_SUCCESSFULLY_FEEDBACK, content);
        //teardown

    }

    @Test
    void WHEN_commodity_has_comments_THEN_get_comment_list_successfully() throws Exception {
        //setup
        String commodityId = "1";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date temp = new Date();

        Comment c1 = new Comment(1, "userEmail@gmail.com", "username", 8101, "text");
        Comment c2 = new Comment(2, "userEmail@gmail.com", "username", 8101, "text");
        Comment c3 = new Comment(3, "userEmail@gmail.com", "username", 8101, "text");
        c1.setDate(dateFormat.format(temp));
        c2.setDate(dateFormat.format(temp));
        c3.setDate(dateFormat.format(temp));

        ArrayList<Comment> cms = new ArrayList<>(Arrays.asList(c1, c2, c3));

        when(baloot.getCommentsForCommodity(anyInt())).thenReturn(cms);

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + COMMENT_API_URL;
        MvcResult result = mockMvc.perform(get(action, commodityId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ArrayList<Comment> actual = objectMapper.readValue(content, new TypeReference<ArrayList<Comment>>() {});        //verify
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(cms);

        //teardown
    }

    @Test
    void WHEN_user_not_exist_in_adding_comment_THEN_get_not_existence_user() throws Exception {
        //setup
        String commodityId = "1";

        doThrow(new NotExistentUser()).when(baloot).getUserById(any());

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + COMMENT_API_URL;
        MvcResult result = mockMvc.perform(post(action, commodityId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        //verify

        //teardown
    }

    @Test
    void WHEN_search_category_THEN_same_category_commodities_return() throws Exception {
        //setup
        Map<String, String> input = new HashMap<String, String>() {{
            put("searchOption", "category");
            put("searchValue", "phone");
        }};
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(input);

        when(baloot.filterCommoditiesByName(any())).thenReturn(initCommodities);
        when(baloot.filterCommoditiesByCategory(any())).thenReturn(initCommodities);
        when(baloot.filterCommoditiesByProviderName(any())).thenReturn(initCommodities);

        //exercise
        String action = COMMODITIES_BASE_URL + SEARCH_API_URL;
        MvcResult result = mockMvc.perform(post(action)
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ArrayList<Commodity> actual = objectMapper.readValue(content, new TypeReference<ArrayList<Commodity>>() {});
        //verify
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(initCommodities);
        //teardown

    }

    @Test
    void WHEN_category_is_phone_THEN_suggest_phones() throws Exception {
        //setup
        String commodityId = "1";

        when(baloot.getCommodityById(any())).thenReturn(initCommodities.get(0));
        when(baloot.suggestSimilarCommodities(any())).thenReturn(new ArrayList<>(List.of(initCommodities.get(1))));

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + SUGGESTED_API_URL;
        MvcResult result = mockMvc.perform(get(action, commodityId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ArrayList<Commodity> actual = objectMapper.readValue(content, new TypeReference<ArrayList<Commodity>>() {});

        //verify
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(new ArrayList<>(List.of(initCommodities.get(1))));
        //teardown

    }

    @Test
    void WHEN_commodity_does_not_exist_in_suggestion_THEN_not_existence_commodity() throws Exception {
        //setup
        String commodityId = "1";

        doThrow(new NotExistentCommodity()).when(baloot).getCommodityById(any());
        when(baloot.suggestSimilarCommodities(any())).thenReturn(new ArrayList<>());

        //exercise
        String action = COMMODITIES_BASE_URL + ID_PATH_VARIABLE_URL + SUGGESTED_API_URL;
        MvcResult result = mockMvc.perform(get(action, commodityId)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ArrayList<Commodity> actual = objectMapper.readValue(content, new TypeReference<ArrayList<Commodity>>() {});

        //verify

        //teardown

    }

    private static Stream<Arguments> populateCommodityRatingScenario(){
        ratingCommodity.addRate("ryhn", 5);
        Commodity addFirstUser = ratingCommodity;
        ratingCommodity.addRate("mhya", 8);
        Commodity addSecUser = ratingCommodity;
        ratingCommodity.addRate("ryhn", 6);
        Commodity changeFirstUser = ratingCommodity;
        ratingCommodity.addRate("ryhnAp", 7);
        Commodity addThirdUser = ratingCommodity;
        ratingCommodity.addRate("mhya", 6);
        Commodity changeSecUser = ratingCommodity;
        ratingCommodity.addRate("mahya", 7);
        Commodity addFourthUser = ratingCommodity;
        return Stream.of(
                Arguments.of(addFirstUser, "ryhn", 5),
                Arguments.of(addSecUser, "mhya", 8),
                Arguments.of(changeFirstUser, "ryhn", 6),
                Arguments.of(addThirdUser, "ryhnAp", 7),
                Arguments.of(changeSecUser, "mhya", 6),
                Arguments.of(addFourthUser, "mahya", 7)
        );
    }

    private static Stream<Arguments> populateCommodityRating(){
        return Stream.of(
                Arguments.of("0"),
                Arguments.of("-1"),
                Arguments.of("11")
        );
    }
}