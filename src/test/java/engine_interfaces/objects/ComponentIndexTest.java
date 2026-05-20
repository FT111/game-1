package engine_interfaces.objects;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentIndexTest {

    private static class TestComponentA extends Component {}
    private static class TestComponentB extends Component {}

    @Test
    void queryReturnsIntersectionForMultipleComponents() {
        ComponentIndex<String> index = new ComponentIndex<>();
        index.add("one", TestComponentA.class);
        index.add("one", TestComponentB.class);
        index.add("two", TestComponentA.class);

        HashSet<String> result = index.query(TestComponentA.class, TestComponentB.class);

        assertEquals(1, result.size());
        assertTrue(result.contains("one"));
    }

    @Test
    void removeIdClearsFromAllComponentLists() {
        ComponentIndex<String> index = new ComponentIndex<>();
        index.add("one", TestComponentA.class);
        index.add("one", TestComponentB.class);

        index.remove("one");

        assertTrue(index.query(TestComponentA.class).isEmpty());
        assertTrue(index.query(TestComponentB.class).isEmpty());
    }
}
