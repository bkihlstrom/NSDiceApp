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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author Björn Kihlström <bjorn_kihlstrom@outlook.com>
 */
public class Tokenizer {

    public class TokenizerException extends Exception {
    };

    private class Rule {

        private final Pattern _pattern;
        private final TokenID _token;

        public Rule(Pattern pattern, TokenID token) {
            _pattern = pattern;
            _token = token;
        }

        public TokenID Token() {
            return _token;
        }

        public Pattern Pattern() {
            return _pattern;
        }
    }

    private final LinkedList<Rule> _rules = new LinkedList<>();

    public void addRule(Pattern pattern, TokenID token) {
        pattern = Pattern.compile("^" + pattern.pattern());
        _rules.add(new Rule(pattern, token));
    }

    public ArrayList<Token> tokenize(String expression)
            throws TokenizerException {

        ArrayList<Token> tokens = new ArrayList<>();

        while (!expression.isEmpty()) {
            String copy = expression;
            for (Rule rule : _rules) {
                Matcher matcher = rule.Pattern().matcher(expression);

                if (matcher.find()) {
                    if (rule.Token() != TokenID.WHITESPACE) {
                        tokens.add(new Token(rule.Token(), matcher.group().trim()));
                    }
                    expression = matcher.replaceFirst("");
                    break;
                }
            }

            if (expression.equals(copy)) {
                throw new TokenizerException();
            }
        }

        return tokens;
    }
}
