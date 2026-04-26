package com.room13.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO loaded from riddles.json — not a Hibernate entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Riddle {

    private int id;
    private String level;
    private String question;
    private String hint;
    private String answer;
    private String digitRule;
    private int digit;
    private String explanation;

    public Riddle() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getDigitRule() { return digitRule; }
    public void setDigitRule(String digitRule) { this.digitRule = digitRule; }

    public int getDigit() { return digit; }
    public void setDigit(int digit) { this.digit = digit; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
