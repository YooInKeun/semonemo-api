package semonemo.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import semonemo.model.dto.MeetingGetResponse
import semonemo.service.MeetingService

@RestController
class MeetingController(
    private val meetingService: MeetingService,
) {

    @GetMapping("/api/meetings")
    fun getMeetings(): Mono<ResponseEntity<List<MeetingGetResponse>>> =
        meetingService.findMeetings()
            .collectList()
            .flatMap { Mono.just(ResponseEntity.ok(MeetingGetResponse.listOf(it))) }
}