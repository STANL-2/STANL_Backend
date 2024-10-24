package stanl_2.weshareyou.domain.board.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.board.aggregate.dto.BoardDTO;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board.repository.BoardRepository;
import stanl_2.weshareyou.domain.board_comment.aggregate.dto.BoardCommentDto;
import stanl_2.weshareyou.domain.board_comment.aggregate.entity.BoardComment;
import stanl_2.weshareyou.domain.board_comment.repository.BoardCommentRepository;
import stanl_2.weshareyou.domain.board_image.aggregate.dto.BoardImageDTO;
import stanl_2.weshareyou.domain.board_image.aggregate.entity.BoardImage;
import stanl_2.weshareyou.domain.board_image.repository.BoardImageRepository;
import stanl_2.weshareyou.domain.board_image.service.BoardImageService;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.domain.s3.S3uploader;
import stanl_2.weshareyou.global.common.dto.CursorDTO;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("boardServiceImpl")
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final S3uploader s3uploader;
    private final BoardImageRepository boardImageRepository;
    private final BoardImageService boardImageService;
    private Timestamp getCurrentTimestamp() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Timestamp.from(nowKst.toInstant());
    }

    @Autowired
    public BoardServiceImpl(BoardRepository boardRepository, ModelMapper modelMapper,
                            MemberRepository memberRepository, BoardCommentRepository boardCommentRepository,
                            S3uploader s3uploader, BoardImageRepository boardImageRepository,
                            BoardImageService boardImageService) {
        this.boardRepository = boardRepository;
        this.modelMapper = modelMapper;
        this.memberRepository = memberRepository;
        this.boardCommentRepository = boardCommentRepository;
        this.s3uploader = s3uploader;
        this.boardImageRepository = boardImageRepository;
        this.boardImageService = boardImageService;
    }

    @Override
    @Transactional
    public BoardDTO createBoard(BoardDTO boardDTO) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        Member member = memberRepository.findById(boardDTO.getMemberId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Board board = new Board();
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setTag(boardDTO.getTag());
        board.setCommentCount(0);
        board.setLikesCount(0);
        board.setCreatedAt(currentTimestamp);
        board.setUpdatedAt(currentTimestamp);
        board.setActive(true);
        board.setMember(member);

        boardRepository.save(board);

        BoardDTO boardResponseDTO = new BoardDTO();
        boardResponseDTO.setTitle(board.getTitle());
        boardResponseDTO.setContent(board.getContent());
        boardResponseDTO.setTag(board.getTag());

        List<MultipartFile> files = boardDTO.getFile();

        if(files != null && !files.isEmpty()) {
            List<BoardImageDTO> imageObj = boardImageService.uploadImages(files, board);
            boardResponseDTO.setImageObj(imageObj);
        }

        return boardResponseDTO;
    }

    @Override
    @Transactional
    public BoardDTO updateBoard(BoardDTO boardDTO) {

        Timestamp currentTimestamp = getCurrentTimestamp();

        List<Long> deletedFileIds = boardDTO.getDeleteIds();

        if (deletedFileIds != null && !deletedFileIds.isEmpty()) {
            boardImageService.updateImages(deletedFileIds);
        }

        Board board = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));

        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setTag(boardDTO.getTag());
        board.setUpdatedAt(currentTimestamp);

        boardRepository.save(board);

        BoardDTO boardResponseDTO = modelMapper.map(board, BoardDTO.class);

        List<MultipartFile> files = boardDTO.getFile();

        if(files != null && !files.isEmpty()) {
            List<BoardImageDTO> imageObj = boardImageService.uploadImages(files, board);
            boardResponseDTO.setImageObj(imageObj);
        }

        return boardResponseDTO;
    }

    @Override
    @Transactional
    public BoardDTO deleteBoard(BoardDTO boardDTO) {

        Timestamp currentTimestamp = getCurrentTimestamp();

        Board board = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));

        board.setActive(false);
        board.setUpdatedAt(currentTimestamp);

        boardRepository.save(board);

        boardImageService.deleteImages(board);

        BoardDTO boardResponseDTO = modelMapper.map(board, BoardDTO.class);

        return boardResponseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDTO readDetailBoard(BoardDTO boardDTO) {

        Board board = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.BOARD_NOT_FOUND));

        List<BoardImage> savedImages = boardImageRepository.findAllByBoardId(board.getId());
        List<BoardImageDTO> imageObj = new ArrayList<>();

        for (BoardImage image : savedImages) {
            BoardImageDTO imageDTO
                    = new BoardImageDTO(image.getId(), image.getImageUrl(), image.getName());
            imageObj.add(imageDTO);
        }

        List<BoardComment> boardComments = boardCommentRepository.findByBoardId(board.getId());

        List<BoardCommentDto> boardCommentDTOs = boardComments.stream()
                .map(comment -> modelMapper.map(comment, BoardCommentDto.class))
                .collect(Collectors.toList());

        BoardDTO boardResponseDTO = new BoardDTO();
        boardResponseDTO.setImageObj(imageObj);
        boardResponseDTO.setContent(board.getContent());
        boardResponseDTO.setLikesCount(board.getLikesCount());
        boardResponseDTO.setMemberProfileUrl(board.getMember().getProfileUrl());
        boardResponseDTO.setMemberNickname(board.getMember().getNickname());
        boardResponseDTO.setComment(boardCommentDTOs);
        boardResponseDTO.setMemberId(boardDTO.getMemberId());
        boardResponseDTO.setTitle(board.getTitle());

        return boardResponseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public CursorDTO readBoard(CursorDTO cursorDTO) {

        Pageable pageable = PageRequest.of(0, cursorDTO.getSize());
        Slice<Board> boardList;

        if(cursorDTO.getCursorId() == null){
            boardList = boardRepository.findByTagOrderByCreatedAtDesc(cursorDTO.getTag(), pageable);
        } else {
            boardList = boardRepository.findByTagAndIdLessThanOrderByCreatedAtDesc
                    (cursorDTO.getTag(), cursorDTO.getCursorId(), pageable);
        }

        Long lastBoardId = boardList.getContent().isEmpty() ? null :
                boardList.getContent().get(boardList.getNumberOfElements() - 1).getId();



        List<BoardDTO> boardDTOList = boardList.getContent().stream()
                .map(board -> {

                    List<BoardImage> savedImages = boardImageRepository.findAllByBoardId(board.getId());
                    List<BoardImageDTO> imageObj = new ArrayList<>();

                    for (BoardImage image : savedImages) {
                        BoardImageDTO imageDTO
                                = new BoardImageDTO(image.getId(), image.getImageUrl(), image.getName());
                        imageObj.add(imageDTO);
                    }

                    BoardDTO boardDTO = new BoardDTO();
                    boardDTO.setId(board.getId());
                    boardDTO.setMemberProfileUrl(board.getMember().getProfileUrl());
                    boardDTO.setMemberNickname(board.getMember().getNickname());
                    boardDTO.setImageObj(imageObj);
                    boardDTO.setTitle(board.getTitle());
                    boardDTO.setContent(board.getContent());

                    boardDTO.setLikesCount(board.getLikesCount());
                    boardDTO.setCommentCount(board.getCommentCount());
                    boardDTO.setMemberId(board.getMember().getId());
                    boardDTO.setActive(board.getActive());
                    return boardDTO;
                })
                .collect(Collectors.toList());

        CursorDTO cursorResponseDTO = new CursorDTO();
        cursorResponseDTO.setCursorId(lastBoardId);
        cursorResponseDTO.setHasNext(boardList.hasNext());
        cursorResponseDTO.setTag(cursorDTO.getTag());
        cursorResponseDTO.setComment(boardDTOList);

        return cursorResponseDTO;
    }
}
