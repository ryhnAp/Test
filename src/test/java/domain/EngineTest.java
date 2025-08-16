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

import static org.junit.jupiter.api.Assertions.*;

public class EngineTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File order1JsonFile = new File("src/test/resources/order1.json").getAbsoluteFile();
    private static final File order1_1JsonFile = new File("src/test/resources/order1-1.json").getAbsoluteFile();
    private static final File order2JsonFile = new File("src/test/resources/order2.json").getAbsoluteFile();
    private static final File order3JsonFile = new File("src/test/resources/order3.json").getAbsoluteFile();

    private Engine engineM;
    private Order order1M;
    private List<Order> orderHistoryM;


    private static Engine engine;
    private static Order order1;
    private static Order order1_1;
    private static Order order2;
    private static Order order3;

    @BeforeEach
    public void setUp() {
        engineM = new Engine();
        order1M = new Order();
        // Set up orderHistory as needed
        orderHistoryM = new ArrayList<>();
        // Add orderHistory data if required for testing average calculation
        // e.g., orderHistory.add(someOrder);
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

    @org.junit.jupiter.api.Test
    void given_empty_order_history_when_get_customer_average_quantity_then_return_zero() {
        // Set orderHistory as empty
        orderHistoryM.clear();
        int actual = engineM.getAverageOrderQuantityByCustomer(order1M.getCustomer());
        assertEquals(0, actual);
    }

    @org.junit.jupiter.api.Test
    void given_order_quantity_greater_than_average_when_get_customer_fraudulent_quantity_then_return_difference() {
        // Set up order with quantity greater than average
        Order order = new Order();
        order.setCustomer(order1M.getCustomer());
        order.setQuantity(10); // Set quantity greater than any calculated average
        // Add necessary setup for order (ID, price, etc.)

        int actual = engineM.getCustomerFraudulentQuantity(order);
        assertEquals(order.getQuantity() - engineM.getAverageOrderQuantityByCustomer(order.getCustomer()), actual);
    }

    @org.junit.jupiter.api.Test
    void given_order_quantity_equal_to_average_when_get_customer_fraudulent_quantity_then_exception() {
        // Set up order with quantity equal to average
        int customer = order1M.getCustomer();
        int average = 5; // Set an average value for testing
        // Add an order to history with quantity equal to the average for the specific customer
        Order order = new Order();
        order.setCustomer(customer);
        order.setQuantity(average);
        orderHistoryM.add(order);

//        int actual = engine.getCustomerFraudulentQuantity(order);
        assertThrows(ArithmeticException.class, ()->engine.getCustomerFraudulentQuantity(order));
//        assertEquals(0, actual);
    }

    @org.junit.jupiter.api.Test
    void given_order_quantity_equal_to_average_when_get_customer_fraudulent_quantity_then_return_zero() {
        // Set up order with quantity equal to average
        int customer = order1M.getCustomer();
        int average = 5; // Set an average value for testing
        // Add an order to history with quantity equal to the average for the specific customer
        Order order = new Order();
        order.setCustomer(customer);
        order.setQuantity(average);
        order1M.setCustomer(customer);
        order1M.setQuantity(average);
        engineM.orderHistory.add(order);

        int actual = engineM.getCustomerFraudulentQuantity(order1M);
        assertEquals(0, actual);
    }

    @org.junit.jupiter.api.Test
    void given_order_quantity_less_than_average_when_get_customer_fraudulent_quantity_then_return_zero() {
        // Set up order with quantity less than average
        int customer = order1M.getCustomer();
        int average = 10; // Set an average value for testing
        // Add an order to history with quantity less than the average for the specific customer
        Order order = new Order();
        order.setCustomer(customer);
        order.setQuantity(average); // Set quantity less than the average for this customer
        order1M.setCustomer(customer);
        order1M.setQuantity(average-2);
        engineM.orderHistory.add(order);

        int actual = engineM.getCustomerFraudulentQuantity(order1M);
        assertEquals(0, actual);
    }

    @ParameterizedTest
    @MethodSource("populateChangeMutation")
    void GIVEN_nine_scenario_3_pos_base_neg_times_WHEN_add_order_and_get_THEN_returns_expected(Engine e, Order o, int expected) {
        //setup
        //exercise
        int val;
        if (expected == 0)
            val = e.getQuantityPatternByPrice(o.getPrice());
        else
            val = expected;
        int actual = e.addOrderAndGetFraudulentQuantity(o);
        //verify
        assertEquals(val, actual);
        //teardown
    }

    @ParameterizedTest
    @MethodSource("populateChangeMutation")
    void GIVEN_nine_scenario_3_pos_base_neg_times_WHEN_get_customer_fraudulent_quantity_THEN_returns_expected(Engine e, Order o, int expected) {
        //setup
        //exercise
        int actual = e.getCustomerFraudulentQuantity(o);
        //verify
        assertEquals(expected, actual);
        //teardown
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
        int funVal = ne.getAverageOrderQuantityByCustomer(o.getCustomer());
        o.setQuantity(funVal-1);
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

    private static Stream<Arguments> populateChangeMutation() {

        Engine avg0 = new Engine();
        Engine avg0_ = new Engine();
        Engine avg0__ = new Engine();
        Engine avg1 = new Engine();
        Engine avg1_ = new Engine();
        Engine avg1__ = new Engine();
        Engine avgNeg1 = new Engine();
        Engine avgNeg1_ = new Engine();
        Engine avgNeg1__ = new Engine();
        Order plus1 = new Order();
        plus1.setId(10);
        plus1.setCustomer(1);
        plus1.setPrice(100);
        plus1.setQuantity(1);
        Order plus1_ = new Order();
        plus1_.setId(100);
        plus1_.setCustomer(1);
        plus1_.setPrice(100);
        plus1_.setQuantity(1);
        avg1.orderHistory.add(plus1);
        avg1_.orderHistory.add(plus1);
        avg1__.orderHistory.add(plus1);

        Order minus1 = new Order();
        minus1.setId(11);
        minus1.setCustomer(1);
        minus1.setPrice(100);
        minus1.setQuantity(-1);
        Order minus1_ = new Order();
        minus1_.setId(111);
        minus1_.setCustomer(1);
        minus1_.setPrice(100);
        minus1_.setQuantity(-1);
        avgNeg1.orderHistory.add(minus1);
        avgNeg1_.orderHistory.add(minus1);
        avgNeg1__.orderHistory.add(minus1);

        Order eq0 = new Order();
        eq0.setId(12);
        eq0.setCustomer(1);
        eq0.setPrice(100);
        eq0.setQuantity(0);
        Order eq0_ = new Order();
        eq0_.setId(12);
        eq0_.setCustomer(1);
        eq0_.setPrice(100);
        eq0_.setQuantity(0);

        return Stream.of(
                Arguments.of(avg0, plus1, 1),
                Arguments.of(avg0_, minus1, 0),
                Arguments.of(avg0__, eq0_, 0),
                Arguments.of(avg1, plus1_, 0),
                Arguments.of(avg1_, minus1, 0),
                Arguments.of(avg1__, eq0, 0),
                Arguments.of(avgNeg1, plus1, 2),
                Arguments.of(avgNeg1_, minus1_, 0),
                Arguments.of(avgNeg1__, eq0, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("populateAddition")
    void testAddition(int in1, int in2, int expected) {
        Engine engine = new Engine();

        assertEquals(expected, engine.add(in1, in2));
    }

    @ParameterizedTest
    @MethodSource("populateIsPositive")
    void testIsPositive(int in, boolean expected) {
        Engine engine = new Engine();

        assertEquals(expected, engine.isPositive(in));
    }

    @ParameterizedTest
    @MethodSource("populateIsEqual")
    void testIsEqual(int in1, int in2, boolean expected) {
        Engine engine = new Engine();

        assertEquals(expected, engine.isEqual(in1, in2));
    }

    @ParameterizedTest
    @MethodSource("populateCheckNumber")
    void testCheckNumber(String expected, int in) {
        Engine engine = new Engine();

        assertEquals(expected, engine.checkNumber(in));
    }

    @ParameterizedTest
    @MethodSource("populateSumAndMultiply")
    void testSumAndMultiply(int in1, int in2, int expected) {
        Engine engine = new Engine();

        int result = engine.sumAndMultiply(in1, in2);
        assertEquals(expected, result);
    }

    @org.junit.jupiter.api.Test
    void testIsValid() {
        Engine engine = new Engine();

        assertTrue(engine.isValid());
    }



    private static Stream<Arguments> populateAddition() {
        return Stream.of(
                Arguments.of(2, 3, 5),
                Arguments.of(0, 0, 0),
                Arguments.of(-1, 0, -1),
                Arguments.of(-1, -2, -3),
                Arguments.of(2, -3, -1),
                Arguments.of(-1, 1, 0),
                Arguments.of(0, 1, 1)
        );
    }

    private static Stream<Arguments> populateIsPositive() {
        return Stream.of(
                Arguments.of(1, true),
                Arguments.of(0, false),
                Arguments.of(-1, false),
                Arguments.of(2, true)
        );
    }

    private static Stream<Arguments> populateIsEqual() {
        return Stream.of(
                Arguments.of(1, 1, true),
                Arguments.of(0, 0, true),
                Arguments.of(-1, -1, true),
                Arguments.of(1, 0, false),
                Arguments.of(0, 1, false),
                Arguments.of(-1, 0, false),
                Arguments.of(0, -1, false),
                Arguments.of(1, -1, false),
                Arguments.of(-1, 1, false)
        );
    }

    private static Stream<Arguments> populateCheckNumber() {
        return Stream.of(
                Arguments.of("Greater", 6),
                Arguments.of("Lesser", 5),
                Arguments.of("Lesser", 4),
                Arguments.of("Lesser", 0),
                Arguments.of("Lesser", -1)
        );
    }

    private static Stream<Arguments> populateSumAndMultiply() {
        return Stream.of(
                Arguments.of(0, 0, 0),
                Arguments.of(0, 1, 2),
                Arguments.of(0, -1, -2),
                Arguments.of(1, 0, 2),
                Arguments.of(-1, 0, -2)
        );
    }



}