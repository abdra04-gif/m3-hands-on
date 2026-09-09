import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class OrderControllerTest {

    @Test
    void listOrdersReturnsSeedOnStartup() {
        var c = new OrderController();
        List<OrderController.Order> all = c.listOrders();
        assertEquals(1, all.size());
        assertEquals("seed-item", all.get(0).item());
    }

    @Test
    void getOrderByIdReturnsSeedForIdOne() {
        var c = new OrderController();
        OrderController.Order o = c.getOrderById(1L);
        assertNotNull(o, "getOrderById(1) should return the seed order");
        assertEquals("seed-item", o.item());
    }

    @Test
    void getOrderByIdReturnsNullForMissing() {
        var c = new OrderController();
        assertNull(c.getOrderById(9999L));
    }

    @Test
    void createOrderAddsAndReturnsOrder() {
        var c = new OrderController();
        OrderController.Order o = c.createOrder("book", 2);
        assertNotNull(o);
        assertEquals("book", o.item());
        assertEquals(2,      o.qty());
        assertEquals(2, c.listOrders().size());
    }
}
