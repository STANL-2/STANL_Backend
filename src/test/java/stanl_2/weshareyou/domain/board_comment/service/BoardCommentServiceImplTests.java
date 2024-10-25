package stanl_2.weshareyou.domain.board_comment.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board.repository.BoardRepository;
import stanl_2.weshareyou.domain.board_comment.aggregate.dto.BoardCommentDto;
import stanl_2.weshareyou.domain.board_comment.aggregate.entity.BoardComment;
import stanl_2.weshareyou.domain.board_comment.repository.BoardCommentRepository;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;

import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;  // 수정된 부분
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class BoardCommentServiceImplTests {
    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BoardCommentServiceImpl boardCommentService;

    @Test
    void 게시글_댓글_작성() {
        // Given
        BoardCommentDto boardCommentDto = new BoardCommentDto();
        boardCommentDto.setBoardId(1L);
        boardCommentDto.setMemberId(1L);
        boardCommentDto.setContent("Test Comment");

        Board board = new Board();
        board.setCommentCount(0);

        Member member = new Member();
        member.setNickname("TestUser");

        BoardComment boardComment = new BoardComment();
        boardComment.setContent("Test Comment");

        // ModelMapper로 매핑된 결과를 반환하도록 모킹
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(boardCommentRepository.save(any(BoardComment.class))).thenReturn(boardComment);
        when(modelMapper.map(any(BoardComment.class), eq(BoardCommentDto.class))).thenReturn(boardCommentDto);

        // When
        BoardCommentDto result = boardCommentService.createBoardComment(boardCommentDto);


        assertEquals("TestUser", result.getNickname());
        assertEquals("Test Comment", result.getContent());
    }

    @Test
    void 게시글_댓글_삭제() {
        // Given
        Long boardId = 1L;
        Board board = new Board();
        board.setCommentCount(1);  // 댓글 하나 존재하는 상태

        BoardComment boardComment = new BoardComment();

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardCommentRepository.findById(boardId)).thenReturn(Optional.of(boardComment));

        // When
        boardCommentService.deleteBoardComment(boardId);

        // Then
        assertEquals(0, board.getCommentCount());  // 댓글 수가 1 감소
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardCommentRepository, times(1)).findById(boardId);
        verify(boardCommentRepository, times(1)).delete(boardComment);
    }
}