package address.unittests.util;

import address.util.collections.UnmodifiableObservableList;
import javafx.collections.FXCollections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.*;

public class UnmodifiableObservableListTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void mutatingMethods_disabled() {
        List<Integer> backing = new ArrayList<>();
        backing.add(10);
        UnmodifiableObservableList<Integer> list = new UnmodifiableObservableList<>(FXCollections.observableList(backing));

        assertSame(list.sorted().getSource(), list);
        assertSame(list.filtered(i -> true).getSource(), list);

        thrown.expect(UnsupportedOperationException.class);
        list.addAll(2, 1);

        thrown.expect(UnsupportedOperationException.class);
        list.setAll(new ArrayList<Number>());

        thrown.expect(UnsupportedOperationException.class);
        list.setAll(1, 2);

        thrown.expect(UnsupportedOperationException.class);
        list.removeAll(1, 2);

        thrown.expect(UnsupportedOperationException.class);
        list.retainAll(1, 2);

        thrown.expect(UnsupportedOperationException.class);
        list.remove(0, 1);

        thrown.expect(UnsupportedOperationException.class);
        Iterator<Integer> iter = list.iterator();
        iter.next();
        iter.remove();

        thrown.expect(UnsupportedOperationException.class);
        list.add(3);

        thrown.expect(UnsupportedOperationException.class);
        list.remove(null);

        thrown.expect(UnsupportedOperationException.class);
        list.addAll(backing);

        thrown.expect(UnsupportedOperationException.class);
        list.addAll(0, backing);

        thrown.expect(UnsupportedOperationException.class);
        list.removeAll(backing);

        thrown.expect(UnsupportedOperationException.class);
        list.retainAll(backing);

        thrown.expect(UnsupportedOperationException.class);
        list.replaceAll(i -> 1);

        thrown.expect(UnsupportedOperationException.class);
        list.sort(Comparator.naturalOrder());

        thrown.expect(UnsupportedOperationException.class);
        list.clear();

        thrown.expect(UnsupportedOperationException.class);
        list.set(0, 2);

        thrown.expect(UnsupportedOperationException.class);
        list.add(0, 2);

        thrown.expect(UnsupportedOperationException.class);
        list.remove(0);

        thrown.expect(UnsupportedOperationException.class);
        ListIterator<Integer> liter = list.listIterator();
        liter.next();
        liter.remove();

        thrown.expect(UnsupportedOperationException.class);
        liter.add(5);

        thrown.expect(UnsupportedOperationException.class);
        liter.set(3);

        thrown.expect(UnsupportedOperationException.class);
        list.removeIf(i -> true);
    }
}
