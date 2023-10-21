package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.NotInStock;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

class CommodityTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File commoditiesJsonFile = new File("src/test/resources/commodities.json").getAbsoluteFile();
    private static final File commodityJsonFile = new File("src/test/resources/commodity.json").getAbsoluteFile();
    private static final File ratingCommodityJsonFile = new File("src/test/resources/ratingCommodity.json").getAbsoluteFile();
    private static ArrayList<Commodity> initCommodities;
    private static Commodity tempCommodity;
    private static Commodity ratingCommodity;

    @BeforeEach
    public void setUp() {


    }

    @AfterEach
    public void tearDown() {

    }

    @BeforeAll
    public static void setup() {
        try {
            String initJsonCommodities = FileUtils.readFileToString(commoditiesJsonFile);
            initCommodities = gson.fromJson(initJsonCommodities, new TypeToken<ArrayList<Commodity>>() {}.getType());
            String initJsonCommodity = FileUtils.readFileToString(commodityJsonFile);
            tempCommodity = gson.fromJson(initJsonCommodity, (Type) Commodity.class);
            String initRatingJsonComment = FileUtils.readFileToString(ratingCommodityJsonFile);
            ratingCommodity = gson.fromJson(initRatingJsonComment, (Type) Commodity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void teardown() {

    }

    @Test
    void WHEN_amount_is_positive_THEN_increase_inStock() {
        //setup
        int count = 5;
        int expected = tempCommodity.getInStock() + count;
        try {
            //exercise
            tempCommodity.updateInStock(count);
            //verify
            assertEquals(expected, tempCommodity.getInStock());
            //teardown
            tempCommodity.updateInStock(-1 * count);
        } catch (NotInStock e) {
            e.printStackTrace();
        }
    }

    @Test
    void WHEN_amount_is_negative_and_no_item_remains_THEN_do_not_update_inStock() {
        //setup
        int count = 20;
        int expected = tempCommodity.getInStock() - count;
        //exercise
        //verify
        assertThrows(NotInStock.class, ()->tempCommodity.updateInStock(-1 * count));
        //teardown
    }

    @Test
    void WHEN_amount_is_negative_and_item_remains_THEN_update_inStock() {
        //setup
        int count = 5;
        int expected = tempCommodity.getInStock() - count;
        try {
            //exercise
            tempCommodity.updateInStock(-1 * count);
            //verify
            assertEquals(expected, tempCommodity.getInStock());
            //teardown
            tempCommodity.updateInStock(count);
        } catch (NotInStock e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @MethodSource("populateCommodityRatingScenario")
    void WHEN_new_or_prev_user_vote_THEN_respectively_add_change_rating(Commodity c, String username, int newVote) throws Exception{
        //setup
        int ratingCount = c.getUserRate().size() +1;
        float sumBeforeVote = c.getRating() * ratingCount;
        float expected = 0;
        //exercise
        if (c.getUserRate().containsKey(username))
            expected = (sumBeforeVote - c.getUserRate().get(username) + newVote)/(ratingCount);
        else
            expected = (sumBeforeVote + newVote)/(ratingCount+1);

        c.addRate(username, newVote);
        float actual = c.getRating();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_score_is_invalid_THEN_do_not_rate_commodity() {
        //setup
        int score = -5;
        float expected = tempCommodity.getRating();
        //exercise
        tempCommodity.addRate("ryhn", score);
        //verify
        assertEquals(expected, tempCommodity.getRating());
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_id_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        String expected = "81";
        String actual = c.getId();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_name_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        String expected = "Test";
        String actual = c.getName();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_provider_id_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        String expected = "98";
        String actual = c.getProviderId();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_price_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        int expected = 100;
        int actual = c.getPrice();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_categories_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        ArrayList<String> expected = new ArrayList<>(List.of("ca1", "ca2"));
        ArrayList<String> actual = c.getCategories();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_rating_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        float expected = 8;
        float actual = c.getRating();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_inStock_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        int expected = 10;
        int actual = c.getInStock();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_img_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        //exercise
        String expected = "";
        String actual = c.getImage();
        //verify
        assertEquals(expected, actual);
        //teardown
    }


    @ParameterizedTest
    @MethodSource("populateCommodityRatingScenario")
    void WHEN_constructor_created_commodity_THEN_default_user_rate_return_successfully(Commodity c, String username, int newVote) {
        assumingThat(username.equals("mahya"), ()->assertEquals(c.getUserRate(), ratingCommodity.getUserRate()));
//        assumeTrue(username.equals("mahya") && (7 == newVote));
        //setup
        //exercise
        //verify
//        assertEquals(c.getUserRate(), ratingCommodity.getUserRate());
        //teardown
    }

    @Test
    void WHEN_constructor_created_commodity_THEN_default_init_rate_return_successfully() {
        //setup
        Commodity c = getTempCommodityGetter();
        float expected = c.getRating();
        c.addRate("ryhn", 8);
        //exercise
        float actual = c.getInitRate();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_commodity_list_ids_change_THEN_ids_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            String replace = "494";
            //exercise
            String unexpected = c.getId();
            c.setId(replace);
            String actual = c.getId();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setId(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_names_change_THEN_names_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            String replace = "Test";
            //exercise
            String unexpected = c.getName();
            c.setName(replace);
            String actual = c.getName();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setName(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_provider_ids_change_THEN_provider_ids_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            String replace = "98";
            //exercise
            String unexpected = c.getProviderId();
            c.setProviderId(replace);
            String actual = c.getProviderId();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setProviderId(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_prices_change_THEN_prices_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            int replace = 150;
            //exercise
            int unexpected = c.getPrice();
            c.setPrice(replace);
            int actual = c.getPrice();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setPrice(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_categories_change_THEN_categories_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            ArrayList<String> replace = new ArrayList<>(List.of("ca1", "ca2"));
            //exercise
            ArrayList<String> unexpected = c.getCategories();
            c.setCategories(replace);
            ArrayList<String> actual = c.getCategories();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setCategories(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_ratings_change_THEN_ratings_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            float replace = 7;
            //exercise
            float unexpected = c.getRating();
            c.setRating(replace);
            float actual = c.getRating();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setRating(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_inStocks_change_THEN_inStocks_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            int replace = 250;
            //exercise
            int unexpected = c.getInStock();
            c.setInStock(replace);
            int actual = c.getInStock();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setInStock(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_images_change_THEN_images_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            String replace = "img";
            //exercise
            String unexpected = c.getImage();
            c.setImage(replace);
            String actual = c.getImage();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setImage(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_userRate_change_THEN_userRate_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            //exercise
            Map<String, Integer> unexpected = c.getUserRate();
            c.setUserRate(ratingCommodity.getUserRate());
            Map<String, Integer> actual = c.getUserRate();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setUserRate(unexpected);
        }
    }

    @Test
    void WHEN_commodity_list_initRate_change_THEN_initRate_must_be_different() {
        for (Commodity c : initCommodities){
            //setup
            //exercise
            float unexpected = c.getInitRate();
            c.setInitRate(c.getRating());
            float actual = c.getInitRate();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setInitRate(unexpected);
        }
    }

    private Commodity getTempCommodityGetter(){
        Commodity c = new Commodity();
        c.setId("81");
        c.setName("Test");
        c.setProviderId("98");
        c.setPrice(100);
        c.setCategories(new ArrayList<String>(List.of("ca1", "ca2")));
        c.setRating(8);
        c.setInStock(10);
        c.setImage("");
        return c;
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