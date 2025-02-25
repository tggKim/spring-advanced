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

public void changePassword(@Auth AuthUser authUser,@Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest){...}
```

- @Size와 @Pattern을 사용해서 @Valid 에노테이션으로 검증하도록 했다.

```
@ExceptionHandler
public ResponseEntity<Map<String, Object>> methodArgumentNotValidException(MethodArgumentNotValidException ex){
  Map<String, String> errors = new HashMap<>();
  for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
    errors.put(fieldError.getField(), fieldError.getDefaultMessage());
  }
  HttpStatus status = HttpStatus.BAD_REQUEST;
  return getErrorResponse(status, errors);
}

public ResponseEntity<Map<String, Object>> getErrorResponse(HttpStatus status, Map<String, String> message) {
  Map<String, Object> errorResponse = new HashMap<>();
  errorResponse.put("status", status.name());
  errorResponse.put("code", status.value());
  errorResponse.put("message", message);

  return new ResponseEntity<>(errorResponse, status);
}
```

- GlobalExceptionHandler 에서 MethodArgumentNotValidException ex 처리하도록 했다.


### 2 N+1 문제

```
@EntityGraph(attributePaths = "user")
@Query("SELECT t FROM Todo t ORDER BY t.modifiedAt DESC")
Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

@EntityGraph(attributePaths = "user")
@Query("SELECT t FROM Todo t WHERE t.id = :todoId")
Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
```

- @EntityGraph를 사용해서 fetch join을 사용

### 3-1 예상대로 성공하는지에 대한 케이스입니다.

```
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

- rawPassword와 encodedPassword의 위치를 바꾸어서 테스트가 정상적으로 작동하도록 만들었다.

### 3-2-1

```
public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
  // given
  long todoId = 1L;
  given(todoRepository.findById(todoId)).willReturn(Optional.empty());

  // when & then
  InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
  assertEquals("Todo not found", exception.getMessage());
}
```

- 테스트 코드의 이름을 수정하고 비교하는 예외 메시지를 수정했다.

### 3-2-2

```
InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
  commentService.saveComment(authUser, todoId, request);
});
```

- ServerException을 서비스에서 던지는 InvalidRequestException으로 교체했다.

### 3-2-3

```
if(todo.getUser() == null){
  throw new InvalidRequestException("todo의 user가 null 입니다.");
}
```

- if문을 ManagerService에 추가해서 todo의 user가 null인지 검사하도록 수정

```
assertEquals("todo의 user가 null 입니다.", exception.getMessage());
```

- ManagerServiceTest 에서 예외 메시지 검증을 수정

### 4-1 Interceptor를 사용하여 어드민 권한 확인하고, 성공시 요청 시각과 URL 로깅하기

```
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserRole userRole = UserRole.valueOf((String)request.getAttribute("userRole"));

        // 관리자 권한이 없는 경우 403을 반환합니다.
        if (!UserRole.ADMIN.equals(userRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
            return false;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("요청 url = {}, 요청 시각 = {}", request.getRequestURL(), dateFormat.format(new Date()));

        return HandlerInterceptor.super.preHandle(request, response, handler);

    }
}
```

- 기존에 JwtFilter에 있던 권한을 확인하는 로직을 제거

- AuthInterceptor를 만들어서 인터셉터에서 권한을 확인하고 로깅 정보를 찍는다.
