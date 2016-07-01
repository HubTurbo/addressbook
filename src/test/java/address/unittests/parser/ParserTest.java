package address.unittests.parser;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.tag.Tag;
import address.parser.ParseException;
import address.parser.Parser;
import address.parser.expr.Expr;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParserTest {
    @Test
    public void parser_multipleQualifiers_correctExprProduced() throws ParseException {
        String filterString = "name:Mueller tag:friends";
        Expr expr = Parser.parse(filterString);

        ReadOnlyViewablePerson readOnlyViewablePerson = prepareReadOnlyViewablePersonMock("John", "Mueller", "", "friends");

        assertTrue(expr.satisfies(readOnlyViewablePerson));
    }

    @Test
    public void parser_containsNotExprAndMultipleQualifiers_correctExprProduced() throws ParseException {
        String filterString = "!name:Mueller tag:friends";
        Expr expr = Parser.parse(filterString);

        ReadOnlyViewablePerson personOne = prepareReadOnlyViewablePersonMock("John", "Mueller", "", "friends");
        ReadOnlyViewablePerson personTwo = prepareReadOnlyViewablePersonMock("John", "Tan", "", "friends");
        ReadOnlyViewablePerson personThree = prepareReadOnlyViewablePersonMock("John", "Lee", "", "colleagues");

        assertFalse(expr.satisfies(personOne));
        assertTrue(expr.satisfies(personTwo));
        assertFalse(expr.satisfies(personThree));
    }

    @Test
    public void parser_multipleNotExprAndMultipleQualifiers_correctExprProduced() throws ParseException {
        String filterString = "!name:Mueller !tag:friends !!city:Singapore";
        Expr expr = Parser.parse(filterString);

        ReadOnlyViewablePerson personOne = prepareReadOnlyViewablePersonMock("John", "Mueller", "", "friends");
        ReadOnlyViewablePerson personTwo = prepareReadOnlyViewablePersonMock("John", "Tan", "Singapore", "friends");
        ReadOnlyViewablePerson personThree = prepareReadOnlyViewablePersonMock("Mull", "Lee", "Malaysia", "colleagues");
        ReadOnlyViewablePerson personFour = prepareReadOnlyViewablePersonMock("Jack", "Lim", "Singapore", "colleagues");

        assertFalse(expr.satisfies(personOne));
        assertFalse(expr.satisfies(personTwo));
        assertFalse(expr.satisfies(personThree));
        assertTrue(expr.satisfies(personFour));
    }

    private ReadOnlyViewablePerson prepareReadOnlyViewablePersonMock(String firstName, String lastName,
                                                                     String city, String... tags) {
        ReadOnlyViewablePerson readOnlyViewablePersonMock = mock(ReadOnlyViewablePerson.class);
        List<Tag> tagList = new ArrayList<>();
        for (String tagString : tags) {
            tagList.add(new Tag(tagString));
        }
        when(readOnlyViewablePersonMock.getCity()).thenReturn(city);
        when(readOnlyViewablePersonMock.getFirstName()).thenReturn(firstName);
        when(readOnlyViewablePersonMock.getLastName()).thenReturn(lastName);
        when(readOnlyViewablePersonMock.getTagList()).thenReturn(tagList);
        return readOnlyViewablePersonMock;
    }
}
