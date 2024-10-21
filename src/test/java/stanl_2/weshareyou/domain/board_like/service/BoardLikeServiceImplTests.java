//package stanl_2.weshareyou.domain.board_like.service;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.shadow.com.univocity.parsers.common.input.CharAppender;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.verification.VerificationMode;
//import org.springframework.boot.test.context.SpringBootTest;
//import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
//import stanl_2.weshareyou.domain.board.repository.BoardRepository;
//import stanl_2.weshareyou.domain.board_like.aggregate.dto.BoardLikeDto;
//import stanl_2.weshareyou.domain.board_like.aggregate.entity.BoardLike;
//import stanl_2.weshareyou.domain.board_like.aggregate.entity.BoardLikeId;
//import stanl_2.weshareyou.domain.board_like.repository.BoardLikeRepository;
//import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
//import stanl_2.weshareyou.domain.member.repository.MemberRepository;
//import stanl_2.weshareyou.global.common.exception.CommonException;
//import stanl_2.weshareyou.global.common.exception.ErrorCode;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//class BoardLikeServiceImplTests {
//    @Mock
//    private BoardLikeRepository boardLikeRepository;
//
//    @Mock
//    private BoardRepository boardRepository;
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @InjectMocks
//    private BoardLikeServiceImpl boardLikeService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testBoardLikeSuccess() {
//        // Given
//
//        BoardLikeDto boardLikeDto = new BoardLikeDto();
//        boardLikeDto.setBoardId(1L);
//        boardLikeDto.setMemberId(1L);
//
//        Board board = new Board();
//        board.setLikesCount(0);
//        Member member = new Member();
//
//        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        when(boardLikeRepository.findById(any(BoardLikeId.class))).thenReturn(Optional.empty());
//
//        // When
//        BoardLikeDto result = boardLikeService.BoardLike(boardLikeDto);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, board.getLikesCount());
//    }
//
//    private void assertEquals(int i, Integer likesCount) {
//
//    }
//
//    @Test
//    void testBoardLikeAlreadyLiked() {
//        // Given
//        BoardLikeDto boardLikeDto = new BoardLikeDto();
//        boardLikeDto.setBoardId(1L);
//        boardLikeDto.setMemberId(1L);
//
//        Board board = new Board();
//        Member member = new Member();
//
//        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        when(boardLikeRepository.findById(any(BoardLikeId.class))).thenReturn(Optional.of(new BoardLike()));
//
//        // When & Then
//        CommonException exception = assertThrows(CommonException.class, () -> {
//            boardLikeService.BoardLike(boardLikeDto);
//        });
//        Assertions.assertEquals(ErrorCode.ALREADY_LIKED, exception.getErrorCode());
//    }
//
//    @Test
//    void testBoardUnLikeSuccess() {
//        // Given
//        BoardLikeDto boardUnLikeDto = new BoardLikeDto();
//        boardUnLikeDto.setBoardId(1L);
//        boardUnLikeDto.setMemberId(1L);
//
//        Board board = new Board();
//        board.setLikesCount(1);
//        BoardLike boardLike = new BoardLike();
//
//        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
//        when(boardLikeRepository.findById(any(BoardLikeId.class))).thenReturn(Optional.of(boardLike));
//
//        // When
//        BoardLikeDto result = boardLikeService.BoardUnLike(boardUnLikeDto);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(0, board.getLikesCount());
//        verify(boardLikeRepository, times(1)).delete(1);
//    }
//
//    private CharAppender verify(BoardLikeRepository boardLikeRepository, VerificationMode times) {
//        return null;
//    }
//
//    @Test
//    void testBoardUnLikeNotFound() {
//        // Given
//        BoardLikeDto boardUnLikeDto = new BoardLikeDto();
//        boardUnLikeDto.setBoardId(1L);
//        boardUnLikeDto.setMemberId(1L);
//
//        Board board = new Board();
//
//        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
//        when(boardLikeRepository.findById(any(BoardLikeId.class))).thenReturn(Optional.empty());
//
//        // When & Then
//        CommonException exception = assertThrows(CommonException.class, () -> {
//            boardLikeService.BoardUnLike(boardUnLikeDto);
//        });
//        Assertions.assertEquals(ErrorCode.NOT_FOUND_LIKE, exception.getErrorCode());
//    }
//
//    @Test
//    void testBoardLikeList() {
//        // Given
//        Long boardId = 1L;
//        Board board = new Board();
//
//        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
//        when(boardLikeRepository.findMemberIdsByBoard(board)).thenReturn(List.of(1L, 2L, 3L));
//
//        // When
//        List<Long> memberIds = boardLikeService.BoardLikeList(boardId);
//
//        // Then
//        assertNotNull(memberIds);
//        assertEquals(3, memberIds.size());
//    }
//
//}