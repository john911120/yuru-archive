package com.yuru.archive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.yuru.archive.DataNotFoundException;

/*
 * 2026-11-03 セキュリティ修正（doc/11-03checkCode.md 参照）
 * - 例外メッセージ（ex.getMessage()）には利用者の入力値が含まれる場合があるため、
 *   クライアントへはそのまま返さず、固定メッセージのみを返す。
 * - 応答は必ず text/plain で返し、ブラウザがHTMLとして解釈しないようにする（反射型XSS対策）。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    static final String BAD_REQUEST_MESSAGE = "リクエストの内容が正しくありません。";

    @ExceptionHandler(DuplicateVoteException.class)
    @ResponseBody
    public ResponseEntity<String> handleDuplicateVote(DuplicateVoteException ex) {
        // サーバー側で定義した固定文言のみを持つ例外
        return plainText(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedVoteException.class)
    @ResponseBody
    public ResponseEntity<String> handleUnauthorized(UnauthorizedVoteException ex) {
        // サーバー側で定義した固定文言のみを持つ例外
        return plainText(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return plainText(HttpStatus.FORBIDDEN, "この操作を行う権限がありません。");
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleNotFound(DataNotFoundException ex) {
        return plainText(HttpStatus.NOT_FOUND, "対象のデータが見つかりません。");
    }

    // 2026-11-03: NoHandlerFoundException も404として扱う。
    // 静的リソースハンドラーがない構成（MockMvc standaloneSetup 等）では、存在しないパスへのアクセスで
    // NoResourceFoundException ではなく NoHandlerFoundException が発生し、従来は500になっていた。
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseBody
    public ResponseEntity<String> handleNoResource(Exception ex) {
        return plainText(HttpStatus.NOT_FOUND, "指定されたページが見つかりません。");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<String> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return plainText(HttpStatus.METHOD_NOT_ALLOWED,
                "このURLでは指定されたHTTPメソッドを使用できません。");
    }

    @ExceptionHandler(InvalidUploadFileException.class)
    @ResponseBody
    public ResponseEntity<String> handleInvalidUpload(InvalidUploadFileException ex) {
        // FileValidator / AttachService が定義した固定文言のみを返す（ファイル名等は含めない）
        return plainText(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    @ResponseBody
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        // ex.getMessage() には「For input string: "<script>..."」のように入力値が含まれるため返さない
        return plainText(HttpStatus.BAD_REQUEST, BAD_REQUEST_MESSAGE);
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseBody
    public ResponseEntity<String> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : "リクエストを処理できませんでした。";
        return plainText(ex.getStatusCode(), message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleGeneral(Exception ex) {
        return plainText(HttpStatus.INTERNAL_SERVER_ERROR, "予期しないエラーが発生しました。");
    }

    private static ResponseEntity<String> plainText(HttpStatusCode status, String body) {
        return ResponseEntity.status(status)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, java.nio.charset.StandardCharsets.UTF_8))
                .body(body);
    }
}
