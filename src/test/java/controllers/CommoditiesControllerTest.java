package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import exceptions.NotExistentCommodity;
import model.Commodity;
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
    private static String initJsonCommodities;
    private static final File commodityJsonFile = new File("src/test/resources/commodity.json").getAbsoluteFile();
    private static final File ratingCommodityJsonFile = new File("src/test/resources/ratingCommodity.json").getAbsoluteFile();
    private static ArrayList<Commodity> initCommodities;
    private static Commodity ratingCommodity;

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
    void WHEN_json_commodities_list_set_THEN_get_commodities_must_give_same_object() throws Exception {
        //setup
        when(baloot.getCommodities()).thenReturn(initCommodities);

        //exercise
        MvcResult result = mockMvc.perform(get("/commodities")
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
    void WHEN_json_commodity_set_THEN_get_single_commodity_must_give_same_object() throws Exception {
        //setup
        when(baloot.getCommodityById(any())).thenReturn(initCommodities.get(0));

        //exercise
        MvcResult result = mockMvc.perform(get("/commodities/1", "1")
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

        Commodity cMock = Mockito.mock(Commodity.class);
//        doNothing().when(cMock).addRate(username, newVote);
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
    void getCommodity() {
    }

    @Test
    void rateCommodity() {
    }

    @Test
    void addCommodityComment() {
    }

    @Test
    void getCommodityComment() {
    }

    @Test
    void searchCommodities() {
    }

    @Test
    void getSuggestedCommodities() {
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
}