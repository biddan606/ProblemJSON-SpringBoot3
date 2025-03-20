package dev.biddan.problemjsonexample;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // 유효성 검사 오류에 대한 Problem Detail 생성
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("필드 유효성 검사 실패");
        problemDetail.setType(URI.create("https://example.com/probs/validation-error"));
        problemDetail.setDetail("입력 값 검증에 실패했습니다.");

        // 요청 URI 설정
        problemDetail.setInstance(URI.create(
                request.getDescription(false).replace("uri=", "")));

        // 필드 오류를 저장할 맵 생성
        Map<String, Object> errors = new HashMap<>();

        // 각 필드 오류에 대해 상세 정보 설정
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, Object> fieldError = new HashMap<>();
            fieldError.put("message", error.getDefaultMessage());
            fieldError.put("code", error.getCode() != null ? error.getCode().toLowerCase() : "unknown");

            // 오류 코드에 따른 추가 정보 제공
            if ("Size".equalsIgnoreCase(error.getCode())) {
                fieldError.put("minLength", 8);
                fieldError.put("currentLength", error.getRejectedValue() != null ?
                        ((String) error.getRejectedValue()).length() : 0);
                fieldError.put("requirements", List.of("최소 8 자"));
            } else if ("Email".equalsIgnoreCase(error.getCode())) {
                fieldError.put("suggestion", "@ 및 도메인 이름을 포함하십시오");
            }

            errors.put(error.getField(), fieldError);
        });

        // 확장 필드 추가
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", "VAL-001");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }
}
