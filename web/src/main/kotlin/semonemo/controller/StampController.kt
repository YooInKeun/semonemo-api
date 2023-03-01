package semonemo.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import semonemo.config.LoginUser
import semonemo.model.SemonemoResponse
import semonemo.model.entity.User
import semonemo.model.stamp.Stamp
import semonemo.model.stamp.StampGetResponse
import semonemo.service.StampService

@RestController
class StampController(
    private val stampService: StampService,
) {

    @GetMapping("/api/stamps")
    fun getStamps(@LoginUser user: User): Mono<ResponseEntity<SemonemoResponse>> {
        return stampService.findStamps(user)
            .collectList()
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = StampGetResponse.listOf(user, it)))) }
    }

    @GetMapping("/api/stamps/{id}")
    fun getStamp(@PathVariable id: Long, @LoginUser user: User): Mono<ResponseEntity<SemonemoResponse>> {
        return stampService.findStamp(id)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = StampGetResponse.of(user, it)))) }
    }

    @GetMapping("/api/stamps/new")
    fun getNewStamps(@LoginUser user: User): Mono<ResponseEntity<SemonemoResponse>> {
        return stampService.findNewStamps(user)
            .collectList()
            .flatMap { generateResponseWithOrder(user, it) }
    }

    @PutMapping("/api/stamps/new")
    fun updateNewStamps(@LoginUser user: User): Mono<ResponseEntity<SemonemoResponse>> {
        return stampService.updateNewStamps(user)
            .collectList()
            .flatMap { generateResponseWithOrder(user, it) }
    }

    private fun generateResponseWithOrder(user: User, newStamps: List<Stamp>): Mono<ResponseEntity<SemonemoResponse>> {
        return stampService.findStamps(user).count()
            .flatMap { totalStampCount ->
                Mono.just(
                    ResponseEntity.ok(
                        SemonemoResponse(
                            data =
                            StampGetResponse.listOf(
                                user = user,
                                newStamps = newStamps,
                                totalStampCount = totalStampCount
                            )
                        )
                    )
                )
            }
    }
}
