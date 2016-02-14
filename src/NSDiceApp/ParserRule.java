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
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author Björn Kihlström <bjorn_kihlstrom@outlook.com>
 */
public class ParserRule {

    private abstract class Wrapper {

        public abstract Expression call(ArrayList<Token> tokens);
    }

    private class TerminalWrapper extends Wrapper {

        private final TokenID _terminal;
        private final Expression _expression;

        public TerminalWrapper(TokenID terminal, Expression expression) {
            _terminal = terminal;
            _expression = expression;
        }

        @Override
        public Expression call(ArrayList<Token> tokens) {
            return terminalFunction(tokens, _terminal, _expression);
        }
    }

    private class NonTerminalWrapper extends Wrapper {

        private final ArrayList<ParserRule> _rules;
        private final Expression _expression;

        public NonTerminalWrapper(ArrayList<ParserRule> rules, Expression expression) {
            _rules = rules;
            _expression = expression;
        }


        @Override
        public Expression call(ArrayList<Token> tokens) {
            return nonTerminalFunction(tokens, _rules, _expression);
        }
    }

    private class LiteralWrapper extends Wrapper {

        private final String _literal;

        public LiteralWrapper(String literal) {
            _literal = literal;
        }

        @Override
        public Expression call(ArrayList<Token> tokens) {
            return literalFunction(tokens, _literal);
        }
    }

    private final LinkedList<Wrapper> _matches;

    public ParserRule() {
        _matches = new LinkedList<>();
    }

    public Expression terminalFunction(
            ArrayList<Token> tokens,
            TokenID terminal,
            Expression expression) {
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).Token() == terminal) {

            Expression expr;
            try {
                expr = Expression.makeCopy(expression);
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException("Internal parse error", ex);
            }
            expr.addToken(tokens.get(tokens.size() - 1));

            return expr;
        } else {
            return null;
        }
    }

    public Expression nonTerminalFunction(
            ArrayList<Token> tokens,
            ArrayList<ParserRule> rules,
            Expression expression) {

        ArrayList<Expression> children = new ArrayList<>();

        // Match all subrules
        for (ParserRule r : rules) {
            Expression exp = r.match(tokens);

            if (exp == null) {
                return null;
            } else {
                // Consume the tokens on match.
                if(exp.getTokens().isEmpty()) {
                    System.out.println("HEY");
                }
                
                tokens = new ArrayList<>(tokens.subList(0,
                        tokens.size() - exp.getTokens().size()));

                children.add(exp);
            }
        }
        
        Collections.reverse(children);

        // Copy the attached expression, add the children and return it.
        Expression exp;
        try {
            exp = Expression.makeCopy(expression);
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Internal parse error", ex);
        }
        exp.setChildren(children);
        return exp;
    }

    public Expression literalFunction(ArrayList<Token> tokens, String literal) {

        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).Literal().equals(literal)) {
            Expression dummy = Expression.dummy();
            dummy.addToken(tokens.get(tokens.size() - 1));
            return dummy;
        } else {
            return null;
        }
    }

    public void addTerminal(TokenID terminal, Expression expression) {
        _matches.add(new TerminalWrapper(terminal, expression));
    }

    public void addNonTerminal(ArrayList<ParserRule> rules, Expression expression) {
        Collections.reverse(rules);
        _matches.add(new NonTerminalWrapper(rules, expression));
    }

    public void addLiteral(String literal) {
        _matches.add(new LiteralWrapper(literal));
    }

    public static ParserRule makeLiteral(String literal) {
        ParserRule pr = new ParserRule();
        pr.addLiteral(literal);
        return pr;
    }

    public Expression match(ArrayList<Token> tokens) {
        for (Wrapper w : _matches) {
            Expression exp = w.call(tokens);
            if (exp != null) {
                return exp;
            }
        }
        return null;
    }
}
