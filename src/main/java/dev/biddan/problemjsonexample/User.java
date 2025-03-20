package dev.biddan.problemjsonexample;

import java.util.UUID;
import lombok.Getter;

@Getter
public class User {

    private final String id;
    private final String email;
    private final String password;
    private final String name;

    public User(String email, String password, String name) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
