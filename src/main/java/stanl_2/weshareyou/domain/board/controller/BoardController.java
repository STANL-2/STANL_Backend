package stanl_2.weshareyou.domain.board.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.board.aggregate.dto.BoardDTO;
import stanl_2.weshareyou.domain.board.aggregate.entity.TAG;
import stanl_2.weshareyou.domain.board.aggregate.vo.request.BoardCreateRequestVO;
import stanl_2.weshareyou.domain.board.aggregate.vo.request.BoardDeleteRequestVO;
import stanl_2.weshareyou.domain.board.aggregate.vo.request.BoardUpdateRequestVO;
import stanl_2.weshareyou.domain.board.aggregate.vo.response.*;
import stanl_2.weshareyou.domain.board.service.BoardService;
import stanl_2.weshareyou.global.common.dto.CursorDTO;
import stanl_2.weshareyou.global.common.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;

@RestController(value = "boardController")
@RequestMapping("/api/v1/board")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final ModelMapper modelMapper;

    @Autowired
    public BoardController(BoardService boardService, ModelMapper modelMapper) {
        this.boardService = boardService;
        this.modelMapper = modelMapper;
    }

    /**
     * 내용: 게시글 생성
     * req:
     * {
     *     "title": "가이드 해드립니다.",
     *     "content": "안녕하세요, 가이드 5년차 홍길동입니다. 가이드가 필요하신분 연락주세요",
     *     "imageUrl": "image1",
     *     "tag": "GUIDE",
     *     "memberId": 1
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "title": "가이드 해드립니다.",
     *         "content": "안녕",
     *         "imageUrl": "image1",
     *         "tag": "GUIDE",
     *         "memberId": 1
     *     },
     *     "error": null
     * }
     */
    @PostMapping("")
    public ApiResponse<?> createBoard(@RequestAttribute("id") Long memberId,
                                      @RequestPart("vo") BoardCreateRequestVO boardCreateRequestVO,
                                      @RequestPart("file") List<MultipartFile> files){

        BoardDTO boardDTO = modelMapper.map(boardCreateRequestVO, BoardDTO.class);
        boardDTO.setMemberId(memberId);

        if(files != null) {
            List<MultipartFile> fileList = new ArrayList<>(files);
            boardDTO.setFile(fileList);
        }

        BoardDTO boardResponseDTO = boardService.createBoard(boardDTO);

        String formattedContent = boardResponseDTO.getContent().replace("\n", "\\n");
        boardResponseDTO.setContent(formattedContent);


        BoardCreateResponseVO boardCreateResponseVO = modelMapper.map(boardResponseDTO, BoardCreateResponseVO.class);

        return ApiResponse.ok(boardCreateResponseVO);
    }

    /**
     * 내용: 게시글 내용 수정
     * req:
     * {
     *     "id": 4,
     *     "title": "guide",
     *     "imageUrl": "image5",
     *     "memberId": 1
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "title": "guide",
     *         "content": "안녕하세요, 가이드 5년차 홍길동입니다. 가이드가 필요하신분 연락주세요",
     *         "imageUrl": "image5",
     *         "tag": "GUIDE",
     *         "memberId": 1
     *     },
     *     "error": null
     * }
     */
    @PutMapping("")
    public ApiResponse<?> updateBoard(@RequestAttribute("id") Long memberId,
                                      @RequestPart("vo") BoardUpdateRequestVO boardUpdateRequestVO,
                                      @RequestPart("newFiles") List<MultipartFile> files,
                                      @RequestPart("deletedFileIds") List<Long> deleteIds) {

        BoardDTO boardDTO = modelMapper.map(boardUpdateRequestVO, BoardDTO.class);

        boardDTO.setMemberId(memberId);

        if(files != null) {
            List<MultipartFile> fileList = new ArrayList<>(files);
            boardDTO.setFile(fileList);
        }

        if(deleteIds != null){
            List<Long> deleteId = new ArrayList<>(deleteIds);
            boardDTO.setDeleteIds(deleteId);
        }

        BoardDTO boardResponseDTO = boardService.updateBoard(boardDTO);

        BoardUpdateResponseVO boardUpdateResponseVO = modelMapper.map(boardResponseDTO, BoardUpdateResponseVO.class);

        return ApiResponse.ok(boardUpdateResponseVO);
    }

    /**
     * 내용: 게시글 삭제
     * req:
     * {
     *     "id": 4,
     *     "memberId": 1
     * }
     * res:
     *{
     *     "success": true,
     *     "result": {
     *         "id": 4,
     *         "active": false
     *     },
     *     "error": null
     * }
     */
    @DeleteMapping("")
    public ApiResponse<?> deleteBoard(@RequestAttribute("id") Long memberId,
                                      @RequestBody BoardDeleteRequestVO boardDeleteRequestVO){

        BoardDTO boardDTO = modelMapper.map(boardDeleteRequestVO, BoardDTO.class);
        boardDTO.setMemberId(memberId);

        BoardDTO boardResponseDTO = boardService.deleteBoard(boardDTO);

        BoardDeleteResponseVO boardDeleteResponseVO = modelMapper.map(boardResponseDTO, BoardDeleteResponseVO.class);

        return ApiResponse.ok(boardDeleteResponseVO);
    }

    /**
     * 내용: 게시글 상세조회
     * req: X
     * res: {
     *     "success": true,
     *     "result": {
     *         "imageUrl": "image1",
     *         "content": "A detailed guide to traveling in Paris.",
     *         "likesCount": 0,
     *         "memberProfileUrl": null,
     *         "memberNickname": "userone",
     *         "comment": [
     *             {
     *                 "content": "string",
     *                 "createdAt": "2024-10-11T15:54:42.000+00:00",
     *                 "updatedAt": "2024-10-11T15:54:42.907+00:00",
     *                 "memberNickname": null,
     *                 "boardId": null
     *             } , ...
     *         ]
     *     },
     *     "error": null
     * }
     */

    @GetMapping("/detail/{id}")
    public ApiResponse<?> readDetailBoard(@PathVariable Long id){

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(id);


        BoardDTO boardResponseDTO = boardService.readDetailBoard(boardDTO);

        BoardReadDetailResponseVO boardReadDetailResponseVO =
                modelMapper.map(boardResponseDTO,BoardReadDetailResponseVO.class);

        return ApiResponse.ok(boardReadDetailResponseVO);
    }

    /**
     * 내용: 게시글 조회(태그)
     * req: http://localhost:8080/api/v1/board/GUIDE/1?cursor=3&size=3
     * res:
     *  {
     *     "success": true,
     *     "result": {
     *         "tag": "GUIDE",
     *         "cursorId": 9,
     *         "comment": [
     *             {
     *                 "memberProfileUrl": null,
     *                 "memberNickname": "usertwo",
     *                 "title": "우도여행도 가능합니다",
     *                 "imageUrl": "제주도 이미지",
     *                 "commentCount": 0,
     *                 "likesCount": 0
     *             },
     *             {
     *                 "memberProfileUrl": null,
     *                 "memberNickname": "userthree",
     *                 "title": "혼자 처음 제주도 여행왔네요",
     *                 "imageUrl": "제주도 이미지",
     *                 "commentCount": 0,
     *                 "likesCount": 0
     *             },
     *             {
     *                 "memberProfileUrl": null,
     *                 "memberNickname": "userthree",
     *                 "title": "혼자 처음 제주도 여행왔네요",
     *                 "imageUrl": "제주도 이미지",
     *                 "commentCount": 0,
     *                 "likesCount": 0
     *             }
     *         ],
     *         "hasNext": true
     *     },
     *     "error": null
     * }
     */
    @GetMapping("/{tag}")
    public ApiResponse<?> readBoard(/*@RequestAttribute("id") Long id,*/
                                    @PathVariable TAG tag,
                                    @RequestParam(value = "cursor", required = false) Long cursorId,
                                    @RequestParam(value ="size", defaultValue = "4") Integer size){

        CursorDTO cursorDTO = new CursorDTO();
//        cursorDTO.setId(id);
        cursorDTO.setTag(tag);
        cursorDTO.setCursorId(cursorId);
        cursorDTO.setSize(size);

        CursorDTO responseCursorDTO = boardService.readBoard(cursorDTO);

        BoardReadResponseVO boardReadResponseVO = modelMapper.map(responseCursorDTO, BoardReadResponseVO.class);

        return ApiResponse.ok(boardReadResponseVO);
    }
}
