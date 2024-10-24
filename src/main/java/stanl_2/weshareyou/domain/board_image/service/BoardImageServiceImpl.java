package stanl_2.weshareyou.domain.board_image.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board_image.aggregate.dto.BoardImageDTO;
import stanl_2.weshareyou.domain.board_image.aggregate.entity.BoardImage;
import stanl_2.weshareyou.domain.board_image.repository.BoardImageRepository;
import stanl_2.weshareyou.domain.s3.S3uploader;

import java.util.ArrayList;
import java.util.List;

@Service("boardImageServiceImpl")
@Slf4j
public class BoardImageServiceImpl implements BoardImageService{

    private final BoardImageRepository boardImageRepository;
    private final S3uploader s3uploader;

    @Autowired
    public BoardImageServiceImpl(BoardImageRepository boardImageRepository, S3uploader s3uploader) {
        this.boardImageRepository = boardImageRepository;
        this.s3uploader = s3uploader;
    }


    @Override
    @Transactional
    public List<BoardImageDTO> uploadImages(List<MultipartFile> files, Board board) {

        List<BoardImage> images = s3uploader.uploadImg(files);

        for (BoardImage image : images) {
            image.setBoard(board);
            boardImageRepository.save(image);
        }

        List<BoardImage> savedImages = boardImageRepository.findAllByBoardId(board.getId());

        List<BoardImageDTO> imageObj = new ArrayList<>();

        for (BoardImage image : savedImages) {
            BoardImageDTO imageDTO = new BoardImageDTO(image.getId(), image.getImageUrl(), image.getName());
            imageObj.add(imageDTO);
        }

        return imageObj;
    }

    @Override
    @Transactional
    public void updateImages(List<Long> deletedFileIds) {

        List<BoardImage> imagesToDelete = boardImageRepository.findAllById(deletedFileIds);
        for (BoardImage image : imagesToDelete) {
            boardImageRepository.deleteAllByName(image.getName());
            s3uploader.deleteImg(image.getName());
        }

    }

    @Override
    @Transactional
    public void deleteImages(Board board) {

        List<BoardImage> imagesToBoardDelete = boardImageRepository.findAllByBoardId(board.getId());

        for(BoardImage image : imagesToBoardDelete) {
            s3uploader.deleteImg(image.getName());
        }

        boardImageRepository.deleteAllByBoardId(board.getId());
    }

}
