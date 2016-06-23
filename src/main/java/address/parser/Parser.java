package address.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import address.parser.expr.AndExpr;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.parser.qualifier.*;

public class Parser {

    private Parser() {}

    public static Expr parse(String input) throws ParseException {
        Expr result = null;

        Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            Expr intermediate = createPredicate(matcher.group(1), matcher.group(2));
            if (result == null) {
                result = intermediate;
            } else {
                result = new AndExpr(intermediate, result);
            }
        }

        if (result == null) {
            throw new ParseException("Failed to parse '" + input + "'");
        }

        return result;
    }

    private static Expr createPredicate(String type, String content) throws ParseException {
        return new PredExpr(getQualifier(type, content));
    }

    private static Qualifier getQualifier(String type, String content) throws ParseException {
        switch (type) {
            case "city":
                return new CityQualifier(content);
            case "lastName":
                return new LastNameQualifier(content);
            case "firstName":
                return new FirstNameQualifier(content);
            case "name":
                return new NameQualifier(content);
            default:
                throw new ParseException("Unrecognised qualifier " + type);
        }
    }
}
