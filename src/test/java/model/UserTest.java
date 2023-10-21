package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.CommodityIsNotInBuyList;
import exceptions.InsufficientCredit;
import exceptions.InvalidCreditRange;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File commoditiesJsonFile = new File("src/test/resources/commodities.json").getAbsoluteFile();
    private static ArrayList<Commodity> initCommodities;

    private static final File usersJsonFile = new File("src/test/resources/users.json").getAbsoluteFile();
    private static ArrayList<User> initUsers;
    private static User tempUser;


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
            String initJsonUsers = FileUtils.readFileToString(usersJsonFile);
            initUsers = gson.fromJson(initJsonUsers, new TypeToken<ArrayList<User>>() {}.getType());
//            String initJsonCommodity = FileUtils.readFileToString(commodityJsonFile);
//            tempUser = gson.fromJson(initJsonCommodity, (Type) User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void teardown() {

    }

    @Test
    void WHEN_amount_is_positive_THEN_increase_wallet() {
        //setup
        float amount = 20;
        User u = getTempUserGetter();
        float expected = u.getCredit() + amount;
        try {
            //exercise
            u.addCredit(amount);
            //verify
            assertEquals(expected, u.getCredit());
            //teardown
        } catch (InvalidCreditRange e) {
            e.printStackTrace();
        }
    }

    @Test
    void WHEN_amount_is_negative_and_no_item_remains_THEN_do_not_update_inStock() {
        //setup
        float amount = -5;
        User u = getTempUserGetter();
        //exercise
        //verify
        assertThrows(InvalidCreditRange.class, ()->u.addCredit(amount));
        //teardown
    }

    @Test
    void WHEN_amount_is_less_than_wallet_size_THEN_money_is_enough_for_purchase() {
        //setup
        User u = getTempUserGetter();
        float amount = u.getCredit() - u.getCredit()/10;
        float expected = u.getCredit() - amount;
        try {
            //exercise
            u.withdrawCredit(amount);
            //verify
            assertEquals(expected, u.getCredit());
            //teardown
        } catch (InsufficientCredit e) {
            e.printStackTrace();
        }
    }

    @Test
    void WHEN_amount_is_more_than_wallet_size_THEN_money_is_not_enough_for_purchase() {
        //setup
        User u = getTempUserGetter();
        float amount = u.getCredit() + 20;
        //exercise
        //verify
        assertThrows(InsufficientCredit.class, ()->u.withdrawCredit(amount));
        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateCommodityUserBasketScenario")
    void WHEN_same_item_add_THEN_increase_item_count_in_buy_list(Commodity c, User u) {
        //setup
        int expected = 1;
        //exercise
        if(u.getBuyList().containsKey(c.getId()))
            expected = u.getBuyList().get(c.getId()) + 1;
        u.addBuyItem(c);
        int actual = u.getBuyList().get(c.getId());
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @ParameterizedTest
    @MethodSource("populatePurchaseBasket")
    void WHEN_same_item_add_THEN_increase_item_count_in_purchase_list(Commodity c, int quantity, User u) {
        //setup
        int expected = quantity;
        //exercise
        if(u.getPurchasedList().containsKey(c.getId()))
            expected = u.getPurchasedList().get(c.getId()) + quantity;
        u.addPurchasedItem(c.getId(), quantity);
        int actual = u.getPurchasedList().get(c.getId());
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateRemovalBuyList")
    void WHEN_item_exist_THEN_remove_and_count_decrease(Commodity c, User u) {
        //setup
        int expected = 0, actual = 0;
        //exercise
        if(u.getBuyList().containsKey(c.getId()))
            expected = u.getBuyList().get(c.getId()) - 1;
        try {
            u.removeItemFromBuyList(c);
        } catch (CommodityIsNotInBuyList e) {
            e.printStackTrace();
        }
        if(u.getBuyList().containsKey(c.getId()))
            actual = u.getBuyList().get(c.getId());
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_buy_list_is_empty_THEN_can_not_remove_item_from_it() {
        //setup
        User u = getTempUserGetter();
        //exercise
        //verify
        assertThrows(CommodityIsNotInBuyList.class, ()->u.removeItemFromBuyList(initCommodities.get(0)));
        //teardown
    }

    @Test
    void WHEN_constructor_created_THEN_default_username_return_successfully() {
        //setup
        User u = getTempUserGetter();
        //exercise
        String expected = "ryhn";
        String actual = u.getUsername();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_THEN_default_password_return_successfully() {
        //setup
        User u = getTempUserGetter();
        //exercise
        String expected = "1234";
        String actual = u.getPassword();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_THEN_default_mail_return_successfully() {
        //setup
        User u = getTempUserGetter();
        //exercise
        String expected = "ryhnap@gmail.com";
        String actual = u.getEmail();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_THEN_default_birth_return_successfully() {
        //setup
        User u = getTempUserGetter();
        //exercise
        String expected = "2001-08-29";
        String actual = u.getBirthDate();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_THEN_default_address_return_successfully() {
        //setup
        User u = getTempUserGetter();
        //exercise
        String expected = "Teh";
        String actual = u.getAddress();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_constructor_created_THEN_default_credit_return_successfully() {
        //setup
        User u = getTempUserGetter();
        //exercise
        float expected = 0;
        float actual = u.getCredit();
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @Test
    void WHEN_user_list_username_change_THEN_usernames_must_be_different() {
        for (User c : initUsers){
            //setup
            String replace = "ryhnap";
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
    void WHEN_user_list_password_change_THEN_passwords_must_be_different() {
        for (User c : initUsers){
            //setup
            String replace = "4321";
            //exercise
            String unexpected = c.getPassword();
            c.setPassword(replace);
            String actual = c.getPassword();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setPassword(unexpected);
        }
    }

    @Test
    void WHEN_user_list_mail_change_THEN_mails_must_be_different() {
        for (User c : initUsers){
            //setup
            String replace = "r@gmail.com";
            //exercise
            String unexpected = c.getEmail();
            c.setEmail(replace);
            String actual = c.getEmail();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setEmail(unexpected);
        }
    }

    @Test
    void WHEN_user_list_birth_change_THEN_births_must_be_different() {
        for (User c : initUsers){
            //setup
            String replace = "2000-8-29";
            //exercise
            String unexpected = c.getBirthDate();
            c.setBirthDate(replace);
            String actual = c.getBirthDate();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setBirthDate(unexpected);
        }
    }

    @Test
    void WHEN_user_list_address_change_THEN_address_must_be_different() {
        for (User c : initUsers){
            //setup
            String replace = "Tehran";
            //exercise
            String unexpected = c.getAddress();
            c.setAddress(replace);
            String actual = c.getAddress();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setAddress(unexpected);
        }
    }

    @Test
    void WHEN_user_list_credit_change_THEN_credits_must_be_different() {
        for (User c : initUsers){
            //setup
            float replace = 200;
            //exercise
            float unexpected = c.getCredit();
            c.setCredit(replace);
            float actual = c.getCredit();
            //verify
            assertNotEquals(unexpected, actual);
            //teardown
            c.setCredit(unexpected);
        }
    }

    private User getTempUserGetter() {
        User user = new User("ryhn", "1234", "ryhnap@gmail.com", "2001-08-29", "Teh");
        return user;
    }

    private static Stream<Arguments> populateCommodityUserBasketScenario(){
        User user = new User("ryhn", "1234", "ryhnap@gmail.com", "2001-08-29", "Teh");
        Commodity c = initCommodities.get(0);
        Commodity c_ = initCommodities.get(1);
        return Stream.of(
                Arguments.of(c, user),
                Arguments.of(c, user),
                Arguments.of(c_, user),
                Arguments.of(c_, user),
                Arguments.of(c, user)
        );
    }

    private static Stream<Arguments> populatePurchaseBasket(){
        User user = new User("ryhn", "1234", "ryhnap@gmail.com", "2001-08-29", "Teh");
        Commodity c = initCommodities.get(0);
        Commodity c_ = initCommodities.get(1);
        return Stream.of(
                Arguments.of(c, 2, user),
                Arguments.of(c, 3, user),
                Arguments.of(c_, 1, user),
                Arguments.of(c_, 3, user),
                Arguments.of(c, 1, user)
        );
    }

    private static Stream<Arguments> populateRemovalBuyList(){
        User user = new User("ryhn", "1234", "ryhnap@gmail.com", "2001-08-29", "Teh");
        Commodity c = initCommodities.get(0);
        Commodity c_ = initCommodities.get(1);
        user.addBuyItem(c);
        user.addBuyItem(c_);
        user.addBuyItem(c);
        user.addBuyItem(c_);
        user.addBuyItem(c);
        return Stream.of(
                Arguments.of(c, user),
                Arguments.of(c_, user),
                Arguments.of(c, user),
                Arguments.of(c_, user),
                Arguments.of(c, user)
        );
    }
}