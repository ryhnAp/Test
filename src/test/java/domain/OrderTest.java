package domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File order1JsonFile = new File("src/test/resources/order1.json").getAbsoluteFile();

    private static Order order1;


    @BeforeAll
    public static void setup() {
        try {
            String initJsonOrder = FileUtils.readFileToString(order1JsonFile);
            order1 = gson.fromJson(initJsonOrder, (Type) Order.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void GIVEN_order_id_WHEN_get_id_THEN_return_id() {
        int expected = order1.getId();
        assertEquals(expected, order1.getId());
    }

    @Test
    void GIVEN_order_price_WHEN_get_price_THEN_return_price() {
        int expected = order1.getPrice();
        assertEquals(expected, order1.getPrice());
    }

    @Test
    void GIVEN_new_order_id_WHEN_get_id_THEN_return_default_id() {
        Order o = new Order();
        int expected = o.getId();
        assertEquals(expected, o.getId());
    }

    @Test
    void GIVEN_new_order_price_WHEN_get_price_THEN_return_default_price() {
        Order o = new Order();
        int expected = o.getPrice();
        assertEquals(expected, o.getPrice());
    }

    @Test
    void GIVEN_order_string_objects_WHEN_equals_THEN_not_instance_of_order() {
        assertFalse(order1.equals(""));
    }

    @ParameterizedTest
    @MethodSource("populateFraudulentQuantity")
    void GIVEN_diff_history_minus1_scenario_WHEN_get_customer_fraudulent_quantity_THEN_returns_related_quantity(Order o, int id, int price) {
        //setup
        //exercise
        //verify
        assertEquals(id, o.getId());
        assertEquals(price, o.getPrice());
        //teardown
    }

    private static Stream<Arguments> populateFraudulentQuantity() {
        Order o1 = new Order();
        o1.setId(4);
        o1.setCustomer(1);
        o1.setPrice(100);
        o1.setQuantity(-100);

        Order o2 = new Order();
        o2.setId(5);
        o2.setCustomer(1);
        o2.setPrice(0);
        o2.setQuantity(-100);

        Order o3 = new Order();
        o3.setId(6);
        o3.setCustomer(1);
        o3.setPrice(1);
        o3.setQuantity(-100);

        Order o4 = new Order();
        o4.setId(7);
        o4.setCustomer(1);
        o4.setPrice(-1);
        o4.setQuantity(-100);

        return Stream.of(
                Arguments.of(o1, 4, 100),
                Arguments.of(o2, 5, 0),
                Arguments.of(o3, 6, 1),
                Arguments.of(o4, 7, -1)
        );
    }
}