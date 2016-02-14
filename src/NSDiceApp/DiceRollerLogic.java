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
import java.util.regex.Pattern;
import java.util.LinkedList;
import javafx.util.Pair;

import static NSDiceApp.TokenID.*;
/**
 *
 * @author Björn Kihlström <bjorn_kihlstrom@outlook.com>
 */
public class DiceRollerLogic {
    
    private final Tokenizer _tokenizer;
    private final DiceLanguageParser _parser;
    
    public DiceRollerLogic() {
        _tokenizer = new Tokenizer();
        _tokenizer.addRule(Pattern.compile("[Dd]\\d+"), DICE);
        _tokenizer.addRule(Pattern.compile("\\d+"), NUMBER);
        _tokenizer.addRule(Pattern.compile("[,\\=\\(\\)\\.]"), LITERAL);
        _tokenizer.addRule(Pattern.compile("[\\+\\-]"), ARITHOP);
        _tokenizer.addRule(Pattern.compile("[\\/\\*]"), FACTOROP);
        _tokenizer.addRule(Pattern.compile("[\\s+]"), WHITESPACE);
        _tokenizer.addRule(Pattern.compile("[a-zA-Z]+"), CHARSEQUENCE);
        
        _parser = new DiceLanguageParser();
    }
    
    public String evaluate(String expString) {
        try {
            ArrayList<Token> tokens = _tokenizer.tokenize(expString);
            
            LinkedList<String> lst = new LinkedList<>();
            tokens.stream().forEach((t) -> {
                lst.add((new Pair<>(t.Literal(), t.Token())).toString());
            });
            
            Expression exp = _parser.parse(tokens);
            ArrayList<Integer> lst2 = exp.evaluate();
            
            return lst.toString() + "\n" + lst2.toString();
        } catch (Tokenizer.TokenizerException exception) {
            return "String can't be parsed.";
        }
    }
}
