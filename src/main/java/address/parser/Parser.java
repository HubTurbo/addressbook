package address.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import address.parser.expr.AndExpr;
import address.parser.expr.Expr;
import address.parser.expr.PredExpr;
import address.parser.qualifier.NameQualifier;

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
        switch (type) {
        case "name":
            return new PredExpr(new NameQualifier(content));
        default:
            throw new ParseException("Unrecognised qualifier " + type);
        }
    }
}
