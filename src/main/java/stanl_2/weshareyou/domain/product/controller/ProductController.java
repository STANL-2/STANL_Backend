package stanl_2.weshareyou.domain.product.controller;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import stanl_2.weshareyou.domain.alarm.service.AlarmService;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.product.aggregate.dto.ProductDTO;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductCreateRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductDeleteRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductUpdateRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.response.*;
import stanl_2.weshareyou.domain.product.service.ProductService;
import stanl_2.weshareyou.global.common.response.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/product")
@Slf4j
public class ProductController {

    private final ModelMapper modelMapper;
    private final ProductService productService;
    private final AlarmService alarmService;

    @Autowired
    public ProductController(ModelMapper modelMapper, ProductService productService, AlarmService alarmService) {
        this.modelMapper = modelMapper;
        this.productService = productService;
        this.alarmService = alarmService;
    }

    /**
     * 내용: 공유물품 등록
     * req:
     * {
     *     "title": "스탠리 텀블러",
     *     "content": "스탠리 상태 좋습니다.",
     *     "imageUrl": "tumbler_image_url",
     *     "category": "TOY",
     *     "startAt": "2024-10-08T00:00:00",
     *     "endAt": "2024-10-10T00:00:00",
     *     "adminId": 1
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "title": "스탠리 텀블러2",
     *         "content": "스탠리 상태 좋습니다2.",
     *         "imageUrl": "tumbler_image_url",
     *         "category": "TOY",
     *         "startAt": "2024-10-08T00:00:00",
     *         "endAt": "2024-10-10T00:00:00",
     *         "adminId": 1
     *     },
     *     "error": null
     * }
     */
    @PostMapping("")
    public ApiResponse<?> createProduct(@RequestBody ProductCreateRequestVO productCreateRequestVO,
                                        @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = new ProductDTO();
        productRequestDTO.setAdminId(id);
        productRequestDTO.setTitle(productCreateRequestVO.getTitle());
        productRequestDTO.setContent(productCreateRequestVO.getContent());
        productRequestDTO.setCategory(productCreateRequestVO.getCategory());
        productRequestDTO.setStartAt(productCreateRequestVO.getStartAt());
        productRequestDTO.setEndAt(productCreateRequestVO.getEndAt());
        productRequestDTO.setImageUrl(productCreateRequestVO.getImageUrl());

        ProductDTO productResponseDTO = productService.createProduct(productRequestDTO);

        ProductCreateResponseVO productCreateResponseVO = modelMapper.map(productResponseDTO, ProductCreateResponseVO.class);

        return ApiResponse.ok(productCreateResponseVO);
    }

    /**
     * 내용: 공유물품 수정
     * req:
     * {
     *     "id": 4,
     *     "title": "스탠리 텀블러1",
     *     "content": "스탠리 상태 좋습니다1.",
     *     "imageUrl": "tumbler_image_url",
     *     "category": "KITCHENWARES",
     *     "startAt": "2024-10-08T00:00:00",
     *     "endAt": "2024-10-10T00:00:00",
     *     "adminId": 1
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 4,
     *         "title": "스탠리 텀블러1",
     *         "content": "스탠리 상태 좋습니다1.",
     *         "imageUrl": "tumbler_image_url",
     *         "category": "KITCHENWARES",
     *         "startAt": "2024-10-08T00:00:00",
     *         "endAt": "2024-10-10T00:00:00",
     *         "adminId": 1
     *     },
     *     "error": null
     * }
     */
    @PutMapping("")
    public ApiResponse<?> updateProduct(@RequestBody ProductUpdateRequestVO productUpdateRequestVO,
                                        @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = modelMapper.map(productUpdateRequestVO, ProductDTO.class);
        productRequestDTO.setAdminId(id);
        ProductDTO productResponseDTO = productService.updateProduct(productRequestDTO);

        ProductUpdateResponseVO productUpdateResponseVO = modelMapper.map(productResponseDTO, ProductUpdateResponseVO.class);

        return ApiResponse.ok(productUpdateResponseVO);
    }

    /**
     * 내용: 공유물품 삭제
     * req:
     * {
     *     "id": 5,
     *     "adminId": 1
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 5,
     *         "adminId": 1
     *     },
     *     "error": null
     * }
     */
    @DeleteMapping("")
    public ApiResponse<?> deleteProduct(@RequestBody ProductDeleteRequestVO productDeleteRequestVO,
                                        @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = modelMapper.map(productDeleteRequestVO, ProductDTO.class);
        productRequestDTO.setAdminId(id);
        ProductDTO productResponseDTO = productService.deleteProduct(productRequestDTO);

        ProductDeleteResponseVO productDeleteResponse = modelMapper.map(productResponseDTO, ProductDeleteResponseVO.class);

        return ApiResponse.ok(productDeleteResponse);
    }

    /**
     * 내용: 공유물품 전체 조회
     * req:
     * res:
     * {
     *     "success": true,
     *     "result": [
     *         {
     *             "id": 1,
     *             "title": "스탠리 텀블러1",
     *             "content": "스탠리 상태 좋습니다1.",
     *             "imageUrl": null,
     *             "category": "KITCHENWARES",
     *             "startAt": "2024-10-08T00:00:00",
     *             "endAt": "2024-10-10T00:00:00",
     *             "createdAt": null,
     *             "updatedAt": "2024-10-09T16:43:47",
     *             "adminId": 1,
     *             "memberId": null
     *         },
     *         {
     *             "id": 2,
     *             "title": "Winter Jacket",
     *             "content": "A warm winter jacket.",
     *             "imageUrl": "jacket_image_url",
     *             "category": "CLOTHES",
     *             "startAt": "2024-10-01T00:00:00",
     *             "endAt": "2024-11-01T00:00:00",
     *             "createdAt": "2024-10-08T12:00:00",
     *             "updatedAt": "2024-10-08T12:00:00",
     *             "adminId": 1,
     *             "memberId": 3
     *         },
     *     ],
     *     "error": null
     * }
     */
    @GetMapping("")
    public ApiResponse<?> readAllProductList() {

        List<ProductDTO> productDTOList = productService.readAllProductList();

        List<ProductReadAllResponseVO> productReadResponseVOList = productDTOList.stream()
                .map(productList -> modelMapper.map(productList, ProductReadAllResponseVO.class))
                .collect(Collectors.toList());

        return ApiResponse.ok(productReadResponseVOList);
    }

    /**
     * 내용: 공유물품 상세 조회
     * req: localhost:8080/api/v1/product/4
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 4,
     *         "title": "스탠리 텀블러1",
     *         "content": "스탠리 상태 좋습니다1.",
     *         "imageUrl": jacket_image_url,
     *         "category": "KITCHENWARES",
     *         "startAt": "2024-10-08T00:00:00",
     *         "endAt": "2024-10-10T00:00:00",
     *         "rental": false,
     *         "createdAt": null,
     *         "updatedAt": "2024-10-09T17:21:47",
     *         "adminId": 1,
     *         "memberId": null
     *     },
     *     "error": null
     * }
     */
    @GetMapping("/{productId}")
    public ApiResponse<?> readProduct(@PathVariable Long productId) {

        ProductDTO productDTO = productService.readProduct(productId);

        ProductReadResponseVO productReadResponseVO = modelMapper.map(productDTO, ProductReadResponseVO.class);

        return ApiResponse.ok(productReadResponseVO);
    }

    /**
     * 내용: 공유물품 카테고리별 검색
     * req: localhost:8080/api/v1/product/category/TOY
     * res:
     * {
     *     "success": true,
     *     "result": [
     *         {
     *             "id": 6,
     *             "title": "스탠리 텀블러",
     *             "imageUrl": jacket_image_url,
     *             "category": "TOY",
     *             "rental": false
     *         },
     *         {
     *             "id": 7,
     *             "title": "스탠리 텀블러",
     *             "imageUrl": jacket_image_url,
     *             "category": "TOY",
     *             "rental": false
     *         }
     *     ],
     *     "error": null
     * }
     */
    @GetMapping("/category/{category}")
    public ApiResponse<?> readProductByCategory(@PathVariable String category) {

        List<ProductDTO> productDTOList = productService.readProductByCategory(category);

        List<ProductReadCategoryResponseVO> productReadCategoryResponseVOList = productDTOList.stream()
                .map(productList -> modelMapper.map(productList, ProductReadCategoryResponseVO.class))
                .collect(Collectors.toList());

        return ApiResponse.ok(productReadCategoryResponseVOList);
    }

    /**
     * 내용: 공유물품 대여신청
     * req: localhost:8080/api/v1/product/share/1?memberId=2
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 1,
     *         "memberId": 2,
     *         "rental": false
     *     },
     *     "error": null
     * }
     * {
     *     "success": false,
     *     "result": null,
     *     "error": {
     *         "code": 40010,
     *         "message": "이미 대여된 물품입니다."
     *     }
     * }
     */
    @PutMapping("/share/{productId}")
    public ApiResponse<?> updateRentalProduct(@PathVariable Long productId,
                                              @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = new ProductDTO();
        productRequestDTO.setId(productId);
        productRequestDTO.setMemberId(id);
        ProductDTO productResponseDTO = productService.updateRentalProduct(productRequestDTO);

        alarmService.sendRentalAlarm(productResponseDTO);

        alarmService.sendRentalAlarm(productResponseDTO, memberId);

        ProductRentalResponseVO productRentalResponseVO = modelMapper.map(productResponseDTO, ProductRentalResponseVO.class);

        return ApiResponse.ok(productRentalResponseVO);
    }

    /**
     * 내용: 공유물품 대여승인
     * req: localhost:8080/api/v1/product/share/approve/1?adminId=1
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 1,
     *         "rental": true,
     *         "memberId": 2
     *     },
     *     "error": null
     * }
     */
    @PutMapping("/share/approve/{productId}")
    public ApiResponse<?> updateRentalApproveProduct(@PathVariable Long productId,
                                                     @RequestAttribute("id") Long adminId) {

        ProductDTO productRequestDTO = new ProductDTO();
        productRequestDTO.setId(productId);
        productRequestDTO.setAdminId(adminId);
        ProductDTO productResponseDTO = productService.updateRentalApproveProduct(productRequestDTO);

        ProductRentalApproveResponseVO productRentalApproveResponseVO = modelMapper.map(productResponseDTO, ProductRentalApproveResponseVO.class);

        return ApiResponse.ok(productRentalApproveResponseVO);
    }

    /**
     * 내용: 공유물품 대여반납
     * req: localhost:8080/api/v1/product/share/return/1?adminId=1
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 1,
     *         "rental": false,
     *         "memberId": null
     *     },
     *     "error": null
     * }
     */
    @PutMapping("/share/return/{productId}")
    public ApiResponse<?> updateRentalReturnProduct(@PathVariable Long productId,
                                                    @RequestAttribute("id") Long adminId) {

        ProductDTO productRequestDTO = new ProductDTO();
        productRequestDTO.setId(productId);
        productRequestDTO.setAdminId(adminId);
        ProductDTO productResponseDTO = productService.updateRentalReturnProduct(productRequestDTO);

        ProductRentalReturnResponseVO productRentalReturnResponseVO = modelMapper.map(productResponseDTO, ProductRentalReturnResponseVO.class);

        return ApiResponse.ok(productRentalReturnResponseVO);
    }
}
