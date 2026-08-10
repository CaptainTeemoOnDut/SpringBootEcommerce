package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.component.ProductSpecification;
import com.longvo.demo_identity_service.dto.request.*;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.entity.*;
import com.longvo.demo_identity_service.entity.interfaces.CategoryAttributeProjection;
import com.longvo.demo_identity_service.enums.AttributeType;
import com.longvo.demo_identity_service.enums.ProductMediaStatus;
import com.longvo.demo_identity_service.enums.ProductMediaType;
import com.longvo.demo_identity_service.enums.ProductStatus;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.ProductMapper;
import com.longvo.demo_identity_service.repository.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {

    @Autowired
    ProductRepository productRepository;
    ShopRepository shopRepository;
    ProductMapper productMapper;
    ProductCategoryRepository productCategoryRepository;
    CategoryAttributeRepository categoryAttributeRepo;
    ProductAttributeValueRepository pavRepo;
    ProductVariationRepository variationRepo;
    VariantRepository variantRepo;
    VariationOptionRepository optionRepo;
    VariationMediaOptionRepository optionMediaRepo;
    VariantAttributeRepository variantAttributeRepo;
    ProductVariationQueryService productVariationQueryService;


    /*public Page<ProductResponse> getProducts(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toProductResponse);
    }*/



    @Transactional
    public ProductResponse createProduct(ProductCreationRequest request) {

        log.info("request = {}", request);

        Product product = productMapper.toProduct(request);

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED));
        product.setCategory(category);

        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXISTED));

        product.setShop(shop);

        product.setStatus(ProductStatus.DRAFT);



        mapMedia(product, request);

        List<CategoryAttributeProjection> projections =
                categoryAttributeRepo.findAllAttributesByCategoryTree(category.getId());

        Map<String, Attribute> attributeMap = projections.stream()
                .map(p -> {
                    Attribute a = new Attribute();
                    a.setId(p.getAttributeId());
                    a.setCode(p.getCode());
                    a.setName(p.getName());
                    a.setDataType(AttributeType.from(p.getDataType()));
                    a.setUnit(p.getUnit());
                    return a;
                })
                .collect(Collectors.toMap(
                        a -> a.getCode().toLowerCase(),
                        a -> a
                ));

        validateRequiredAttributesFromProjection(projections, request);

        List<ProductAttributeValue> values = new ArrayList<>();

        for (AttributeValueRequest req : request.getCategoryDetails()) {
            Attribute attribute = attributeMap.get(req.getCode().toLowerCase());
            if (attribute == null) {
                throw new AppException(ErrorCode.INVALID_ATTRIBUTE);
            }

            ProductAttributeValue pav = new ProductAttributeValue();
            pav.setProduct(product);
            pav.setAttribute(attribute);

            switch (attribute.getDataType()) {
                case STRING -> pav.setValueString(req.getValue());
                case NUMBER -> pav.setValueNumber(Double.parseDouble(req.getValue()));
                case BOOLEAN -> pav.setValueBoolean(Boolean.parseBoolean(req.getValue()));
            }

            values.add(pav);
        }

        log.info("product unitsInStock = {}", product.getUnitsInStock());


        productRepository.save(product);

        pavRepo.saveAll(values);

        // ======================
        // 2. Tạo Variations + Options + Media
        // ======================
        Map<String, ProductVariation> variationMap = new HashMap<>();
        Map<String, ProductVariationOption> optionMap = new HashMap<>();
        // key = color:Black

        for (ProductVariationCreationRequest vReq : request.getVariations()) {

            ProductVariation variation = new ProductVariation();
            variation.setCode(vReq.getCode());
            variation.setName(vReq.getName());
            variation.setProduct(product);
            variation.setPosition(0); //tam thoi
            variationRepo.save(variation);

            variationMap.put(vReq.getCode(), variation);

            for (ProductVariationOptionCreationRequest oReq : vReq.getOptions()) {

                ProductVariationOption option = new ProductVariationOption();
                option.setValue(oReq.getValue());
                option.setVariation(variation);
                option.setPosition(0); //tam thoi
                optionRepo.save(option);

                optionMap.put(vReq.getCode() + ":" + oReq.getValue(), option);

                // media theo option (ảnh màu)
                if (oReq.getImages() != null) {
                    for (ProductVariationOptionMediaCreationRequest mReq : oReq.getImages()) {
                        ProductVariationOptionMedia media =
                                ProductVariationOptionMedia.builder()
                                        .publicId(mReq.getPublicId())
                                        .publicUrl(mReq.getPublicUrl())
                                        .type(ProductMediaType.IMAGE)
                                        .productVariationOption(option)
                                        .position(0) // tam thoi
                                        .status(ProductMediaStatus.ACTIVE) // tam thoi
                                        .build();
                        optionMediaRepo.save(media);
                    }
                }
            }
        }

        // ======================
        // 3. Tạo Variants
        // ======================
        Set<String> uniqueCheck = new HashSet<>();

        for (ProductVariantCreationRequest vReq : request.getVariants()) {

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setPrice(vReq.getPrice());
            variant.setStock(vReq.getStock());
            variant.setSku(vReq.getSku());

            List<VariantAttribute> attributes = new ArrayList<>();
            StringBuilder uniqueKey = new StringBuilder();

            /*for (VariantAttributeCreationRequest attrReq : vReq.getAttributes()) {

                String mapKey = attrReq.getVariationCode() + ":" + attrReq.getOptionValue();
                ProductVariationOption option = optionMap.get(mapKey);

                if (option == null) {
                    throw new AppException(ErrorCode.INVALID_VARIANT_ATTRIBUTE);
                }

                VariantAttribute va = new VariantAttribute();
                va.setVariant(variant);
                //va.setVariationCode(attrReq.getVariationCode());
                ProductVariation variation = va.getVariation();
                if (variation == null) {
                    // Nếu null, bạn phải khởi tạo nó hoặc gán từ một nguồn khác
                    variation = new ProductVariation();
                    va.setVariation(variation);
                }
                variation.setCode(attrReq.getVariationCode());
                //va.getVariation().setCode(attrReq.getVariationCode());
                //va.setOptionValue(attrReq.getOptionValue());
                ProductVariationOption options = va.getOption();
                if (options == null) {
                    // Nếu null, bạn phải khởi tạo nó hoặc gán từ một nguồn khác
                    options = new ProductVariationOption();
                    va.setOption(options);
                }
                options.setValue(attrReq.getOptionValue());
                //va.getOption().setValue(attrReq.getOptionValue());

                attributes.add(va);
                uniqueKey.append(mapKey).append("|");
            }*/

            for (VariantAttributeCreationRequest attrReq : vReq.getAttributes()) {

                String mapKey = attrReq.getVariationCode() + ":" + attrReq.getOptionValue();
                ProductVariationOption option = optionMap.get(mapKey); // Đã lấy được Option từ Map

                if (option == null) {
                    throw new AppException(ErrorCode.INVALID_VARIANT_ATTRIBUTE);
                }

                // Lấy Variation tương ứng từ variationMap đã tạo ở Bước 2
                ProductVariation variation = variationMap.get(attrReq.getVariationCode());
                if (variation == null) {
                    throw new AppException(ErrorCode.INVALID_VARIATION);
                }

                VariantAttribute va = new VariantAttribute();
                va.setVariant(variant);

                // GÁN ĐỐI TƯỢNG ĐÃ CÓ (Đã có ID từ DB), KHÔNG ĐƯỢC NEW Ở ĐÂY
                va.setVariation(variation);
                va.setOption(option);

                attributes.add(va);
                uniqueKey.append(mapKey).append("|");
            }

            // check duplicate variant
            if (!uniqueCheck.add(uniqueKey.toString())) {
                throw new AppException(ErrorCode.DUPLICATE_VARIANT);
            }

            //variant.setValues(attributes);
            variant.setAttributes(attributes);
            variantRepo.save(variant);
            variantAttributeRepo.saveAll(attributes);
        }

        System.out.print("PRODUCT CREATED" + product);

        return productMapper.toProductResponse(product);
    }

    private void validateRequiredAttributesFromProjection(
            List<CategoryAttributeProjection> projections,
            ProductCreationRequest request
    ) {
        Set<String> providedCodes = request.getCategoryDetails().stream()
                .map(AttributeValueRequest::getCode)
                .collect(Collectors.toSet());

        for (CategoryAttributeProjection p : projections) {
            if (Boolean.TRUE.equals(p.getRequired())
                    && !providedCodes.contains(p.getCode())) {
                throw new AppException(ErrorCode.ATTRIBUTE_REQUIRED);
            }
        }
    }



    //@Transactional
    //@PreAuthorize("hasRole('ADMIN')")
    /*public ProductResponse createProduct(ProductCreationRequest request) {

        System.out.println(request);

        Product product = productMapper.toProduct(request);

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED));
        product.setCategory(category);

        mapMedia(product, request);



        List<CategoryAttributeProjection> categoryAttributeProjections =
                categoryAttributeRepo.findAllAttributesByCategoryTree(category.getId());

        List<Attribute> Attributes = categoryAttributeProjections.stream()
                .map(CategoryAttributeProjection::getAttributeId)

        Map<String, Attribute> attributeMap = categoryAttributes.stream()
                .map(CategoryAttribute::getAttribute)
                .collect(Collectors.toMap(Attribute::getCode, a -> a));

        validateRequiredAttributes(categoryAttributes, request);

        List<ProductAttributeValue> values = new ArrayList<>();

        log.debug("ATTRIBUTE MAP KEYS: {}", attributeMap.keySet());

        for (AttributeValueRequest req : request.getDetails()) {
            log.debug("REQ CODE: {}", req.getCode());
            // 1. Tìm attribute theo code
            Attribute attribute = attributeMap.get(req.getCode());
            log.debug("ATTRIBUTE: {}", attribute);
            if (attribute == null) {
                log.debug("ATTRIBUTE NULL IS: {}", attribute);
                throw new AppException(ErrorCode.INVALID_ATTRIBUTE);
            }

            // 2. Tạo entity lưu value
            ProductAttributeValue pav = new ProductAttributeValue();
            pav.setProduct(product);
            pav.setAttribute(attribute);

            // 3. Map value theo datatype
            switch (attribute.getDataType()) {
                case STRING -> pav.setValueString(req.getValue());

                case NUMBER -> {
                    try {
                        pav.setValueNumber(Double.parseDouble(req.getValue()));
                    } catch (NumberFormatException e) {
                        throw new AppException(ErrorCode.INVALID_ATTRIBUTE_TYPE);
                    }
                }

                case BOOLEAN -> pav.setValueBoolean(Boolean.parseBoolean(req.getValue()));
            }

            values.add(pav);
        }

        pavRepo.saveAll(values);

        productRepository.save(product);

        System.out.println("CREATE PRODUCT SUCCESSFULLY");

        return productMapper.toProductResponse(product);
    }*/

    /*@Transactional
    public ProductResponse createProduct(ProductCreationRequest request) {

        Product product = productMapper.toProduct(request);

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED));
        product.setCategory(category);

        List<CategoryAttribute> categoryAttributes =
                categoryAttributeRepo.findByCategoryId(category.getId());

        validateRequiredAttributes(categoryAttributes, request);

        Map<String, Attribute> attributeMap = categoryAttributes.stream()
                .map(CategoryAttribute::getAttribute)
                .collect(Collectors.toMap(Attribute::getCode, a -> a));

        for (AttributeValueRequest req : request.getDetails()) {
            Attribute attribute = attributeMap.get(req.getCode());
            if (attribute == null) {
                throw new AppException(ErrorCode.INVALID_ATTRIBUTE);
            }

            ProductAttributeValue pav = mapAttributeValue(req, attribute);
            product.addAttributeValue(pav);
        }

        mapMedia(product, request);

        productRepository.save(product);

        return productMapper.toProductResponse(product);
    }*/


    private void mapMedia(Product product, ProductCreationRequest request) {


        // Images
        if (request.getImages() != null && !request.getImages().isEmpty()) {

            List<ReviewMediaRequest> imgages = request.getImages();

            for (int i = 0; i < imgages.size(); i++) {

                ProductMedia media = ProductMedia.builder()
                        .type(ProductMediaType.IMAGE)
                        .publicId(imgages.get(i).getPublicId())
                        .publicUrl(imgages.get(i).getPublicUrl())
                        .status(ProductMediaStatus.ACTIVE)
                        .position(i)
                        .build();

                product.addMedia(media);

                // set thumbnail từ ảnh đầu tiên
                if(i == 0) {
                    product.setThumbnailUrl(imgages.get(0).getPublicUrl());
                }
            }
        }

        // Video ( only 1 video )
        if (request.getVideo() != null) {
            ProductMedia video = ProductMedia.builder()
                    .type(ProductMediaType.VIDEO)
                    .publicId(request.getVideo().getPublicId())
                    .publicUrl(request.getVideo().getPublicUrl())
                    .status(ProductMediaStatus.ACTIVE)
                    .position(0)
                    .build();

            product.addMedia(video);
        }
    }

    private void validateRequiredAttributes(
            List<CategoryAttribute> categoryAttributes,
            ProductCreationRequest request) {

        // 1. Check details rỗng
        if (request.getCategoryDetails() == null || request.getCategoryDetails().isEmpty()) {
            throw new AppException(ErrorCode.ATTRIBUTE_REQUIRED);
        }

        // 2. Lấy tất cả code user gửi lên
        Set<String> requestCodes = request.getCategoryDetails().stream()
                .map(AttributeValueRequest::getCode)
                .collect(Collectors.toSet());

        // 3. Duyệt các attribute của category
        for (CategoryAttribute ca : categoryAttributes) {

            if (!ca.isRequired()) continue;

            String requiredCode = ca.getAttribute().getCode();

            // 4. Nếu thiếu attribute bắt buộc
            if (!requestCodes.contains(requiredCode)) {
                throw new AppException(ErrorCode.ATTRIBUTE_REQUIRED);
            }
        }
    }



    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct (Long id) {
        productRepository.deleteById(id);
    }

    /*@PreAuthorize("hasRole('ADMIN')")
    public Page<ProductResponse> getPendingApprovalProduct (ProductStatus status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable ).map(productMapper::toProductResponse);
    }*/

    public ProductResponse pushProduct (Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        product.setStatus(ProductStatus.valueOf(String.valueOf(ProductStatus.PENDING_APPROVAL)));

        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    public ProductResponse approveProduct (Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        product.setStatus(ProductStatus.valueOf(String.valueOf(ProductStatus.APPROVED)));
        product.setApprovedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    public ProductResponse rejectProduct (Long id, ProductRejectRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        product.setStatus(ProductStatus.valueOf(String.valueOf(ProductStatus.REJECTED)));
        product.setRejectReason(request.getReason());
        product.setRejectNote(request.getNote());
        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct (Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productMapper.updateProduct(product, request);
        ProductCategory productCategory = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED));
        product.setCategory(productCategory);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    public Page<ProductCardResponse> search(ProductSearchRequest request, Pageable pageable) {
        return productRepository.searchProducts(request, pageable);
    }

    public Page<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toProductResponse);
    }

    public Page<ProductResponse> getProductsByShopId(String shopId, Pageable pageable) {
        return productRepository.findByShopId(shopId, pageable).map(productMapper::toProductResponse);
    }

    public Page<ProductCardResponse> filterProductCards(
            ProductFilterRequest request,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = ProductSpecification.filter(request);

        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(this::mapToDTO);
    }

    private ProductCardResponse mapToDTO(Product p) {
        return new ProductCardResponse(
                p.getId(),
                p.getName(),
                p.getThumbnailUrl(),
                p.getUnitPrice(),
                p.getSoldCount(),
                p.getShop().getIsMall()
        );
    }

    public Page<ProductCardResponse> findProductCards(Long categoryId, Pageable pageable) {
        System.out.println("CATEGORY ID: " + categoryId);
        if (!productCategoryRepository.existsById(categoryId)) {
            throw new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED);
        }

        System.out.println("CATEGORY HAHA: " + productRepository.findProductCards(categoryId,ProductStatus.APPROVED, pageable));

        return productRepository.findProductCards(categoryId,ProductStatus.APPROVED, pageable);
    }

    public Page<ProductTableRowResponse> findProductsForSellerTable(Long shopId, Pageable pageable) {
        // Tam comment dong nay nhe
        /*if (!shopRepository.existsById(shopId)) {
            throw new AppException(ErrorCode.SHOP_NOT_EXISTED);
        }*/
        return productRepository.findProductsForSellerTable(shopId, pageable);
    }

    public Page<ProductTableRowResponse> findPendingApprovalProductsForAdminTable(Pageable pageable) {


        return productRepository.findProductsForAdminTable(ProductStatus.PENDING_APPROVAL,pageable);
    }

    public Page<ProductTableRowResponse> findPendingApprovalProductsForShopTable(Long shopId, Pageable pageable) {


        return productRepository.findPendingApprovalProductsForShopTable(shopId, ProductStatus.PENDING_APPROVAL,pageable);
    }

    public Page<ProductTableRowResponse> findHiddenProductsForAdminTable(Pageable pageable) {


        return productRepository.findProductsForAdminTable(ProductStatus.HIDDEN,pageable);
    }

    public Page<ProductTableRowResponse> findHiddenProductsForShopTable(Long shopId, Pageable pageable) {


        return productRepository.findHiddenProductsForShopTable(shopId, ProductStatus.HIDDEN,pageable);
    }

    /*public Page<ProductResponse> getProductsByShopIdAndStatus(String shopId,ProductStatus status, Pageable pageable) {
        return productRepository.findByShopIdAndStatus(shopId,status, pageable).map(productMapper::toProductResponse);
    }*/

    public Page<ProductResponse> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContaining(name, pageable).map(productMapper::toProductResponse);
    }

    public ProductResponse getProductById(Long productId) {
        return productMapper.toProductResponse(productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED)));
    }


    //@PreAuthorize("hasRole('ADMIN')")
    public ProductDetailResponse getProductDetail(Long productId) {
        //System.out.println("PRODUCT ID HA HA HA" + productId);
        ProductDetailResponse detail = productRepository.getProductDetails(productId);

        if (detail == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        List<ProductVariationResponse> variation = productVariationQueryService.getProductVariations(productId);

        // 2. Lấy tất cả các biến thể của sản phẩm đó kèm theo các Option của chúng
        // Nên dùng @EntityGraph hoặc Join Fetch trong Repository để tối ưu 1 lần query
        //List<ProductVariant> variants = variantRepo.findAllByProductId(productId);

        detail.setVariations(variation);

        // 3. Xây dựng variantLookup map
        detail.setVariantLookup(getVariantLookup(productId));

        ProductCategory productCategory = productCategoryRepository.findById(detail.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED));

        List<CategoryBreadcrumbResponse> breadcrumbResponses = buildBreadcrumb(productCategory);

        detail.setCategories(breadcrumbResponses);

        return detail;
    }

    @Transactional(readOnly = true)
    public Map<String, VariantInforDTO> getVariantLookup(Long productId) {
        // Lấy tất cả các biến thể của sản phẩm, kèm theo attributes và options
        List<ProductVariant> variants = variantRepo.findAllByProductId(productId);

        return variants.stream().collect(Collectors.toMap(
                variant -> variant.getAttributes().stream()
                        // Lấy ID của Option (ví dụ: ID của "Black", ID của "XL")
                        .map(attr -> attr.getOption().getId().toString())
                        // Sắp xếp ID để đảm bảo tính nhất quán (ví dụ: Luôn là "10-20")
                        .sorted()
                        .collect(Collectors.joining("-")),
                variant -> new VariantInforDTO(variant.getId(),variant.getPrice(), variant.getStock()),
                (existing, replacement) -> existing // Xử lý nếu trùng lặp key
        ));
    }

    public List<CategoryBreadcrumbResponse> buildBreadcrumb(ProductCategory leaf) {
        List<CategoryBreadcrumbResponse> result = new ArrayList<>();
        ProductCategory current = leaf;

        while (current != null) {
            result.add(
                    new CategoryBreadcrumbResponse(
                            current.getId(),
                            current.getName()
                    )
            );
            current = current.getParent();
        }

        Collections.reverse(result);
        return result;
    }

}
