package Main;

import java.util.ArrayList;
import java.util.List;

public class Solution {

    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.recurse("2+8*(9/4-1.5)^(1+1)", 0); // Expected output: 6.5 6

    }

    public void recurse(final String expression, int countOperation) {
        //implement
        List<Lexeme> lexemes = parsing(expression);
        System.out.println(lexemes);
        LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);

        double result = Math.round(expression(lexemeBuffer) * 100) / 100.0;
        if (result % 1.0 == 0)
            System.out.println((long) result + " " + countExpressions(expression));
        else
            System.out.println(result + " " + countExpressions(expression));
    }

    public Solution() {
        //don't delete
    }

    public int countExpressions(String expression) {
        String s = expression.replaceAll("[^-|sin|cos|tan|+|*|/|^]", "");
        String s1 = s.replaceAll("sin|cos|tan", "1");
        return s1.length();
    }

    public static class Lexeme {
        LexemeType type;
        String value;

        public Lexeme(LexemeType type, String value) {
            this.type = type;
            this.value = value;
        }

        public Lexeme(LexemeType type, Character value) {
            this.type = type;
            this.value = value.toString();
        }

        @Override
        public String toString() {
            return "Lexeme{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public static class LexemeBuffer {
        private int position;
        public List<Lexeme> lexemes;

        public LexemeBuffer(List<Lexeme> lexemes) {
            this.lexemes = lexemes;
        }

        public Lexeme next() {
            return lexemes.get(position++);
        }

        public void back() {
            position--;
        }

        public int getPosition() {
            return position;
        }
    }

    public static List<Lexeme> parsing(String textForPars) {
        List<Lexeme> lexemes = new ArrayList<>();
        int position = 0;
        while (position < textForPars.length()) {
            char c = textForPars.charAt(position);
            switch (c) {
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET, c));
                    position++;
                    break;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET, c));
                    position++;
                    break;
                case '+':
                    lexemes.add(new Lexeme(LexemeType.PLUS, c));
                    position++;
                    break;
                case '-':
                    lexemes.add(new Lexeme(LexemeType.MINUS, c));
                    position++;
                    break;
                case '*':
                    lexemes.add(new Lexeme(LexemeType.MUL, c));
                    position++;
                    break;
                case '/':
                    lexemes.add(new Lexeme(LexemeType.DIV, c));
                    position++;
                    break;
                case '^':
                    lexemes.add(new Lexeme(LexemeType.POW, c));
                    position++;
                    break;
                case 's':
                    if (position + 3 < textForPars.length() &&
                            textForPars.charAt(position + 1) == 'i' &&
                            textForPars.charAt(position + 2) == 'n' &&
                            textForPars.charAt(position + 3) == '(') {
                        lexemes.add(new Lexeme(LexemeType.SIN, c));
                        position += 4;
                        break;
                    } else throw new WrongExpressionExeption();
                case 'c':
                    if (position + 3 < textForPars.length() &&
                            textForPars.charAt(position + 1) == 'o' &&
                            textForPars.charAt(position + 2) == 's' &&
                            textForPars.charAt(position + 3) == '(') {
                        lexemes.add(new Lexeme(LexemeType.COS, c));
                        position += 4;
                        break;
                    } else throw new WrongExpressionExeption();
                case 't':
                    if (position + 3 < textForPars.length() &&
                            textForPars.charAt(position + 1) == 'a' &&
                            textForPars.charAt(position + 2) == 'n' &&
                            textForPars.charAt(position + 3) == '(') {
                        lexemes.add(new Lexeme(LexemeType.TAN, c));
                        position += 4;
                        break;
                    } else throw new WrongExpressionExeption();
                default:
                    if (c <= '9' && c >= '0') {
                        StringBuilder stringBuilder = new StringBuilder();
                        int pointCounter = 0;
                        do {
                            stringBuilder.append(c);
                            position++;
                            if (position >= textForPars.length())
                                break;
                            c = textForPars.charAt(position);
                            if (c == '.') {
                                pointCounter++;
                                if (pointCounter > 1) {
                                    throw new WrongExpressionExeption();
                                }
                            }
                        } while ((c <= '9' && c >= '0') || c == '.');
                        if (pointCounter == 1)
                            lexemes.add(new Lexeme(LexemeType.DOUBLE, stringBuilder.toString()));
                        else lexemes.add(new Lexeme(LexemeType.NUMBER, stringBuilder.toString()));
                    } else {
                        if (c != ' ')
                            throw new WrongExpressionExeption();
                        position++;
                    }
            }
        }
        lexemes.add(new Lexeme(LexemeType.END, ""));
        return lexemes;
    }
    // Rules:
    // expression : plus_minus EOF
    // plus_minus : mul_div ( ( '+' | '-' ) mul_div )*
    // mul_div : pow ( ( '*' | '/' ) pow )*
    // pow : first ( '^' first )*
    // first : NUM |DOUBLE| '-' pow | ( '(' | 'cos(' | 'sin(' | 'tan(' ) add_sub ')'

    public static double expression(LexemeBuffer lexemes) {
        Lexeme lexeme = lexemes.next();
        if (lexeme.type == LexemeType.END)
            return 0.d;
        else {
            lexemes.back();
            return plusminus(lexemes);
        }
    }

    public static double plusminus(LexemeBuffer lexemes) {
        double value = muldiv(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case PLUS:
                    value += muldiv(lexemes);
                    break;
                case MINUS:
                    value -= muldiv(lexemes);
                    break;
                default:
                    lexemes.back();
                    return value;
            }
        }
    }

    public static double muldiv(LexemeBuffer lexemes) {
        double value = pow(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case MUL:
                    value *= pow(lexemes);
                    break;
                case DIV:
                    value /= pow(lexemes);
                    break;
                default:
                    lexemes.back();
                    return value;
            }
        }
    }

    public static double pow(LexemeBuffer lexemes) {
        double value = first(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case POW:
                    value = Math.pow(value, first(lexemes));
                    break;
                default:
                    lexemes.back();
                    return value;
            }
        }
    }

    public static double first(LexemeBuffer lexemes) {
        Lexeme lexeme = lexemes.next();
        switch (lexeme.type) {
            case MINUS:
                double value = first(lexemes);
                return -value;
            case NUMBER:
                return Integer.parseInt(lexeme.value);
            case DOUBLE:
                return Double.parseDouble(lexeme.value);
            case LEFT_BRACKET:
            case SIN:
            case COS:
            case TAN:
                double val = 0.0;
                if (lexeme.type == LexemeType.LEFT_BRACKET)
                    val = plusminus(lexemes);
                if (lexeme.type == LexemeType.SIN)
                    val = Math.sin(Math.toRadians(plusminus(lexemes)));
                if (lexeme.type == LexemeType.COS)
                    val = Math.cos(Math.toRadians(plusminus(lexemes)));
                if (lexeme.type == LexemeType.TAN)
                    val = Math.tan(Math.toRadians(plusminus(lexemes)));
                lexeme = lexemes.next();
                if (lexeme.type != LexemeType.RIGHT_BRACKET) {
                    throw new WrongExpressionExeption();
                }
                return val;
            default:
                throw new WrongExpressionExeption();
        }
    }

}
