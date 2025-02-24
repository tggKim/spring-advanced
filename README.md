# SPRING ADVANCED

### 1-1 Early Return

```
if (userRepository.existsByEmail(signupRequest.getEmail())) {
  throw new InvalidRequestException("이미 존재하는 이메일입니다.");
}
```

- 이메일 중복을 판단하는 코드 위치를 가장 첫 번째 줄로 옮겨서 encode가 불필요하게 호출되는 것을 막음

### 1-2 불필요한 if-else  피하기

```
if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
  throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
  }

WeatherDto[] weatherArray = responseEntity.getBody();
if (weatherArray == null || weatherArray.length == 0) {
  throw new ServerException("날씨 데이터가 없습니다.");
}
```

  - 별개의 if문으로 처리하는 것이 가독성이 좋다고 판단해서 중첩 if문을 바깥으로 꺼냈다

### 1-3 Validation

```
@NotBlank
@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
@Pattern(regexp = "^(.*\\d.*[A-Z].*|.*[A-Z].*\\d.*)$", message = "비밀번호는 숫자와 대문자를 포함해야 됩니다.")
private String newPassword;

@ExceptionHandler
public ResponseEntity<Map<String, Object>> methodArgumentNotValidException(MethodArgumentNotValidException ex){
  HttpStatus status = HttpStatus.BAD_REQUEST;
  return getErrorResponse(status, ex.getMessage());
}
```

- @Size와 @Pattern을 사용해서 @Valid  에노테이션으로 검증하도록 했고

- GlobalExceptionHandler 에서 MethodArgumentNotValidException ex 처리하도록 했다.




