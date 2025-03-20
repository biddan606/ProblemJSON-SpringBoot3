package dev.biddan.problemjsonexample;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("이미 등록된 이메일입니다: " + email);
        this.email = email;
    }
}
