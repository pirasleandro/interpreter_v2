import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class LolcodeInterpreter {
    private String[] code;
    private Scanner scan = new Scanner(System.in);
    private HashMap<String, Object> variables = new HashMap<>();
    private static final String[] MULTI_WORD_TOKENS = {
        "IF U SAY SO",
        "I HAS A",
        "ITZ LIEK A",
        "IS NOW A",
        ", O RLY?",
        "IM IN YR",
        "IM OUTTA YR",
        "HOW IZ I",
        "O HAI IM",
        "HAI \\S+",
        "BTW .+",
        "ITZ A",
        "BOTH SAEM",
        "BIGGR OF",
        "SMALLR OF",
        "SUM OF",
        "PRODUKT OF",
        "DIFF OF",
        "QUOSHUNT OF",
        "MOD OF",
        "YA RLY",
        "NO WAI",
        ", WTF?",
        "FOUND YR",
        "HAS A"
    };

    public LolcodeInterpreter(String[] code) {
        this.code = code;
    }

    public void interpret() throws ParseException {
        for (int i = 0; i < code.length; i++) {
            if (step(code[i])) {
                break;
            }
        }
    }

    private boolean step(String line) throws ParseException {
        String[] words = null;
        try {
            words = parse(line).split(" ");
        } catch (ParseException e) {
            return true;
        }
        line: for (int i = 0; i < words.length; i++) {
            switch (words[i]) {
                case "BTW": break line;
                case "OBTW": {
                    for (int j = i; j < words.length; i++) {
                        if (words[j] == "TLDR") break line;
                    }
                    throw new ParseException("ERROR: multiline comment must be closed with \"TLDR\"", 0);
                }
                case "VISIBLE": System.out.println(readExpression(words, i+1)); break;
                case "GIMMEH": variables.put(words[i+1], scan.nextLine());
                case "I_HAS_A": {
                    if (words[i+1].matches("[a-zA-Z_]+[a-zA-Z_0-9]*")) {
                        if (!variables.containsKey(words[i+1])) {
                            variables.put(words[i+1], null);
                            System.out.println(">>> CREATED VARIABLE " + words[i+1]);
                        } else {
                            throw new ParseException("ERROR: Variable " + words[i+1] + " already exists", 0);
                        }
                        if (words[i+2].equals("ITZ")) {
                            if (words[i+3] == "A") {
                                switch (words[i+4]) {
                                    case "NOOB": variables.put(words[i+1], words[i+3]);
                                    case "TROOF": variables.put(words[i+1], false);
                                    case "NUMBR": variables.put(words[i+1], 0);
                                    case "NUMBAR": variables.put(words[i+1], 0.0f);
                                    case "YARN": variables.put(words[i+1], "");
                                }
                            } else {
                                variables.put(words[i+1], words[i+3]);
                            }
                            System.out.println(">>> SET VARIABLE " + words[i+1] + " TO " + variables.get(words[i+1]));
                        } else {
                            System.out.println(">>> wat");
                        }
                    } else {
                        throw new ParseException("ERROR: Invalid variable identifier \"" + words[i+1] + "\"", 0);
                    }
                }
                case "IM_IN_YR": {
                    continue; // BACKLOG
                }
                case "KTHXBYE": return false;
                default: {
                    if (variables.containsKey(words[i])) {
                        try {
                            if (words[i+1] == "R") {
                                variables.put(words[i], readExpression(words, i+1));
                            } else if (words[i+1] == "IS_NOW_A") {
                                variables.put(words[i], convert(variables.get(words[i]), words[i+2]));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            continue;
                        }
                    } else {
                        Object obj = readExpression(words, i);
                        switch (words[i+1]) {
                            case ",_O_RLY?": {
                                if (obj instanceof Boolean) {
                                    if ((Boolean) obj) break line;
                                } else {
                                    // AAAA
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private String parse(String line) throws ParseException {
        System.out.println("\n/----- PARSE LINE: " + line + " -----\\\n");
        ArrayList<String> output = new ArrayList<>();
        startIndex: for (int i = 0; i < line.split(" ").length; i++) {
            for (String multiWordToken : MULTI_WORD_TOKENS) {
                try {
                    String regex = multiWordToken.replace(" ", "_");
                    String temp = String.join("_", Arrays.asList(line.split(" ")).subList(i, i + multiWordToken.split(" ").length));
                    if (temp.matches(regex)) {
                        System.out.println(temp + " == " + regex);
                        output.add(temp);
                        i += multiWordToken.split(" ").length-1;
                        continue startIndex;
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
            }
            output.add(line.split(" ")[i]);
        }
        System.out.println("\n\\----- PARSED: " + output.toString() + " ----/\n");
        return String.join(" ", output);
    }

    private Object readExpression(String[] line, int index) throws ParseException {
        switch (line[index]) {
            case "BOTH_SAEM": {
                Object[] values = getExpressionParams(line, index);
                return values[0] == values[1];
            }
            case "DIFFRINT": {
                Object[] values = getExpressionParams(line, index);
                return values[0] != values[1];
            }
            case "BIGGR_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    if (values[0].floatValue() > values[1].floatValue()) return values[0];
                    else return values[1];
                }
            }
            case "SMALLR_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    if (values[0].floatValue() < values[1].floatValue()) return values[0];
                    else return values[1];
                }
            }
            case "SUM_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    return values[0].floatValue() + values[1].floatValue();
                }
            }
            case "DIFF_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    return values[0].floatValue() - values[1].floatValue();
                }
            }
            case "PRODUKT_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    return values[0].floatValue() * values[1].floatValue();
                }
            }
            case "QUOSHUNT_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    return values[0].floatValue() / values[1].floatValue();
                }
            }
            case "MOD_OF": {
                Object[] objs = getExpressionParams(line, index);
                if (objs instanceof Number[]) {
                    Number[] values = (Number[]) objs;
                    return values[0].floatValue() % values[1].floatValue();
                }
            }
            case "WIN": return true;
            case "FAIL": return false;
            case "NOOB": return null;
            case "MAEK": {
                Object value = readExpression(line, index+1);
                int i = 0;
                while (line[i] != "A") { i++; }
                try {
                    switch (line[i+1]) {
                        case "NOOB": return null;
                        case "TROOF": {
                            if (value == null) return false;
                            else if (value instanceof Boolean) return (Boolean) value;
                            else if (value instanceof Integer) return ((Integer) value).intValue() > 0;
                            else if (value instanceof Float) return ((Float) value).floatValue() > 0;
                            else if (value instanceof String) return value == "";
                        }
                        case "NUMBR": {
                            if (value == null) return 0;
                            else if (value instanceof Boolean) return ((Boolean) value ? 1 : 0);
                            else if (value instanceof Integer) return (Integer) value;
                            else if (value instanceof Float) return Math.round((Float) value);
                            else if (value instanceof String) return Integer.parseInt((String) value);
                        }
                        case "NUMBAR": {
                            if (value == null) return 0.0f;
                            else if (value instanceof Boolean) return ((Boolean) value ? 1.0f : 0.0f);
                            else if (value instanceof Integer) return (Float) value;
                            else if (value instanceof Float) return (Float) value;
                            else if (value instanceof String) return Float.parseFloat((String) value);
                        }
                        case "YARN": {
                            if (value == null) return "";
                            else if (value instanceof Boolean) return ((Boolean) value ? "WIN" : "FALSE");
                            else if (value instanceof Integer) return String.valueOf(value);
                            else if (value instanceof Float) return String.valueOf(value);
                            else if (value instanceof String) return (String) value;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    // TODO
                }
            }
            default: {
                if (line[index].matches("[0-9]+.[0-9]+")) {
                    return Float.parseFloat(line[index]);
                } else if (line[index].matches("[0-9]+")) {
                    return Integer.parseInt(line[index]);
                } else if (line[index].matches("\".+")) {
                    StringBuilder sb = new StringBuilder();
                    String lineString = String.join(" ", Arrays.asList(line).subList(index, line.length));
                    int i = 1;
                    try {
                        while (lineString.charAt(i) != '"') {
                            i++;
                            sb.append(lineString.charAt(i));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        throw new ParseException("ERROR: Strings must be enclosed by quotation marks", 0);
                    }
                } else if (variables.containsKey(line[index])) {
                    return variables.get(line[index]);
                }
            }
        }
        throw new ParseException("ERROR: Expression expected", 0);
    }

    private Object[] getExpressionParams(String[] line, int index) throws ParseException {
        Object first = readExpression(line, index);
        int i = 0;
        while (line[i] != "AN") { i++; }
        Object second = readExpression(line, i+1);
        return new Object[] {
            first,
            second
        };
    }

    private Object convert(Object value, String type) {
        return null; // TODO
    }
}
