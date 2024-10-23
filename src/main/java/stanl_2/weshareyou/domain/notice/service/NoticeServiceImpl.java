package stanl_2.weshareyou.domain.notice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.domain.notice.aggregate.dto.NoticeDTO;
import stanl_2.weshareyou.domain.notice.aggregate.entity.Notice;
import stanl_2.weshareyou.domain.notice.repository.NoticeRepository;
import stanl_2.weshareyou.global.common.dto.CursorDTO;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService{
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final ModelMapper modelMapper;
    private Timestamp getCurrentTimestamp() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Timestamp.from(nowKst.toInstant());
    }

    @Autowired
    public NoticeServiceImpl(NoticeRepository noticeRepository, ModelMapper modelMapper, MemberRepository memberRepository) {
        this.noticeRepository = noticeRepository;
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        this.modelMapper = modelMapper;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public CursorDTO readNotices(CursorDTO cursorDTO) {
        Pageable pageable = PageRequest.of(0, cursorDTO.getSize());
        Slice<Notice> noticeList;

        if(cursorDTO.getCursorId() == null){
            noticeList = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            noticeList = noticeRepository.findByIdLessThanOrderByCreatedAtDesc(cursorDTO.getCursorId(), pageable);

        }

        Long lastNoticeId = noticeList.getContent().isEmpty() ? null :
                noticeList.getContent().get(noticeList.getNumberOfElements() - 1).getId();

        if(noticeList.isEmpty()){
            throw new CommonException(ErrorCode.NOTICE_NOT_FOUND);
        }
        else {
            List<NoticeDTO> noticeDTOList = noticeList.getContent().stream()
                    .filter(notice -> notice.getActive())
                    .map(notice -> {
                        NoticeDTO noticeDTO = new NoticeDTO();
                        noticeDTO.setId(notice.getId());
                        noticeDTO.setTitle(notice.getTitle());
                        noticeDTO.setCreatedAt(notice.getCreatedAt());
                        noticeDTO.setAdminId(notice.getMember().getId());
                        return noticeDTO;
                    })
                    .toList();

            CursorDTO cursorResponseDTO = new CursorDTO();
            cursorResponseDTO.setCursorId(lastNoticeId);
            cursorResponseDTO.setHasNext(noticeList.hasNext());
            /* 설명. 반환형 */
            cursorResponseDTO.setComment(noticeDTOList);

            return cursorResponseDTO;
        }
    }

    @Override
    @Transactional
    public NoticeDTO readNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOTICE_NOT_FOUND));

        /* 설명. @ManyToOne 관계일 때, dto에 엔티티 타입 제외하면 편해질 수 있다. (따로 setter) */

        NoticeDTO noticeReadByIdResponseDTO = new NoticeDTO();

        noticeReadByIdResponseDTO.setId(notice.getId());
        noticeReadByIdResponseDTO.setTitle(notice.getTitle());
        noticeReadByIdResponseDTO.setContent(notice.getContent());
        noticeReadByIdResponseDTO.setCreatedAt(notice.getCreatedAt());
        noticeReadByIdResponseDTO.setUpdatedAt(notice.getUpdatedAt());
        noticeReadByIdResponseDTO.setActive(notice.getActive());
        noticeReadByIdResponseDTO.setAdminId(notice.getMember().getId());

        return noticeReadByIdResponseDTO;
    }

    @Override
    @Transactional
    public NoticeDTO createNotice(NoticeDTO noticeCreateRequestDTO){
        Timestamp currentTimestamp = getCurrentTimestamp();
        Member admin = memberRepository.findById(noticeCreateRequestDTO.getAdminId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Notice notice = modelMapper.map(noticeCreateRequestDTO, Notice.class);

        notice.setMember(admin);

        notice.setCreatedAt(currentTimestamp);
        notice.setUpdatedAt(currentTimestamp);
        notice.setActive(true);

        Notice savedNotice = noticeRepository.save(notice);

        if (savedNotice == null || savedNotice.getId() == null) {
            throw new CommonException(ErrorCode.NOTICE_REGISTER_FAIL);
        }
        NoticeDTO noticeCreateResponseDTO = new NoticeDTO();

        noticeCreateResponseDTO.setId(savedNotice.getId());
        noticeCreateResponseDTO.setTitle(savedNotice.getTitle());


        return noticeCreateResponseDTO;
}

    @Override
    @Transactional
    public Boolean updateNotice(NoticeDTO noticeUpdateRequestDTO){

        Timestamp currentTimestamp = getCurrentTimestamp();

        Notice notice = noticeRepository.findById(noticeUpdateRequestDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOTICE_NOT_FOUND));

        notice.setTitle(noticeUpdateRequestDTO.getTitle());
        notice.setContent(noticeUpdateRequestDTO.getContent());
        notice.setUpdatedAt(currentTimestamp);

        Notice updatedNotice = noticeRepository.save(notice);

        if (updatedNotice != null && !updatedNotice.getId().equals(noticeUpdateRequestDTO.getId())) {
            throw new CommonException(ErrorCode.NOTICE_UPDATE_FAIL);
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean deleteNotice(NoticeDTO noticeDeleteRequestDTO) {

        Timestamp currentTimestamp = getCurrentTimestamp();
        Notice notice = noticeRepository.findById(noticeDeleteRequestDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOTICE_NOT_FOUND));

        notice.setActive(false);
        notice.setUpdatedAt(currentTimestamp);

        Notice deleteNotice = noticeRepository.save(notice);

        if (deleteNotice != null && !deleteNotice.getId().equals(noticeDeleteRequestDTO.getId())) {
            throw new CommonException(ErrorCode.NOTICE_DELETE_FAIL);
        }

        return true;
    }

}
