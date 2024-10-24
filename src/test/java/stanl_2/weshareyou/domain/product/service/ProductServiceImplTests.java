package stanl_2.weshareyou.domain.product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.domain.product.aggregate.dto.ProductDTO;
import stanl_2.weshareyou.domain.product.aggregate.entity.Product;
import stanl_2.weshareyou.domain.product.repository.ProductRepository;
import stanl_2.weshareyou.domain.s3.S3uploader;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceImplTests {

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private S3uploader s3uploader;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Test
    @DisplayName("상품 생성 테스트")
    void testCreateProduct() {
        // given
        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("New Product");
        productDTO.setContent("New Product Content");
        productDTO.setAdminId(1L);

        Member admin = new Member();
        admin.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setTitle(productDTO.getTitle());
        product.setContent(productDTO.getContent());
        product.setAdminId(admin);
        product.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        product.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        MultipartFile file = mock(MultipartFile.class);
        String imageUrl = "https://example.com/image.jpg";

        when(memberRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(s3uploader.uploadOneImage(file)).thenReturn(imageUrl);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        ProductDTO result = productService.createProduct(productDTO, file);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getAdminId());
        assertEquals("New Product", result.getTitle());
        verify(s3uploader, times(1)).uploadOneImage(file);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 테스트")
    void testDeleteProduct() {
        // given
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setAdminId(1L);

        Member admin = new Member();
        admin.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setAdminId(admin);
        product.setImageUrl("https://example.com/image.jpg");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(productRepository.findByIdAndAdminId(1L, admin)).thenReturn(Optional.of(product));

        // when
        ProductDTO result = productService.deleteProduct(productDTO);

        // then
        assertNotNull(result);
        verify(s3uploader, times(1)).deleteImg("https://example.com/image.jpg");
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("상품 대여 업데이트 테스트")
    void testUpdateRentalProduct() {
        // given
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setStartAt(Timestamp.valueOf("2024-10-10 10:00:00"));
        productDTO.setEndAt(Timestamp.valueOf("2024-10-20 10:00:00"));
        productDTO.setMemberId(1L);

        Member member = new Member();
        member.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setTitle("Test Product");
        product.setContent("Test Content");
        product.setRental(false); // 상품이 아직 대여 중이 아님
        product.setMemberId(member);

        // 상품이 이미 대여 중이라면 예외 발생
        when(productRepository.findById(productDTO.getId())).thenReturn(Optional.of(product));
        when(memberRepository.findById(productDTO.getMemberId())).thenReturn(Optional.of(member));

        // when
        ProductDTO result = productService.updateRentalProduct(productDTO);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getMemberId());
        assertTrue(result.getRental());
        assertEquals("Test Product", result.getTitle());
        assertEquals(Timestamp.valueOf("2024-10-10 10:00:00"), result.getStartAt());
        assertEquals(Timestamp.valueOf("2024-10-20 10:00:00"), result.getEndAt());

        verify(productRepository, times(1)).save(product);
    }
}
