package stanl_2.weshareyou.domain.product.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.domain.product.aggregate.dto.ProductDTO;
import stanl_2.weshareyou.domain.product.aggregate.entity.Product;
import stanl_2.weshareyou.domain.product.repository.ProductRepository;
import stanl_2.weshareyou.domain.s3.S3uploader;
import stanl_2.weshareyou.global.common.dto.CursorDTO;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3uploader s3uploader;

    private Timestamp getCurrentTimestamp() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Timestamp.from(nowKst.toInstant());
    }

    @Autowired
    public ProductServiceImpl(ModelMapper modelMapper, ProductRepository productRepository, MemberRepository memberRepository, S3uploader s3uploader) {
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3uploader = s3uploader;
    }

    public ProductDTO toProductDTO(Product product) {
        ProductDTO productResponseDTO = new ProductDTO();
        productResponseDTO.setId(product.getId());
        productResponseDTO.setTitle(product.getTitle());
        productResponseDTO.setContent(product.getContent());
        productResponseDTO.setCategory(product.getCategory());
        productResponseDTO.setStartAt(product.getStartAt());
        productResponseDTO.setEndAt(product.getEndAt());
        productResponseDTO.setImageUrl(product.getImageUrl());
        productResponseDTO.setCreatedAt(product.getCreatedAt());
        productResponseDTO.setUpdatedAt(product.getUpdatedAt());
        productResponseDTO.setAdminId(product.getAdminId().getId());
        productResponseDTO.setRental(product.isRental());
        productResponseDTO.setStatus(product.getStatus());

        return productResponseDTO;
    }

    private String getKey(String url){
        for(int i=0;i<url.length()-15;i++){
            if(url.substring(i, i+15).equals(".amazonaws.com/")){
                return url.substring(i+15, url.length());
            }
        }
        return null;
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile file) {
        Timestamp currentTimestamp = getCurrentTimestamp();
        Member member = memberRepository.findById(productDTO.getAdminId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = new Product();
        product.setTitle(productDTO.getTitle());
        product.setContent(productDTO.getContent());
        product.setCategory(productDTO.getCategory());
        product.setStartAt(currentTimestamp);
        product.setEndAt(currentTimestamp);
        product.setStatus(productDTO.getStatus());
        product.setAdminId(member);
        product.setCreatedAt(currentTimestamp);
        product.setUpdatedAt(currentTimestamp);
        product.setStatus(productDTO.getStatus());

        String imageUrl = s3uploader.uploadOneImage(file);
        product.setImageUrl(imageUrl);

        productRepository.save(product);
        ProductDTO productResponseDTO = toProductDTO(product);

        return productResponseDTO;
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(ProductDTO productDTO, MultipartFile file) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        Member member = memberRepository.findById(productDTO.getAdminId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findByIdAndAdminId(productDTO.getId(), member)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_AUTHOR_NOT_FOUND));

        product.setTitle(productDTO.getTitle());
        product.setContent(productDTO.getContent());
        product.setCategory(productDTO.getCategory());
        product.setStartAt(productDTO.getStartAt());
        product.setEndAt(productDTO.getEndAt());
        product.setStatus(productDTO.getStatus());
        product.setCreatedAt(currentTimestamp);
        product.setUpdatedAt(currentTimestamp);
        product.setAdminId(member);

        String key = product.getImageUrl();
        if(key == null){
            throw new CommonException(ErrorCode.BAD_REQUEST_IMAGE);
        }
        s3uploader.deleteImg(key);
        String url = s3uploader.uploadOneImage(file);
        product.setImageUrl(url);

        productRepository.save(product);

        ProductDTO productResponseDTO = toProductDTO(product);

        return productResponseDTO;
    }

    @Override
    @Transactional
    public ProductDTO deleteProduct(ProductDTO productDTO) {

        Member member = memberRepository.findById(productDTO.getAdminId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findByIdAndAdminId(productDTO.getId(), member)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_AUTHOR_NOT_FOUND));

        product.setId(productDTO.getId());
        product.setAdminId(member);

        productRepository.delete(product);

        s3uploader.deleteImg(product.getImageUrl());

        ProductDTO productResponseDTO = toProductDTO(product);

        return productResponseDTO;
    }

    @Override
    @Transactional
    public ProductDTO readProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductDTO productResponseDTO = toProductDTO(product);

        return productResponseDTO;
    }

    @Override
    @Transactional
    public CursorDTO readProductByCategory(CursorDTO cursorDTO) {

        Pageable pageable = PageRequest.of(0, cursorDTO.getSize());
        Slice<Product> productList;

        if (cursorDTO.getCursorId() == null) {
            productList = productRepository.findByCategoryOrderByCreatedAtDesc(cursorDTO.getCategory(), pageable);
        } else {
            productList = productRepository.findByCategoryAndIdLessThanOrderByCreatedAtDesc(
                    cursorDTO.getCategory(), cursorDTO.getCursorId(), pageable
            );
        }

        Long lastProductId = productList.getContent().isEmpty() ? null :
                productList.getContent().get(productList.getNumberOfElements() - 1).getId();

        if (productList.isEmpty()) {
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        } else {
            List<ProductDTO> productDTOList = productList.stream()
                    .map(this::toProductDTO)
                    .collect(Collectors.toList());

            CursorDTO cursorResponseDTO = new CursorDTO();
            cursorResponseDTO.setCursorId(lastProductId);
            cursorResponseDTO.setHasNext(productList.hasNext());
            cursorResponseDTO.setCategory(cursorDTO.getCategory());
            cursorResponseDTO.setComment(productDTOList);

            return cursorResponseDTO;
        }
    }

    @Override
    @Transactional
    public ProductDTO updateRentalProduct(ProductDTO productDTO) {

        Member member = memberRepository.findById(productDTO.getMemberId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.isRental()) {
            throw new CommonException(ErrorCode.PRODUCT_IS_RENTAL);
        } else {
            product.setMemberId(member);
            product.setRental(true);
            product.setStartAt(productDTO.getStartAt());
            product.setEndAt(productDTO.getEndAt());

            productRepository.save(product);

            ProductDTO productResponseDTO = new ProductDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setRental(product.isRental());
            productResponseDTO.setMemberId(product.getMemberId().getId());
            productResponseDTO.setTitle(product.getTitle());
            productResponseDTO.setStartAt(product.getStartAt());
            productResponseDTO.setEndAt(product.getEndAt());

            return productResponseDTO;
        }
    }

    @Override
    @Transactional
    public ProductDTO updateRentalApproveProduct(ProductDTO productDTO) {

        Member member = memberRepository.findById(productDTO.getAdminId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isRental() && product.getMemberId() != null) {
            product.setRental(true);

            productRepository.save(product);

            ProductDTO productResponseDTO = new ProductDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setRental(product.isRental());
            productResponseDTO.setMemberId(product.getMemberId().getId());

            return productResponseDTO;
        } else {
            throw new CommonException(ErrorCode.PRODUCT_IS_RENTAL);
        }
    }

    @Override
    @Transactional
    public ProductDTO updateRentalReturnProduct(ProductDTO productDTO) {

        Member member = memberRepository.findById(productDTO.getAdminId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.isRental() && product.getMemberId() != null) {
            product.setRental(false);
            product.setMemberId(null);

            productRepository.save(product);

            ProductDTO productResponseDTO = new ProductDTO();
            productResponseDTO.setId(product.getId());
            productResponseDTO.setRental(product.isRental());

            if (product.getMemberId() != null) {
                throw new CommonException(ErrorCode.PRODUCT_IS_NOT_RETURN);
            } else {
                productResponseDTO.setMemberId(null);
            }
            return productResponseDTO;

        } else {
            throw new CommonException(ErrorCode.PRODUCT_IS_RETURN);
        }
    }

}
