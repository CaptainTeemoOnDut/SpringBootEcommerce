package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductVariantService {

    VariantRepository variantRepository;
    //VariantAttributeRepository attributeRepository;

    public VariantInfoResponse revalidate(Long variantId) {
        return variantRepository.findVariantInforById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_EXISTS));
    }

    /*@Transactional(readOnly = true)
    public ProductVariantLookupResponse getVariantLookup(Long productId) {

        List<ProductVariantFlatRow> rows =
                variantRepository.findVariantLookup(productId);

        Map<Long, List<ProductVariantFlatRow>> grouped =
                rows.stream()
                        .collect(Collectors.groupingBy(ProductVariantFlatRow::getVariantId));

        List<ProductVariantResponse> variants = new ArrayList<>();

        for (Map.Entry<Long, List<ProductVariantFlatRow>> entry : grouped.entrySet()) {

            Long variantId = entry.getKey();
            List<ProductVariantFlatRow> variantRows = entry.getValue();

            Map<String, Long> attributes = new HashMap<>();

            for (ProductVariantFlatRow r : variantRows) {
                attributes.put(r.getVariationCode(), r.getOptionId());
            }

            ProductVariantFlatRow first = variantRows.get(0);

            variants.add(new ProductVariantResponse(
                    variantId,
                    attributes,
                    first.getPrice(),
                    first.getStock(),
                    first.getSku()
            ));
        }

        return new ProductVariantLookupResponse(variants);
    }*/

}
