package domain;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Comment;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EngineTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File order1JsonFile = new File("src/test/resources/order1.json").getAbsoluteFile();
    private static final File order1_1JsonFile = new File("src/test/resources/order1-1.json").getAbsoluteFile();
    private static final File order2JsonFile = new File("src/test/resources/order2.json").getAbsoluteFile();
    private static final File order3JsonFile = new File("src/test/resources/order3.json").getAbsoluteFile();

    private static Engine engine;
    private static Order order1;
    private static Order order1_1;
    private static Order order2;
    private static Order order3;

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void tearDown() {

    }

    @BeforeAll
    public static void setup() {
        try {
            String initJsonOrder = FileUtils.readFileToString(order1JsonFile);
            order1 = gson.fromJson(initJsonOrder, (Type) Order.class);
            initJsonOrder = FileUtils.readFileToString(order1_1JsonFile);
            order1_1 = gson.fromJson(initJsonOrder, (Type) Order.class);
            initJsonOrder = FileUtils.readFileToString(order2JsonFile);
            order2 = gson.fromJson(initJsonOrder, (Type) Order.class);
            initJsonOrder = FileUtils.readFileToString(order3JsonFile);
            order3 = gson.fromJson(initJsonOrder, (Type) Order.class);
            engine = new Engine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void teardown() {

    }

    @ParameterizedTest
    @MethodSource("populateOrderHistoryScenario")
    void WHEN_get_quantity_pattern_by_price_THEN_return_valid_diff(Engine e, int price, int expected){
        //setup
        //exercise
        int actual = e.getQuantityPatternByPrice(price);
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @org.junit.jupiter.api.Test
    void WHEN_no_history_in_get_customer_fraudulent_quantity_THEN_return_order_quantity(){
        //setup
        int expected = order1.getQuantity();
        //exercise
        int actual = engine.getCustomerFraudulentQuantity(order1);
        //verify
        assertEquals(expected, actual);
        //teardown

    }

    @org.junit.jupiter.api.Test
    void WHEN_avg_history_in_get_customer_fraudulent_quantity_is_more_than_order_THEN_return_zero(){
        //setup
        Engine history = new Engine(); //diff(50) !=  currentOrder.quantity(100) - previous.quantity(150)
        history.orderHistory.add(order1);
        history.orderHistory.add(order1_1);
        history.orderHistory.add(order2);
        history.orderHistory.add(order3);
        history.orderHistory.add(order1);

        int expected = 0;
        //exercise
        int actual = history.getCustomerFraudulentQuantity(order1);
        //verify

        assertEquals(expected, actual);
        //teardown

    }

    private static Stream<Arguments> populateOrderHistoryScenario(){
        Engine orderHistorySize0 = new Engine();
        Engine orderHistoryGet0 = new Engine();
        orderHistoryGet0.orderHistory.add(order1);
        Engine orderHistoryGet0LoopContinueOnce = new Engine();
        orderHistoryGet0LoopContinueOnce.orderHistory.add(order1);
        orderHistoryGet0LoopContinueOnce.orderHistory.add(order1_1);
        Engine currentPriceNotMatchContinue = new Engine(); //curr = 150
        currentPriceNotMatchContinue.orderHistory.add(order1);
        currentPriceNotMatchContinue.orderHistory.add(order1_1);
        currentPriceNotMatchContinue.orderHistory.add(order2);
        Engine diff0ReturnNonZeroDiff = currentPriceNotMatchContinue;
        Engine diffNon0SubtractEqualReturnNonZeroDiff = new Engine(); //diff(50) ==  currentOrder.quantity(200) - previous.quantity(150)
        diffNon0SubtractEqualReturnNonZeroDiff.orderHistory.add(order1);
        diffNon0SubtractEqualReturnNonZeroDiff.orderHistory.add(order1_1);
        diffNon0SubtractEqualReturnNonZeroDiff.orderHistory.add(order2);
        diffNon0SubtractEqualReturnNonZeroDiff.orderHistory.add(order3);
        Engine diffNon0IsSubtractNotEqualReturnNonZeroDiff = new Engine(); //diff(50) !=  currentOrder.quantity(100) - previous.quantity(150)
        diffNon0IsSubtractNotEqualReturnNonZeroDiff.orderHistory.add(order1);
        diffNon0IsSubtractNotEqualReturnNonZeroDiff.orderHistory.add(order1_1);
        diffNon0IsSubtractNotEqualReturnNonZeroDiff.orderHistory.add(order2);
        diffNon0IsSubtractNotEqualReturnNonZeroDiff.orderHistory.add(order3);
        diffNon0IsSubtractNotEqualReturnNonZeroDiff.orderHistory.add(order1);
        return Stream.of(
                Arguments.of(orderHistorySize0, 100, 0),
                Arguments.of(orderHistoryGet0, 100, 0),
                Arguments.of(orderHistoryGet0LoopContinueOnce, 100, 0),
                Arguments.of(currentPriceNotMatchContinue, 50, 0),
                Arguments.of(diff0ReturnNonZeroDiff, 100, 50),
                Arguments.of(diffNon0SubtractEqualReturnNonZeroDiff, 100, 50),
                Arguments.of(diffNon0IsSubtractNotEqualReturnNonZeroDiff, 100, 0)

        );
    }
}