package semonemo

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono
import semonemo.model.SemonemoResponse
import semonemo.model.exception.UnauthorizedException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(exception: UnauthorizedException): Mono<ResponseEntity<SemonemoResponse>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(SemonemoResponse(statusCode = 401, message = exception.message))
        )
    }
}
