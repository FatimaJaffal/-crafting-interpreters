package com.craftinginterpreters.lox;

public class Token {
  final TokenType type;
  final String lexem;
  final Object literal;
  final int line;

  Token(TokenType type, String lexem, Object literal, int line) {
    this.type = type;
    this.lexem = lexem;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexem + " " + literal;
  }

}
