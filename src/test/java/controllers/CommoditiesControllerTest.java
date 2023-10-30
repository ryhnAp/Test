package controllers;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}