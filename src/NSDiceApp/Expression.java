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

/**
 *
 * @author Björn Kihlström <bjorn_kihlstrom@outlook.com>
 */
public abstract class Expression implements Cloneable {


    private ArrayList<Expression> _children;
    private ArrayList<Token> _tokens;
    
    public static Expression makeCopy(Expression e) throws CloneNotSupportedException {
        Expression expCln = (Expression) e.clone();
        expCln._children = new ArrayList<>();
        expCln._tokens = new ArrayList<>();
        return expCln;
    }

    public Expression() {
        _children = new ArrayList<>();
        _tokens = new ArrayList<>();
    }

    public void addToken(Token t) {
        _tokens.add(t);
    }
    
    public void setChildren(ArrayList<Expression> children) {
        _children = children;
        _children.stream().forEach((e) -> {
            _tokens.addAll(e.getTokens());
        });
    }
    
    public Expression getChildAt(int index) {
        return _children.get(index);
    }

    // Override this if dummy expressions are desired.
    public boolean shouldIgnore() {
        return false;
    }

    public ArrayList<Token> getTokens() {
        return _tokens;
    }

    public abstract ArrayList<Integer> evaluate();
    
    public ArrayList<Integer> makeList(Integer... nums) {
        return new ArrayList<>(Arrays.asList(nums));
    }
    
    public int result(int index) {
        return _children.get(index).evaluate().get(0);
    }
    
    public static Expression dummy() {
        return new Expression() {
                @Override
                public ArrayList<Integer> evaluate() {
                    throw new UnsupportedOperationException("Literals should not be parsed");
                }
                
                @Override
                public boolean shouldIgnore() {
                    return true;
                }
            };
    }
}
