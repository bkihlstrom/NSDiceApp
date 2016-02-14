/*
 * The MIT License
 *
 * Copyright 2016 Björn Kihlström <bjorn_kihlstrom@outlook.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package NSDiceApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static NSDiceApp.ParserRuleID.*;
import static NSDiceApp.TokenID.*;

/**
 *
 * @author Björn Kihlström <bjorn_kihlstrom@outlook.com>
 */
public class DiceLanguageParser {

    private final HashMap<ParserRuleID, ParserRule> _rules;
    private final ParserRule _root;
    private final Random _rnd;

    public DiceLanguageParser() {
        _rules = new HashMap<>();
        _rnd = new Random();

        // plusminus
        _rules.put(PLUSMINUS, new ParserRule());
        _rules.get(PLUSMINUS).addTerminal(ARITHOP, new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                String literal = getTokens().get(0).Literal();
                switch (literal) {
                    case "+":
                        return makeList(1);
                    case "-":
                        return makeList(-1);
                    default:
                        throw new RuntimeException("Internal parse error");
                }
            }
        });

        // multdiv
        _rules.put(MULTDIV, new ParserRule());
        _rules.get(MULTDIV).addTerminal(FACTOROP, new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                String literal = getTokens().get(0).Literal();
                switch (literal) {
                    case "*":
                        return makeList(1);
                    case "/":
                        return makeList(-1);
                    default:
                        throw new RuntimeException("Internal parse error");
                }
            }
        });

        // non-terminals
        _rules.put(DOTOPERATION, new ParserRule());
        _rules.get(DOTOPERATION).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(DOTOPERATION),
                        ParserRule.makeLiteral("."),
                        _rules.get(NAME))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                //No name operations supported yet.
                return makeList(-1);
            }
        });
        _rules.get(DOTOPERATION).addTerminal(CHARSEQUENCE, new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                //No name operations supported yet.
                return makeList(-1);
            }
        });

        // Atom rules declared.
        _rules.put(ATOM, new ParserRule());

        // Number constant expression
        _rules.get(ATOM).addTerminal(NUMBER, new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return makeList(Integer.parseInt(getTokens().get(0).Literal()));
            }
        });

        // Dice expression
        _rules.get(ATOM).addTerminal(DICE, new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                String literal = getTokens().get(0).Literal().replace("d", "");
                return makeList(_rnd.nextInt(Integer.parseInt(literal)) + 1);
            }
        });

        // Pass anything else into dot operation.
        _rules.get(ATOM).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(DOTOPERATION))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(0).evaluate();
            }
        });

        //term
        _rules.put(TERM, new ParserRule());
        _rules.get(TERM).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(TERM),
                        _rules.get(MULTDIV),
                        _rules.get(ATOM))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return makeList((int) (result(0) * Math.pow(result(2), result(1))));
            }
        });
        _rules.get(TERM).addNonTerminal(
                new ArrayList<>(Arrays.asList(_rules.get(ATOM))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(0).evaluate();
            }
        });

        _rules.put(ARITHEXPR, new ParserRule());

        _rules.get(ARITHEXPR).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(ARITHEXPR),
                        _rules.get(PLUSMINUS),
                        _rules.get(TERM))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return makeList(result(0) + result(1) * result(2));
            }
        });
        _rules.get(ARITHEXPR).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        ParserRule.makeLiteral("-"),
                        _rules.get(TERM))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return makeList(-result(0));
            }
        });
        _rules.get(ARITHEXPR).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(TERM),
                        _rules.get(TERM))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                int res = 0;
                for (int i = 0; i < result(0); i++) {
                    res += result(1);
                }
                return makeList(res);
            }
        });
        _rules.get(ARITHEXPR).addNonTerminal(
                new ArrayList<>(Arrays.asList(_rules.get(TERM))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(0).evaluate();
            }
        });

        // Make atom recursive to be able to do some things
        _rules.get(ATOM).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        ParserRule.makeLiteral("("),
                        _rules.get(ARITHEXPR),
                        ParserRule.makeLiteral(")"))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(0).evaluate();
            }
        });

        _rules.put(ASSIGNMENT, new ParserRule());
        _rules.get(ASSIGNMENT).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(DOTOPERATION),
                        ParserRule.makeLiteral("="),
                        _rules.get(ARITHEXPR))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(1).evaluate();
            }
        });
        _rules.get(ASSIGNMENT).addNonTerminal(
                new ArrayList<>(Arrays.asList(_rules.get(ARITHEXPR))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(0).evaluate();
            }
        });

        _rules.put(EXPR, new ParserRule());
        _rules.get(EXPR).addNonTerminal(
                new ArrayList<>(Arrays.asList(
                        _rules.get(EXPR),
                        ParserRule.makeLiteral(","),
                        _rules.get(ASSIGNMENT))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                ArrayList<Integer> ret = getChildAt(0).evaluate();
                ret.addAll(getChildAt(1).evaluate());
                return ret;
            }
        });
        _rules.get(EXPR).addNonTerminal(
                new ArrayList<>(Arrays.asList(_rules.get(ASSIGNMENT))),
                new Expression() {
            @Override
            public ArrayList<Integer> evaluate() {
                return getChildAt(0).evaluate();
            }
        });

        _root = _rules.get(EXPR);
    }

    public Expression parse(ArrayList<Token> tokens) {
        Expression e = _root.match(tokens);
        return e;
    }
}
