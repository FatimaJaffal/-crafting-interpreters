package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0; // Points to the first character in the lexeme being scanned.
  private int current = 0; // Points at the character currently being considered.
  private int line = 1; // Tracks what source line "current" is on.

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", AND);
    keywords.put("class", CLASS);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("fun", FUN);
    keywords.put("for", FOR);
    keywords.put("if", IF);
    keywords.put("nil", NIL);
    keywords.put("or", OR);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("this", THIS);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '/':
        /*
         * Comments are lexemes, but they aren't meaningful, and the parser doesn't want
         * to deal with them, so we don't call addToken() here.
         */
        if (match('/')) {
          SingleLineComment();
        } else if (match('*')) {
          multiLineComment();
        } else {
          addToken(SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private void SingleLineComment() {
    // A comment goes until the end of the line.
    while (peek() != '\n' && !isAtEnd())
      advance();
  }

  private void multiLineComment() {
    /*
     * We require some extra state to track the nesting, which makes this not quite regular.
     */
    int nesting = 1;
    while (nesting > 0) {
      if (peek() == '\0') {
        Lox.error(line, "Unexpected end of comment.");
        return;
      }
      if (peek() == '/' && peekNext() == '*') {
        advance();
        advance();
        nesting++;
        continue;
      }
      if (peek() == '*' && peekNext() == '/') {
        advance();
        advance();
        nesting--;
        continue;
      }
      if (peek() == '\n')
        line++;
      advance();
    }
  }

  private void identifier() {
    // Maximal munch
    while (isAlphaNumeric(peek()))
      advance();
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = IDENTIFIER;
    addToken(type);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private void number() {
    /*
     * A series of digits optionally followed by a "." and one or more trailing
     * digits.
     */
    while (isDigit(peek()))
      advance();
    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();
    }
    while (isDigit(peek()))
      advance();
    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    advance(); // The closing ".

    // Trim the surrounding qoutes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private char peek() {
    return isAtEnd() ? '\0' : source.charAt(current);
  }

  private char peekNext() {
    return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
  }

  private boolean match(char expected) {
    if (isAtEnd())
      return false;
    if (source.charAt(current) != expected)
      return false;
    current++;
    return true;
  }

  private boolean isAtEnd() { // We have consumed all the characters.
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
