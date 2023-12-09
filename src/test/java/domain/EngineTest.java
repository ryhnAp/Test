package domain;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Comment;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
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
import java.util.*;
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


    @org.junit.jupiter.api.Test
    void GIVEN_no_orders_WHEN_get_average_order_quantity_by_customer_THEN_return_zero() {
        // setup
        Engine e = new Engine();

        // exercise
        int averageQuantity = e.getAverageOrderQuantityByCustomer(1);

        // verify
        assertEquals(0, averageQuantity);
    }

    @ParameterizedTest
    @MethodSource("provideOrderHistory")
    void GIVEN_order_history_WHEN_get_average_order_quantity_by_customer_THEN_return_correct_average(Engine engine, int customer, double expectedAverage) {
        // setup
        // exercise
        int averageQuantity = engine.getAverageOrderQuantityByCustomer(customer);
        // verify
        assertEquals(expectedAverage, averageQuantity);
    }

    @ParameterizedTest
    @MethodSource("populateOrderHistoryScenario")
    void WHEN_get_quantity_pattern_by_price_THEN_return_valid_diff(Engine e, int price, int expected) {
        //setup
        //exercise
        int actual = e.getQuantityPatternByPrice(price);
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    @org.junit.jupiter.api.Test
    void GIVEN_no_history_order_quantity_0_WHEN_get_customer_fraudulent_quantity_THEN_return_order_quantity() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());
        orderWith0Quantity.setQuantity(0);
        int expected = orderWith0Quantity.getQuantity();
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_no_history_order_quantity_1_WHEN_get_customer_fraudulent_quantity_THEN_return_order_quantity() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());
        orderWith0Quantity.setQuantity(1);
        int expected = orderWith0Quantity.getQuantity();
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_no_history_negative_order_quantity_1_WHEN_get_customer_fraudulent_quantity_THEN_return_zero() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());
        orderWith0Quantity.setQuantity(-1);
        int expected = 0;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_negative_quantity_history_order_quantity_0_WHEN_get_customer_fraudulent_quantity_THEN_return_diff_quantity() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());

        Order orderWithNegQuantity = new Order();
        orderWithNegQuantity.setId(order1.getId());
        orderWithNegQuantity.setCustomer(order1.getCustomer());
        orderWithNegQuantity.setPrice(order1.getPrice());
        orderWithNegQuantity.setQuantity(-1);
        e.orderHistory.add(orderWithNegQuantity);
        orderWith0Quantity.setQuantity(0);

        int expected = 1;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_negative_quantity_history_negative_order_quantity_2_WHEN_get_customer_fraudulent_quantity_THEN_return_zero() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());

        Order orderWithNegQuantity = new Order();
        orderWithNegQuantity.setId(order1.getId());
        orderWithNegQuantity.setCustomer(order1.getCustomer());
        orderWithNegQuantity.setPrice(order1.getPrice());
        orderWithNegQuantity.setQuantity(-1);
        e.orderHistory.add(orderWithNegQuantity);
        orderWith0Quantity.setQuantity(-2);

        int expected = 0;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_negative_quantity_history_negative_order_quantity_1_WHEN_get_customer_fraudulent_quantity_THEN_return_zero() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());

        Order orderWithNegQuantity = new Order();
        orderWithNegQuantity.setId(order1.getId());
        orderWithNegQuantity.setCustomer(order1.getCustomer());
        orderWithNegQuantity.setPrice(order1.getPrice());
        orderWithNegQuantity.setQuantity(-1);
        e.orderHistory.add(orderWithNegQuantity);
        orderWith0Quantity.setQuantity(-1);

        int expected = 0;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_negative_quantity_history_order_quantity_1_WHEN_get_customer_fraudulent_quantity_THEN_return_diff_quantity() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());

        Order orderWithNegQuantity = new Order();
        orderWithNegQuantity.setId(order1.getId());
        orderWithNegQuantity.setCustomer(order1.getCustomer());
        orderWithNegQuantity.setPrice(order1.getPrice());
        orderWithNegQuantity.setQuantity(-1);
        e.orderHistory.add(orderWithNegQuantity);
        orderWith0Quantity.setQuantity(1);

        int expected = 2;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_one_quantity_history_order_quantity_0_WHEN_get_customer_fraudulent_quantity_THEN_return_zero() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());

        Order orderWithNegQuantity = new Order();
        orderWithNegQuantity.setId(order1.getId());
        orderWithNegQuantity.setCustomer(order1.getCustomer());
        orderWithNegQuantity.setPrice(order1.getPrice());
        orderWithNegQuantity.setQuantity(1);
        e.orderHistory.add(orderWithNegQuantity);
        orderWith0Quantity.setQuantity(0);

        int expected = 0;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_one_quantity_history_order_quantity_1_WHEN_get_customer_fraudulent_quantity_THEN_return_zero() {
        //setup
        Engine e = new Engine();
        Order orderWith0Quantity = new Order();
        orderWith0Quantity.setId(order1.getId());
        orderWith0Quantity.setCustomer(order1.getCustomer());
        orderWith0Quantity.setPrice(order1.getPrice());

        Order orderWithNegQuantity = new Order();
        orderWithNegQuantity.setId(order1.getId());
        orderWithNegQuantity.setCustomer(order1.getCustomer());
        orderWithNegQuantity.setPrice(order1.getPrice());
        orderWithNegQuantity.setQuantity(1);
        e.orderHistory.add(orderWithNegQuantity);
        orderWith0Quantity.setQuantity(1);

        int expected = 0;
        int actual = e.getCustomerFraudulentQuantity(orderWith0Quantity);
        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void GIVEN_no_history_WHEN_get_customer_fraudulent_quantity_THEN_return_order_quantity() {
        //setup
        Engine e = new Engine();
        int expected = order1.getQuantity();

        //exercise
        int actual = e.getCustomerFraudulentQuantity(order1);
        //verify
        assertEquals(expected, actual);
        //teardown

    }

    Engine setNegativeQuantity(Engine e){
        Engine negHistory = new Engine();
        for (int i=0; i<e.orderHistory.size(); i++){
            Order o = new Order();
            o.setId(e.orderHistory.get(i).getId());
            o.setCustomer(e.orderHistory.get(i).getCustomer());
            o.setPrice(e.orderHistory.get(i).getPrice());
            o.setQuantity(-1*e.orderHistory.get(i).getQuantity());
            negHistory.orderHistory.add(o);
        }
        return negHistory;
    }

    @ParameterizedTest
    @MethodSource("populateFraudulentQuantity")
    void GIVEN_diff_history_plus1_scenario_WHEN_get_customer_fraudulent_quantity_THEN_returns_related_quantity(Engine e, Order o) {
        //setup
        //exercise
        Engine ne = setNegativeQuantity(e);
        int funVal = ne.getAverageOrderQuantityByCustomer(o.getCustomer());
        o.setQuantity(funVal+1);
        int expected_ = o.getQuantity() - funVal;
        int actual = ne.getCustomerFraudulentQuantity(o);
        //verify
        assertEquals(expected_, actual);
        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateFraudulentQuantity")
    void GIVEN_diff_history_scenario_WHEN_get_customer_fraudulent_quantity_THEN_returns_related_quantity(Engine e, Order o) {
        //setup
        //exercise
        Engine ne = setNegativeQuantity(e);
        int funVal = ne.getAverageOrderQuantityByCustomer(o.getCustomer());
        o.setQuantity(funVal);
        int expected_ = o.getQuantity() - funVal;
        int actual = ne.getCustomerFraudulentQuantity(o);
        //verify
        assertEquals(expected_, actual);
        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateFraudulentQuantity")
    void GIVEN_diff_history_minus1_scenario_WHEN_get_customer_fraudulent_quantity_THEN_returns_related_quantity(Engine e, Order o) {
        //setup
        //exercise
        Engine ne = setNegativeQuantity(e);
        o.setQuantity(ne.getAverageOrderQuantityByCustomer(o.getCustomer())-1);
        int expected_ = 0;
        int actual = ne.getCustomerFraudulentQuantity(o);
        //verify
        assertEquals(expected_, actual);
        //teardown
    }

    @org.junit.jupiter.api.Test
    void GIVEN_avg_history_more_than_order_quantity_WHEN_get_customer_fraudulent_quantity_THEN_return_zero() {
        //setup
        Engine history = new Engine();
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

    @org.junit.jupiter.api.Test
    void GIVEN_fraudulent_order_WHEN_add_order_and_get_fraudulent_quantity_THEN_returns_fraudulent_quantity() {
        // setup
        Engine engine = new Engine();

        // Add orders to the engine

        Order order1 = new Order();
        order1.setId(1);
        order1.setCustomer(1);
        order1.setPrice(100);
        order1.setQuantity(5);
        engine.orderHistory.add(order1);

        Order order2 = new Order();
        order2.setId(2);
        order2.setCustomer(1);
        order2.setPrice(100);
        order2.setQuantity(7);
        engine.orderHistory.add(order2);

        Order order3 = new Order();
        order3.setId(3);
        order3.setCustomer(2);
        order3.setPrice(200);
        order3.setQuantity(10);
        engine.orderHistory.add(order3);

        // Create a fraudulent order
        Order fraudulentOrder = new Order();
        fraudulentOrder.setId(4);
        fraudulentOrder.setCustomer(1);
        fraudulentOrder.setPrice(100);
        fraudulentOrder.setQuantity(15);
        // exercise
        int fraudulentQuantity = engine.addOrderAndGetFraudulentQuantity(fraudulentOrder);

        // verify
        assertEquals(9, fraudulentQuantity);
    }

    @ParameterizedTest
    @MethodSource("provideOrderHistoryLast")
    void GIVEN_fraudulent_order_WHEN_add_order_get_fraudulent_quantity_THEN_returns_fraudulent_quantity(List<Order> orderHistory, Order fraudulentOrder, int expectedFraudulentQuantity) {
        // setup
        engine.orderHistory.addAll(orderHistory);

        // exercise
        int fraudulentQuantity = engine.addOrderAndGetFraudulentQuantity(fraudulentOrder);

        // verify
        assertEquals(expectedFraudulentQuantity, fraudulentQuantity);
    }

    @ParameterizedTest
    @MethodSource("populateOrderFraudulentQuantity")
    void GIVEN_quantity0_scenario_WHEN_get_fraudulent_quantity_THEN_returns_related_quantity(Engine e, Order o, int expected) {
        //setup
        //exercise
        int actual = e.addOrderAndGetFraudulentQuantity(o);
        //verify
        assertEquals(expected, actual);
        //teardown
    }

    private static Order createOrder(int id, int customer, int quantity) {
        Order order = new Order();
        order.setId(id);
        order.setCustomer(customer);
        order.setQuantity(quantity);
        return order;
    }

    private static Engine createEngineWithOrders() {
        Engine engine = new Engine();

        // Add orders to the orderHistory list
        List<Order> orderHistory = engine.orderHistory;

        orderHistory.add(createOrder(1, 1, 5));
        orderHistory.add(createOrder(2, 1, 7));
        orderHistory.add(createOrder(3, 2, 10));

        return engine;
    }

    private static Stream<Arguments> provideOrderHistory() {
        Engine engineWithOrders = createEngineWithOrders();

        return Stream.of(
                Arguments.of(engineWithOrders, 1, 6),
                Arguments.of(engineWithOrders, 2, 10)
        );
    }

    private static Stream<Arguments> populateOrderHistoryScenario() {
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

    private static List<Object[]> provideOrderHistoryLast() {
        List<Object[]> testCases = new ArrayList<>();

        // Test Case 1: No order history
        List<Order> orderHistory1 = new ArrayList<>();
        Order fraudulentOrder1 = new Order();
        fraudulentOrder1.setId(4);
        fraudulentOrder1.setCustomer(1);
        fraudulentOrder1.setPrice(100);
        fraudulentOrder1.setQuantity(15);
        int expectedFraudulentQuantity1 = 15;
        testCases.add(new Object[]{orderHistory1, fraudulentOrder1, expectedFraudulentQuantity1});

        // Test Case 2: Order history with valid orders
        List<Order> orderHistory2 = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1);
        order1.setCustomer(1);
        order1.setPrice(100);
        order1.setQuantity(5);
        orderHistory2.add(order1);

        Order order2 = new Order();
        order2.setId(2);
        order2.setCustomer(1);
        order2.setPrice(100);
        order2.setQuantity(7);
        orderHistory2.add(order2);

        Order order3 = new Order();
        order3.setId(3);
        order3.setCustomer(2);
        order3.setPrice(200);
        order3.setQuantity(10);
        orderHistory2.add(order3);

        Order fraudulentOrder2 = new Order();
        fraudulentOrder2.setId(4);
        fraudulentOrder2.setCustomer(1);
        fraudulentOrder2.setPrice(100);
        fraudulentOrder2.setQuantity(15);
        int expectedFraudulentQuantity2 = 0;
        testCases.add(new Object[]{orderHistory2, fraudulentOrder2, expectedFraudulentQuantity2});

        return testCases;
    }

    private static Stream<Arguments> populateOrderFraudulentQuantity() {
        Order o = new Order();
        o.setId(4);
        o.setCustomer(1);
        o.setPrice(100);
        o.setQuantity(100);
        Engine equalNewHistoryQuantity = new Engine();
        equalNewHistoryQuantity.orderHistory.add(order1);
        equalNewHistoryQuantity.orderHistory.add(order1);
        Engine avgLessQuantity = new Engine();
        avgLessQuantity.orderHistory.add(order1);
        avgLessQuantity.orderHistory.add(order1);
        avgLessQuantity.orderHistory.add(order1);
        avgLessQuantity.orderHistory.add(order1);
        Engine avgMoreQuantity = new Engine();
        avgMoreQuantity.orderHistory.add(order1);
        avgMoreQuantity.orderHistory.add(order1_1);
        avgMoreQuantity.orderHistory.add(order2);
        avgMoreQuantity.orderHistory.add(order3);
        avgMoreQuantity.orderHistory.add(order1);
        return Stream.of(
                Arguments.of(equalNewHistoryQuantity, o, 0),
                Arguments.of(avgLessQuantity, o, 0),
                Arguments.of(avgMoreQuantity, o, 0)
        );
    }

    private static Stream<Arguments> populateFraudulentQuantity() {
        Order o = new Order();
        o.setId(4);
        o.setCustomer(1);
        o.setPrice(100);
        o.setQuantity(-100);
        Engine equalNewHistoryQuantity = new Engine();
        equalNewHistoryQuantity.orderHistory.add(order1);
        equalNewHistoryQuantity.orderHistory.add(order1);
        Engine avgLessQuantity = new Engine();
        avgLessQuantity.orderHistory.add(order1);
        avgLessQuantity.orderHistory.add(order1);
        avgLessQuantity.orderHistory.add(order1);
        avgLessQuantity.orderHistory.add(order1);
        Engine avgMoreQuantity = new Engine();
        avgMoreQuantity.orderHistory.add(order1);
        avgMoreQuantity.orderHistory.add(order1_1);
        avgMoreQuantity.orderHistory.add(order2);
        avgMoreQuantity.orderHistory.add(order3);
        avgMoreQuantity.orderHistory.add(order1);
        return Stream.of(
                Arguments.of(equalNewHistoryQuantity, o),
                Arguments.of(avgLessQuantity, o),
                Arguments.of(avgMoreQuantity, o)
        );
    }
}