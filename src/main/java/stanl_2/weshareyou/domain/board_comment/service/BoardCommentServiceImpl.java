package stanl_2.weshareyou.domain.board_comment.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board.repository.BoardRepository;
import stanl_2.weshareyou.domain.board_comment.aggregate.dto.BoardCommentDto;
import stanl_2.weshareyou.domain.board_comment.aggregate.entity.BoardComment;
import stanl_2.weshareyou.domain.board_comment.repository.BoardCommentRepository;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class BoardCommentServiceImpl implements BoardCommentService{
    private final BoardCommentRepository boardCommentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private Timestamp getCurrentTimestamp() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Timestamp.from(nowKst.toInstant());
    }

    @Autowired
    public BoardCommentServiceImpl(BoardCommentRepository boardCommentRepository, BoardRepository boardRepository,MemberRepository memberRepository, ModelMapper modelMapper) {
        this.boardCommentRepository = boardCommentRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public BoardCommentDto createBoardComment(BoardCommentDto boardCommentDto) {
        Timestamp currentTimestamp = getCurrentTimestamp();
        Long boardId = boardCommentDto.getBoardId();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));
        Long memberId =boardCommentDto.getMemberId();
        Member member= memberRepository.findById(memberId)
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));
        BoardComment boardComment = new BoardComment();
        boardComment.setContent(boardCommentDto.getContent());
        boardComment.setCreatedAt(currentTimestamp);
        boardComment.setUpdatedAt(currentTimestamp);
        board.setCommentCount(board.getCommentCount() + 1);
        boardComment.setBoard(board);
        boardComment.setMember(member);
        boardCommentRepository.save(boardComment);

        BoardCommentDto boardCommentDto1 = modelMapper.map(boardComment, BoardCommentDto.class);
        boardCommentDto1.setBoardCommentId(board.getId());
        boardCommentDto1.setNickname(member.getNickname());
        boardCommentDto1.setBoardId(board.getId());


        return boardCommentDto1;
    }

    @Transactional
    @Override
    public BoardCommentDto updateBoardComment(Long boardId,BoardCommentDto boardCommentDto) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        BoardComment boardcomment = boardCommentRepository.findById(boardId)
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));

        boardcomment.setContent(boardCommentDto.getContent());
        boardcomment.setUpdatedAt(currentTimestamp);
        boardCommentRepository.save(boardcomment);

        BoardCommentDto boardCommentDto1 = modelMapper.map(boardcomment, BoardCommentDto.class);
        boardCommentDto1.setBoardCommentId(boardcomment.getId());
        return boardCommentDto1;
    }
    @Transactional
    @Override
    public void deleteBoardComment(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));
        BoardComment boardcomment = boardCommentRepository.findById(boardId)
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));
        board.setCommentCount(board.getCommentCount()-1);
        boardCommentRepository.delete(boardcomment);
    }

    @Transactional
    @Override
    public List<BoardCommentDto> readCommentsByBoardId(Long boardId){
        List<BoardComment> boardComments = boardCommentRepository.findByBoardId(boardId);
        return boardComments.stream().map(boardComment  -> {
            BoardCommentDto boardCommentDto = new BoardCommentDto();
            boardCommentDto.setBoardCommentId(boardComment.getId());
            boardCommentDto.setContent(boardComment.getContent());
            boardCommentDto.setMemberProfileUrl(boardComment.getMember().getProfileUrl());

                    if (boardComment.getMember() != null) {
                boardCommentDto.setNickname(boardComment.getMember().getNickname()); // Member의 닉네임 추가
            } else {
                boardCommentDto.setNickname("Unknown");
            }

            boardCommentDto.setCreatedAt(boardComment.getCreatedAt());
            boardCommentDto.setUpdatedAt(boardComment.getUpdatedAt());
            return boardCommentDto;
            }
        )
        .collect(Collectors.toList());
    }

//    @Transactional
//    @Override
//    public BoardCommentDto readCommentsByBoardId(Long boardCommentId) {
//        BoardComment boardComment = boardCommentRepository.findById(boardCommentId)
//                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));
//        BoardCommentDto boardCommentDto = modelMapper.map(boardComment, BoardCommentDto.class);
//        boardCommentDto.setMemberId(boardComment.getMember() != null ? boardComment.getMember().getId() : null);
//        boardCommentDto.setBoardId(boardComment.getBoard() != null ? boardComment.getBoard().getId() : null);
//        return boardCommentDto;
//    }

    @Transactional
    @Override
    public List<BoardCommentDto> readComments() {
        List<BoardComment> boardComments = boardCommentRepository.findAll(); // 모든 댓글 조회
        return boardComments.stream()
            .map(boardComment -> {
                BoardCommentDto boardCommentDto = modelMapper.map(boardComment, BoardCommentDto.class);
//                    boardCommentDto.setMemberId(boardComment.getMember() != null ? boardComment.getMember().getId() : null);
                boardCommentDto.setBoardCommentId(boardComment.getBoard() != null ? boardComment.getBoard().getId() : null);
                return boardCommentDto;
            })
            .collect(Collectors.toList());
    }
}
