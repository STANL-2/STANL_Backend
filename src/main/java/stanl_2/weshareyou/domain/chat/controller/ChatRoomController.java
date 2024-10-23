package stanl_2.weshareyou.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import stanl_2.weshareyou.domain.chat.entity.ChatRoom;
import stanl_2.weshareyou.domain.chat.entity.ChatRoomMessage;
import stanl_2.weshareyou.domain.chat.service.ChatRoomMessageService;
import stanl_2.weshareyou.domain.chat.service.ChatRoomService;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.global.common.response.ApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("api/v1/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomMessageService chatRoomMessageService;
    private final MemberRepository memberRepository;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("")
    @Operation(summary="채팅방 조회하기")
    public Map<String, Object> showRooms(@RequestAttribute("nickname") String nickname) {
        List<ChatRoom> rooms = chatRoomService.findRoomsByUser(nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("rooms", rooms);
        response.put("user", nickname);

        return response; // JSON 데이터 반환
    }
    @GetMapping("/{roomId}")
    public Map<String, Object> roomDetail(@PathVariable String roomId
                                ,@RequestAttribute("nickname") String nickname) {
        ChatRoom room = chatRoomService.findRoomById(roomId);

        ChatRoomMessage messages = chatRoomMessageService.getMessagesByRoomId(roomId);

        chatRoomMessageService.markMessagesAsRead(roomId, nickname);


        Map<String, Object> response = new HashMap<>();
        response.put("room", room);
        response.put("messages", messages.getMessages());
        response.put("nickname", messages.getMessages());
        response.put("profileUrl", messages.getMessages());

        System.out.println(response);
        return response;
    }

    // 새로운 채팅방 생성
//    @PostMapping("")
//    public ChatRoom createRoom(@RequestBody Map<String, String> requestBody){
//        String sender = requestBody.get("sender");
//        String receiver = requestBody.get("receiver");
//        String senderProfileUrl = memberRepository.findProfileUrlByNickname(sender);
//        String receiverProfileUrl = memberRepository.findProfileUrlByNickname(receiver);
//
//        return chatRoomService.createChatRoom(sender, receiver,senderProfileUrl,receiverProfileUrl);
//    }

    // 선택 채팅방 삭제
    /* 설명. 선택 채팅방 삭제 시 sender, receiver 각각에 delete가 있어야한다*/
    @DeleteMapping("/{roomId}")
    public ApiResponse<?> deleteRoom(@PathVariable String roomId
                              , @RequestAttribute("nickname") String nickname) {

        if(chatRoomService.deleteChatRoom(roomId, nickname)){
            return ApiResponse.ok("delete success");
        }
        return ApiResponse.ok("delete fail");
    }
}
