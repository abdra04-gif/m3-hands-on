import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A plain-Java controller. 
 *
 * Only {@link #listOrders()} is hand-written and will pass its test
 * on day one. {@link #getOrderById(long)} and
 * {@link #createOrder(String, int)} are TODO stubs; their tests
 * fail until the student autocompletes them.
 */
public class OrderController {

    public record Order(long id, String item, int qty) {}

    private final Map<Long, Order> store = new HashMap<>();
    private long nextId = 1;

    public OrderController() {
        Order seed = new Order(nextId++, "seed-item", 1);
        store.put(seed.id(), seed);
    }

    /** GET /orders -- return every stored order. Hand-written. */
    public List<Order> listOrders() {
        return new ArrayList<>(store.values());
    }

    /** GET /orders/{id} -- return the matching order, or null if
     *  none exists. TODO: complete with Copilot. */
    public Order getOrderById(long id) {
        // TODO: look up id in `store` and return it (or null).
        throw new UnsupportedOperationException("TODO: complete via AI");
    }

    /** POST /orders -- create a new order, assign it the next id,
     *  store it, and return it. TODO: complete with Copilot. */
    public Order createOrder(String item, int qty) {
        // TODO: validate item/qty, allocate nextId, put in store, return.
        throw new UnsupportedOperationException("TODO: complete via AI");
    }
}
