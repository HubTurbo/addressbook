package address.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import address.parser.expr.AndExpr;
import address.parser.expr.Expr;
import address.parser.expr.NotExpr;
import address.parser.expr.PredExpr;
import address.parser.qualifier.*;

public class Parser {
    public Expr parse(String input) throws ParseException {
        Expr result = null;

        Pattern pattern = Pattern.compile("\\s*(!*\\w+)\\s*:\\s*(\\w+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        while (!matcher.hitEnd()) {
            if (!matcher.find()) throw new ParseException("Part of input invalid '" + input + "'");
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

    private Expr createPredicate(String qualifierName, String qualifierContent) throws ParseException {
        Matcher matcher = getNegativeMatcher(qualifierName);
        if (matcher.matches()) {
            return new NotExpr(createPredicate(matcher.group(1), qualifierContent));
        }
        return getRawPredicate(qualifierName, qualifierContent);
    }

    private Expr getRawPredicate(String qualifierName, String content) throws ParseException {
        return new PredExpr(getQualifier(qualifierName, content));
    }

    private Matcher getNegativeMatcher(String type) {
        Pattern pattern = Pattern.compile("!(!*\\w+)", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(type);
    }

    private Qualifier getQualifier(String type, String content) throws ParseException {
        switch (type) {
            case "city":
                return new CityQualifier(content);
            case "lastName":
                return new LastNameQualifier(content);
            case "firstName":
                return new FirstNameQualifier(content);
            case "name":
                return new NameQualifier(content);
            case "street":
                return new StreetQualifier(content);
            case "tag":
                return new TagQualifier(content);
            case "id":
                return new IdQualifier(parseInt(content));
            default:
                throw new ParseException("Unrecognised qualifier " + type);
        }
    }

    private Integer parseInt(String content) throws ParseException {
        try {
            return Integer.valueOf(content);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid integer: " + content);
        }
    }
}
