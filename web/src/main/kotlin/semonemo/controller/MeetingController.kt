package semonemo.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import semonemo.config.LoginUserArgumentResolver.Companion.findLoginUser
import semonemo.config.LoginUserArgumentResolver.Companion.unauthorizedResponse
import semonemo.model.invitation.AttendanceUpdateRequest
import semonemo.model.meeting.MeetingGetResponse
import semonemo.model.meeting.MeetingSaveRequest
import semonemo.model.meeting.MeetingResponse
import semonemo.model.SemonemoResponse
import semonemo.model.invitation.WantToAttendRequest
import semonemo.model.exception.ForbiddenException
import semonemo.service.MeetingService
import java.time.LocalDateTime

@RestController
class MeetingController(
    private val meetingService: MeetingService,
) {

    @PostMapping("/api/meetings", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createMeeting(
        @RequestBody request: MeetingSaveRequest,
        session: WebSession
    ): Mono<ResponseEntity<SemonemoResponse>> {
        val user = findLoginUser(session) ?: return unauthorizedResponse()

        return meetingService.saveMeeting(user, request)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = MeetingResponse.of(it)))) }
            .onErrorResume {
                Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(SemonemoResponse(statusCode = 400, message = it.message))
                )
            }
    }

    @GetMapping("/api/meetings")
    fun getMeetings(session: WebSession): Mono<ResponseEntity<SemonemoResponse>> {
        val user = findLoginUser(session) ?: return unauthorizedResponse()
        val now = LocalDateTime.now()

        return meetingService.findMeetings(now, user)
            .collectList()
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = MeetingGetResponse.listOf(it, user, now)))) }
    }

    @GetMapping("/api/meetings/{id}")
    fun getMeetings(session: WebSession, @PathVariable id: Long): Mono<ResponseEntity<SemonemoResponse>> {
        val user = findLoginUser(session) ?: return unauthorizedResponse()

        return meetingService.findMeeting(id, user)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = MeetingGetResponse.of(it, user)))) }
            .onErrorResume {
                Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(SemonemoResponse(statusCode = 400, message = it.message))
                )
            }
    }

    @DeleteMapping("/api/meetings/{id}")
    fun removeMeeting(session: WebSession, @PathVariable id: Long): Mono<ResponseEntity<SemonemoResponse>> {
        val user = findLoginUser(session) ?: return unauthorizedResponse()

        return meetingService.removeMeeting(user, id)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = MeetingResponse.of(it)))) }
            .onErrorResume {
                when (it) {
                    is java.lang.IllegalArgumentException -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(SemonemoResponse(statusCode = 404, message = it.message))
                    )
                    is ForbiddenException -> Mono.just(
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(SemonemoResponse(statusCode = 403, message = it.message))
                    )
                    else -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SemonemoResponse(statusCode = 500))
                    )
                }
            }
    }

    @PutMapping("/api/meetings/{id}/attendance")
    fun updateWantToAttend(
        session: WebSession,
        @PathVariable id: Long,
        @RequestBody request: WantToAttendRequest
    ): Mono<ResponseEntity<SemonemoResponse>> {
        val user = findLoginUser(session) ?: return unauthorizedResponse()

        return meetingService.updateWantToAttend(id, user, request)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = MeetingResponse.of(it)))) }
            .onErrorResume {
                when (it) {
                    is java.lang.IllegalStateException -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(SemonemoResponse(statusCode = 400, message = it.message))
                    )
                    is java.lang.IllegalArgumentException -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(SemonemoResponse(statusCode = 404, message = it.message))
                    )
                    else -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SemonemoResponse(statusCode = 500))
                    )
                }
            }
    }

    @PutMapping("/api/meetings/{meetingId}/users/{userId}/attendance")
    fun updateAttendance(
        session: WebSession,
        @PathVariable meetingId: Long,
        @PathVariable userId: Long,
        @RequestBody request: AttendanceUpdateRequest
    ): Mono<ResponseEntity<SemonemoResponse>> {
        val loginUser = findLoginUser(session) ?: return unauthorizedResponse()

        return meetingService.updateAttendance(loginUser, meetingId, userId, request)
            .flatMap { Mono.just(ResponseEntity.ok(SemonemoResponse(data = MeetingResponse.of(it)))) }
            .onErrorResume {
                when (it) {
                    is java.lang.IllegalStateException -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            SemonemoResponse(statusCode = 400, message = it.message)
                        )
                    )
                    is java.lang.IllegalArgumentException
                    -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            SemonemoResponse(statusCode = 404, message = it.message)
                        )
                    )
                    is ForbiddenException -> Mono.just(
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(SemonemoResponse(statusCode = 403, message = it.message))
                    )
                    else -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SemonemoResponse(statusCode = 500))
                    )
                }
            }
    }
}
