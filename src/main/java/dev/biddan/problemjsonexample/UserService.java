package dev.biddan.problemjsonexample;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User register(UserRegistrationRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // 새로운 사용자 생성
        User user = new User(request.email(), request.password(), request.name());
        return userRepository.save(user);
    }

    public record UserRegistrationRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "잘못된 이메일 형식입니다")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다")
            @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
            String password,

            @NotBlank(message = "이름은 필수입니다")
            String name
    ) {

    }
}
