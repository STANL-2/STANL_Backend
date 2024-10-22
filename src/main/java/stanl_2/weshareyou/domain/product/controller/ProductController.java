package stanl_2.weshareyou.domain.product.controller;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.alarm.service.AlarmService;
import stanl_2.weshareyou.domain.product.aggregate.dto.ProductDTO;
import stanl_2.weshareyou.domain.product.aggregate.entity.ProductCategory;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductCreateRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductDeleteRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductRentalRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.request.ProductUpdateRequestVO;
import stanl_2.weshareyou.domain.product.aggregate.vo.response.*;
import stanl_2.weshareyou.domain.product.service.ProductService;
import stanl_2.weshareyou.global.common.dto.CursorDTO;
import stanl_2.weshareyou.global.common.response.ApiResponse;

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
     *     "category": "TOY",
     *     "status": "RENTAL",
     *     "startAt": "2024-10-08T00:00:00",
     *     "endAt": "2024-10-10T00:00:00"
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 8,
     *         "title": "스탠리 텀블러",
     *         "content": "스탠리 상태 좋습니다.",
     *         "imageUrl": "https://weshareubk.s3.ap-northeast-2.amazonaws.com/c24acf7d-037b-4bae-9893-1cea36f89c34.png",
     *         "category": "TOY",
     *         "status": "RENTAL",
     *         "startAt": "2024-10-08T00:00:00.000+00:00",
     *         "endAt": "2024-10-10T00:00:00.000+00:00",
     *         "adminId": 4
     *     },
     *     "error": null
     * }
     */
    @PostMapping("")
    public ApiResponse<?> createProduct(@RequestPart("file") MultipartFile imageUrl,
                                        @RequestPart("vo") ProductCreateRequestVO productCreateRequestVO,
                                        @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = new ProductDTO();
        productRequestDTO.setAdminId(id);
        productRequestDTO.setTitle(productCreateRequestVO.getTitle());
        productRequestDTO.setContent(productCreateRequestVO.getContent());
        productRequestDTO.setCategory(productCreateRequestVO.getCategory());
        productRequestDTO.setStatus(productCreateRequestVO.getStatus());

        ProductDTO productResponseDTO = productService.createProduct(productRequestDTO, imageUrl);

        ProductCreateResponseVO productCreateResponseVO = modelMapper.map(productResponseDTO, ProductCreateResponseVO.class);

        return ApiResponse.ok(productCreateResponseVO);
    }

    /**
     * 내용: 공유물품 수정
     * req:
     * {
     *     "id":8,
     *     "title": "스탠리 텀블러1",
     *     "content": "스탠리 상태 좋습니다1.",
     *     "status": "RENTAL",
     *     "category": "KITCHENWARES",
     *     "startAt": "2024-10-08T00:00:00",
     *     "endAt": "2024-10-10T00:00:00"
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 8,
     *         "title": "스탠리 텀블러1",
     *         "content": "스탠리 상태 좋습니다1.",
     *         "imageUrl": "https://weshareubk.s3.ap-northeast-2.amazonaws.com/937afb52-6ecb-4eaf-ae0a-a2ae1839edbe.png",
     *         "category": "KITCHENWARES",
     *         "status": "RENTAL",
     *         "startAt": "2024-10-08T00:00:00.000+00:00",
     *         "endAt": "2024-10-10T00:00:00.000+00:00",
     *         "adminId": 4
     *     },
     *     "error": null
     * }rror": null
     * }
     */
    @PutMapping("")
    public ApiResponse<?> updateProduct(@RequestPart("file") MultipartFile imageUrl,
                                        @RequestPart("vo") ProductUpdateRequestVO productUpdateRequestVO,
                                        @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = modelMapper.map(productUpdateRequestVO, ProductDTO.class);
        productRequestDTO.setAdminId(id);
        ProductDTO productResponseDTO = productService.updateProduct(productRequestDTO, imageUrl);

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
     * req: localhost:8080/api/v1/product?cursor=3&size=3
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "cursorId": 2,
     *         "contents": [
     *             {
     *                 "id": 1,
     *                 "title": "Tent",
     *                 "content": "A two-person tent for camping.",
     *                 "imageUrl": "tent_image_url",
     *                 "category": {},
     *                 "startAt": "2024-09-30T15:00:00.000+00:00",
     *                 "endAt": "2024-10-31T15:00:00.000+00:00",
     *                 "rental": false,
     *                 "createdAt": "2024-10-08T03:00:00.000+00:00",
     *                 "updatedAt": "2024-10-08T03:00:00.000+00:00",
     *                 "adminId": 1,
     *                 "memberId": null
     *             },
     *             {
     *                 "id": 2,
     *                 "title": "Winter Jacket",
     *                 "content": "A warm winter jacket.",
     *                 "imageUrl": "jacket_image_url",
     *                 "category": {},
     *                 "startAt": "2024-09-30T15:00:00.000+00:00",
     *                 "endAt": "2024-10-31T15:00:00.000+00:00",
     *                 "rental": false,
     *                 "createdAt": "2024-10-08T03:00:00.000+00:00",
     *                 "updatedAt": "2024-10-08T03:00:00.000+00:00",
     *                 "adminId": 1,
     *                 "memberId": null
     *             }
     *         ],
     *         "hasNext": false
     *     },
     *     "error": null
     * }
     */
    // 나중에 삭제예정
//    @GetMapping("")
//    public ApiResponse<?> readAllProductList(@RequestParam(value = "cursor", required = false) Long cursorId,
//                                             @RequestParam(value ="size", defaultValue = "4") Integer size) {
//
//        CursorDTO cursorDTO = new CursorDTO();
//        cursorDTO.setCursorId(cursorId);
//        cursorDTO.setSize(size);
//
//        CursorDTO responsCursorDTO = productService.readAllProductList(cursorDTO);
//
//        ProductReadAllResponseVO productReadAllResponseVO = new ProductReadAllResponseVO();
//        productReadAllResponseVO.setCursorId(responsCursorDTO.getCursorId());
//        productReadAllResponseVO.setContents(responsCursorDTO.getComment());
//        productReadAllResponseVO.setHasNext(responsCursorDTO.isHasNext());
//
//        return ApiResponse.ok(productReadAllResponseVO);
//    }

    /**
     * 내용: 공유물품 상세 조회
     * req: localhost:8080/api/v1/product/6
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 6,
     *         "title": "스탠리 텀블러2",
     *         "content": "스탠리 상태 좋습니다2.",
     *         "imageUrl": "tumbler_image_url",
     *         "category": "KITCHENWARES",
     *         "status": "RENTAL",
     *         "startAt": "2024-10-08T00:00:00.000+00:00",
     *         "endAt": "2024-10-10T00:00:00.000+00:00",
     *         "rental": false,
     *         "createdAt": "2024-10-14T11:28:42.420+00:00",
     *         "updatedAt": "2024-10-14T11:28:42.420+00:00",
     *         "adminId": 4,
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
     * req: localhost:8080/api/v1/product/category/KITCHENWARES?cursor=&size=3
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "category": "KITCHENWARES",
     *         "cursorId": 3,
     *         "contents": [
     *             {
     *                 "id": 5,
     *                 "title": "스탠리 텀블러1",
     *                 "content": "스탠리 상태 좋습니다1.",
     *                 "imageUrl": "tumbler_image_url",
     *                 "category": "KITCHENWARES",
     *                 "status": "RENTAL",
     *                 "startAt": "2024-10-07T15:00:00.000+00:00",
     *                 "endAt": "2024-10-09T15:00:00.000+00:00",
     *                 "rental": false,
     *                 "createdAt": "2024-10-12T10:33:51.000+00:00",
     *                 "updatedAt": "2024-10-12T10:34:00.000+00:00",
     *                 "adminId": 4,
     *                 "memberId": null
     *             },
     *             {
     *                 "id": 3,
     *                 "title": "Cooking Pot",
     *                 "content": "A durable cooking pot for camping.",
     *                 "imageUrl": "pot_image_url",
     *                 "category": "KITCHENWARES",
     *                 "status": "RENTAL",
     *                 "startAt": "2024-09-30T15:00:00.000+00:00",
     *                 "endAt": "2024-10-31T15:00:00.000+00:00",
     *                 "rental": false,
     *                 "createdAt": "2024-10-08T03:00:00.000+00:00",
     *                 "updatedAt": "2024-10-08T03:00:00.000+00:00",
     *                 "adminId": 1,
     *                 "memberId": null
     *             }
 z    *         ],
     *         "hasNext": false
     *     },
     *     "error": null
     * }
     */
    @GetMapping("/category/{category}")
    public ApiResponse<?> readProductByCategory(@PathVariable ProductCategory category,
                                                @RequestParam(value = "cursor", required = false) Long cursorId,
                                                @RequestParam(value ="size", defaultValue = "4") Integer size) {

        CursorDTO cursorDTO = new CursorDTO();
        cursorDTO.setCursorId(cursorId);
        cursorDTO.setSize(size);
        cursorDTO.setCategory(category);

        CursorDTO responseCursorDTO = productService.readProductByCategory(cursorDTO);

        ProductReadAllResponseVO productReadAllResponseVO = new ProductReadAllResponseVO();
        productReadAllResponseVO.setCursorId(responseCursorDTO.getCursorId());
        productReadAllResponseVO.setContents(responseCursorDTO.getComment());
        productReadAllResponseVO.setHasNext(responseCursorDTO.isHasNext());
        productReadAllResponseVO.setCategory(responseCursorDTO.getCategory());

        return ApiResponse.ok(productReadAllResponseVO);
    }

    /**
     * 내용: 공유물품 대여신청
     * req: localhost:8080/api/v1/product/share/1?memberId=2
     * {
     *     "startAt": "2024-10-08T03:00:00.000+00:00",
     *     "endAt": "2024-10-09T03:00:00.000+00:00"
     * }
     * res:
     * {
     *     "success": true,
     *     "result": {
     *         "id": 12,
     *         "memberId": 5,
     *         "rental": true,
     *         "startAt": "2024-10-08T03:00:00.000+00:00",
     *         "endAt": "2024-10-09T03:00:00.000+00:00"
     *     },
     *     "error": null
     * }
     */
    @PutMapping("/share/{productId}")
    public ApiResponse<?> updateRentalProduct(@PathVariable Long productId,
                                              @RequestBody ProductRentalRequestVO productRentalRequestVO,
                                              @RequestAttribute("id") Long id) {

        ProductDTO productRequestDTO = new ProductDTO();
        productRequestDTO.setId(productId);
        productRequestDTO.setMemberId(id);
        productRequestDTO.setStartAt(productRentalRequestVO.getStartAt());
        productRequestDTO.setEndAt(productRentalRequestVO.getEndAt());
        ProductDTO productResponseDTO = productService.updateRentalProduct(productRequestDTO);

        alarmService.sendRentalAlarm(productResponseDTO);

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
