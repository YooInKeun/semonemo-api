package semonemo.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import semonemo.config.LoginUser
import semonemo.model.blacklist.BlacklistSaveRequest
import semonemo.model.blacklist.BlacklistSaveResponse
import semonemo.model.SemonemoResponse
import semonemo.model.entity.User
import semonemo.service.BlacklistService

@RestController
class BlacklistController(
    private val blacklistService: BlacklistService,
) {

    @PostMapping("/api/blacklist", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createBlacklist(@RequestBody request: BlacklistSaveRequest, @LoginUser user: User): Mono<ResponseEntity<SemonemoResponse>> {
        return blacklistService.saveBlacklist(user, request)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = BlacklistSaveResponse.of(it)))) }
            .onErrorResume { Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SemonemoResponse(statusCode = 400, message = it.message))) }
    }
}
