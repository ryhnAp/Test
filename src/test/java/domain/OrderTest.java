package domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

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
}