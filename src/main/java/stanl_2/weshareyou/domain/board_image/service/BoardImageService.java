package stanl_2.weshareyou.domain.board_image.service;

import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board_image.aggregate.dto.BoardImageDTO;
import stanl_2.weshareyou.domain.board_image.aggregate.entity.BoardImage;

import java.util.List;

public interface BoardImageService {

    void updateImages(List<Long> deletedFileIds);

    void deleteImages(Board board);

    List<BoardImageDTO> uploadImages(List<MultipartFile> files, Board board);

    List<BoardImageDTO> readImages(Board board);
}
