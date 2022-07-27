package semonemo.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import semonemo.model.entity.Meeting
import semonemo.model.entity.MeetingStatus
import semonemo.model.entity.User
import semonemo.model.exception.ForbiddenException
import semonemo.repository.MeetingRepository

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
) {

    // TODO: 정렬 기능 추가
    @Transactional(readOnly = true)
    fun findMeetings(): Flux<Meeting> = meetingRepository.findAllByStatus(MeetingStatus.ACTIVE)

    @Transactional
    fun removeMeeting(user: User, id: Long): Mono<Meeting> = meetingRepository.findById(id)
        .switchIfEmpty(Mono.defer { Mono.error(IllegalArgumentException("존재하지 않는 모임입니다. (id: $id)")) })
        .flatMap {
            if (it.hostUserId != user.id) {
                return@flatMap Mono.defer { Mono.error(ForbiddenException("권한이 없는 유저입니다. (호스트가 아님)")) }
            }

            if (it.isRemoved) {
                return@flatMap Mono.defer { Mono.error(IllegalArgumentException("이미 제거된 모임입니다. (id: $id)")) }
            }

            it.remove()
            meetingRepository.save(it)
        }
}
