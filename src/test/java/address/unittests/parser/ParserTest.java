package address.unittests.parser;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.tag.Tag;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParserTest {
    Parser parser;

    @Before
    public void setup() {
        parser = new Parser();
    }

    @Test
    public void parser_multipleQualifiers_correctExprProduced() throws ParseException {
        String filterString = "name:Mueller tag:friends";
        Expr expr = parser.parse(filterString);

        ReadOnlyViewablePerson readOnlyViewablePerson = prepareReadOnlyViewablePersonMock(1, "John", "Mueller", "", "", "friends");

        assertTrue(expr.satisfies(readOnlyViewablePerson));
    }

    @Test
    public void parser_containsNotExprAndMultipleQualifiers_correctExprProduced() throws ParseException {
        String filterString = "!name:Mueller tag:friends";
        Expr expr = parser.parse(filterString);

        ReadOnlyViewablePerson personOne = prepareReadOnlyViewablePersonMock(1, "John", "Mueller", "", "", "friends");
        ReadOnlyViewablePerson personTwo = prepareReadOnlyViewablePersonMock(2, "John", "Tan", "", "", "friends");
        ReadOnlyViewablePerson personThree = prepareReadOnlyViewablePersonMock(3, "John", "Lee", "", "", "colleagues");

        assertFalse(expr.satisfies(personOne));
        assertTrue(expr.satisfies(personTwo));
        assertFalse(expr.satisfies(personThree));
    }

    @Test
    public void parser_multipleNotExprAndMultipleQualifiers_correctExprProduced() throws ParseException {
        String filterString = "!name:Mueller !tag:friends !!city:Singapore";
        Expr expr = parser.parse(filterString);

        ReadOnlyViewablePerson personOne = prepareReadOnlyViewablePersonMock(1, "John", "Mueller", "", "", "friends");
        ReadOnlyViewablePerson personTwo = prepareReadOnlyViewablePersonMock(2, "John", "Tan", "", "Singapore", "friends");
        ReadOnlyViewablePerson personThree = prepareReadOnlyViewablePersonMock(3, "Mull", "Lee", "", "Malaysia", "colleagues");
        ReadOnlyViewablePerson personFour = prepareReadOnlyViewablePersonMock(4, "Jack", "Lim", "", "Singapore", "colleagues");

        assertFalse(expr.satisfies(personOne));
        assertFalse(expr.satisfies(personTwo));
        assertFalse(expr.satisfies(personThree));
        assertTrue(expr.satisfies(personFour));
    }

    @Test
    public void parser_allQualifiers_correctExprProduced() throws ParseException {
        String filterString = "name:Mueller tag:friends city:Singapore street:Victoria id:5";
        Expr expr = parser.parse(filterString);

        ReadOnlyViewablePerson personOne = prepareReadOnlyViewablePersonMock(1, "John", "Tan", "", "Singapore", "friends");
        ReadOnlyViewablePerson personTwo = prepareReadOnlyViewablePersonMock(2, "John", "Mueller", "Victoria Street", "Singapore", "friends");
        ReadOnlyViewablePerson personThree = prepareReadOnlyViewablePersonMock(3, "Mull", "Lee", "Johor Street", "Malaysia", "colleagues");
        ReadOnlyViewablePerson personFour = prepareReadOnlyViewablePersonMock(4, "Jack", "Lim", "Heng Mui Keng Terrace", "Singapore", "colleagues");
        ReadOnlyViewablePerson personFive = prepareReadOnlyViewablePersonMock(5, "Martin", "Mueller", "Victoria Street", "Singapore", "friends");

        assertFalse(expr.satisfies(personOne));
        assertFalse(expr.satisfies(personTwo));
        assertFalse(expr.satisfies(personThree));
        assertFalse(expr.satisfies(personFour));
        assertTrue(expr.satisfies(personFive));
    }

    private ReadOnlyViewablePerson prepareReadOnlyViewablePersonMock(int id, String firstName, String lastName,
                                                                     String street, String city, String... tags) {
        ReadOnlyViewablePerson readOnlyViewablePersonMock = mock(ReadOnlyViewablePerson.class);
        List<Tag> tagList = new ArrayList<>();
        for (String tagString : tags) {
            tagList.add(new Tag(tagString));
        }
        when(readOnlyViewablePersonMock.getId()).thenReturn(id);
        when(readOnlyViewablePersonMock.getFirstName()).thenReturn(firstName);
        when(readOnlyViewablePersonMock.getLastName()).thenReturn(lastName);
        when(readOnlyViewablePersonMock.getStreet()).thenReturn(street);
        when(readOnlyViewablePersonMock.getCity()).thenReturn(city);
        when(readOnlyViewablePersonMock.getTagList()).thenReturn(tagList);
        return readOnlyViewablePersonMock;
    }
}
