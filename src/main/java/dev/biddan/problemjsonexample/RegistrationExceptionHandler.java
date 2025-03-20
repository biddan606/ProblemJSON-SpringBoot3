package dev.biddan.problemjsonexample;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 회원가입 비즈니스 로직 관련 예외를 처리하는 핸들러
 * - ResponseEntityExceptionHandler를 상속하지 않음
 * - 회원가입 도메인에 특화된 예외만 처리
 * - 높은 우선순위로 설정
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegistrationExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage());

        problemDetail.setTitle("이메일 중복 오류");
        problemDetail.setType(URI.create("https://example.com/probs/email-already-exists"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        // 확장 필드 추가
        problemDetail.setProperty("email", ex.getEmail());
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", "REG-001");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }
}
