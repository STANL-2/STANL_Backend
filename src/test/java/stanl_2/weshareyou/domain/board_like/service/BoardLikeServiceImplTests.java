package stanl_2.weshareyou.domain.board_like.service;


import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board.repository.BoardRepository;
import stanl_2.weshareyou.domain.board_like.aggregate.dto.BoardLikeDto;
import stanl_2.weshareyou.domain.board_like.aggregate.entity.BoardLikeId;
import stanl_2.weshareyou.domain.board_like.repository.BoardLikeRepository;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.global.common.exception.CommonException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

@SpringBootTest
class BoardLikeServiceImplTests {

    @Mock
    private BoardLikeRepository boardLikeRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BoardLikeServiceImpl boardLikeService;

    @Test
    void 좋아요_추가() {
        // Given
        BoardLikeDto boardLikeDto = new BoardLikeDto();
        boardLikeDto.setBoardId(1L);
        boardLikeDto.setMemberId(1L);

        Board board = new Board();
        board.setLikesCount(0);
        Member member = new Member();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(boardLikeRepository.findById(any(BoardLikeId.class))).thenReturn(Optional.empty());

        // When
        BoardLikeDto result = boardLikeService.BoardLike(boardLikeDto);

        // Then
        assertNotNull(result);
        assertEquals(1, board.getLikesCount());
    }


    @Test
    void 좋아요_취소() {
        // Given
        BoardLikeDto boardLikeDto = new BoardLikeDto();
        boardLikeDto.setBoardId(1L);
        boardLikeDto.setMemberId(1L);

        Board board = new Board();
        board.setLikesCount(1);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(boardLikeRepository.findById(any(BoardLikeId.class))).thenReturn(Optional.empty()); // 좋아요 기록 없음

        // When & Then
        CommonException exception = assertThrows(CommonException.class, () -> {
            boardLikeService.BoardUnLike(boardLikeDto);
        });

        // 예외 메시지 검증
        assertEquals("좋아요를 누르지 않았습니다.", exception.getMessage());
    }
}
