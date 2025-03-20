package dev.biddan.problemjsonexample;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserApiController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserRegistrationResponse> register(
            @Valid @RequestBody UserService.UserRegistrationRequest request) {
        User user = userService.register(request);

        UserRegistrationResponse response = new UserRegistrationResponse(
                user.getId(), user.getEmail(), user.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    public record UserRegistrationResponse(
            String id,
            String email,
            String name
    ) {

    }
}
