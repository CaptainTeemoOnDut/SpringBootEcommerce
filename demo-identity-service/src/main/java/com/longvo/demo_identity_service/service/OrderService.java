package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.configuration.SecurityUtils;
import com.longvo.demo_identity_service.dto.DraftOrderCreatedEvent;
import com.longvo.demo_identity_service.dto.DraftOrderRedis;
import com.longvo.demo_identity_service.dto.OrderGroupRedis;
import com.longvo.demo_identity_service.dto.request.*;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.entity.*;
import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.enums.OrderStatus;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.OrderMapper;
import com.longvo.demo_identity_service.mapper.ProductVariantMapper;
import com.longvo.demo_identity_service.mapper.UserMapper;
import com.longvo.demo_identity_service.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    OrderGroupRepository orderGroupRepository;
    AddressRepository addressRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    CartRedisService cartRedisService;
    StringRedisTemplate redisTemplate;
    RedisCacheService redisCacheService;
    ProductVariantRepository productVariantRepository;
    VNPayService vnPayService;
    private final OrderItemRepository orderItemRepository;
    private ApplicationEventPublisher eventPublisher;


    private String getCartKey(Long userId) {
        return "cart:" + userId;
    }

    public OrderGroupPreviewResponse previewOrder(CartRequest request) {
        //TAM COMMENT userId nhe
        //Long userId = SecurityUtils.getCurrentUserId();
        Long userId = 1L;

        // 1. Lấy toàn bộ SKU IDs mà user đã chọn
        List<Long> selectedSkuIds = request.getSkuIds();



        // 3. Query thông tin Product/Shop từ DB dựa trên list SkuId
        // Lưu ý: Query vào bảng ProductVariant chứ không phải bảng cart_items
        List<ProductVariant> variants = productVariantRepository.findAllWithProductAndShopByVariantIds(selectedSkuIds);

        // 2. Group theo Shop và tính toán logic (InMemory)
        List<ShopOrderPreview> shopPreviews = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal subtotalBeforeDiscount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        Map<Shop, List<ProductVariant>> groupedByShop = variants.stream()
                .collect(Collectors.groupingBy(va -> va.getProduct().getShop()));

        for (Map.Entry<Shop, List<ProductVariant>> entry : groupedByShop.entrySet()) {
            ShopOrderPreview shopPreview = calculateShopPreview(entry.getKey(), entry.getValue(), selectedSkuIds);
            shopPreviews.add(shopPreview);
            grandTotal = grandTotal.add(shopPreview.getShopTotal());
        }

        // 3. Lấy địa chỉ (Handle null nếu user chưa có địa chỉ)
        Address address = addressRepository.findDefaultByUserId(userId);

        return OrderGroupPreviewResponse.builder()
                .shops(shopPreviews)
                .fullAddress(address.getFullAddress())
                .receiverName("Vo Long")
                .phoneNumber("0348743025")
                .subtotalBeforeDiscount(subtotalBeforeDiscount)
                .discountAmount(discountAmount)
                .grandTotal(grandTotal)
                .paymentMethod(PaymentMethods.COD)
                .build();

    }

    public ShopOrderPreview calculateShopPreview(Shop shop, List<ProductVariant> variants, List<Long> selectedSkuIds) {

        //TAM COMMENT userId nhe
        //Long userId = SecurityUtils.getCurrentUserId();
        Long userId = 1L;
        String cartKey = getCartKey(userId);

        // Sử dụng hMGet để lấy nhiều field cùng lúc trong Redis cho nhanh
        List<Object> quantitiesFromRedis = redisTemplate.opsForHash().multiGet(cartKey,
                selectedSkuIds.stream().map(String::valueOf).collect(Collectors.toList()));

        // 2. CHUYỂN LIST THÀNH MAP để tra cứu theo ID (Key: SkuId, Value: Quantity)
        // Map này giúp bạn tìm quantity của 1 ID bất kỳ mà không quan tâm index
        Map<Long, Integer> qtyMap = new HashMap<>();
        for (int i = 0; i < selectedSkuIds.size(); i++) {
            Object val = quantitiesFromRedis.get(i);
            if (val != null) {
                qtyMap.put(selectedSkuIds.get(i), Integer.parseInt(val.toString()));
            }
        }

        BigDecimal shopSubtotal = BigDecimal.ZERO;
        List<OrderItemResponse> itemResponses = new ArrayList<>();


        for (ProductVariant variant : variants) {

            // 3. TRA CỨU TỪ MAP (An toàn, không bị lỗi Index)
            Integer currentQtyInCart = qtyMap.getOrDefault(variant.getId(), 0);
            if (currentQtyInCart <= 0) continue; // Bỏ qua nếu món này không thực sự tồn tại trong giỏ Redis
            if (variant.getStock() < currentQtyInCart) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            BigDecimal unitPrice = variant.getPrice();
            BigDecimal lineTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(currentQtyInCart)
                    );

            shopSubtotal = shopSubtotal.add(lineTotal);

            itemResponses.add(
                    OrderItemResponse.builder()
                            .variantId(variant.getId())
                            .variantName(variant.getProduct().getName())
                            .imageUrl(variant.getImageUrl())
                            .unitPrice(unitPrice)
                            .quantity(currentQtyInCart)
                            .lineTotal(lineTotal)
                            .build()
            );
        }

        // Giả sử shipping cố định
        BigDecimal shippingFee = new BigDecimal("30000");

        BigDecimal shopTotal = shopSubtotal.add(shippingFee);

        return ShopOrderPreview.builder()
                .shopId(shop.getId())
                .shopName(shop.getName())
                .items(itemResponses)
                .shopSubtotal(shopSubtotal)
                .shippingFee(shippingFee)
                .shopTotal(shopTotal)
                .build();


    }

    /*public OrderGroupPreviewResponse previewOrder(CartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. Fetch dữ liệu với JOIN FETCH để tránh N+1 Query

        List<CartItem> cartItems = cartItemRepository.findAllWithProductAndShopByIds(request.getCartItemIds());

        // Validate ownership & status (chỉ kiểm tra, không update)
        validateCartItems(cartItems, userId);

        // 2. Group theo Shop và tính toán logic (InMemory)
        List<ShopOrderPreview> shopPreviews = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        Map<Shop, List<CartItem>> groupedByShop = cartItems.stream()
                .collect(Collectors.groupingBy(ci -> ci.getVariant().getProduct().getShop()));

        for (Map.Entry<Shop, List<CartItem>> entry : groupedByShop.entrySet()) {
            ShopOrderPreview shopPreview = calculateShopPreview(entry.getKey(), entry.getValue());
            shopPreviews.add(shopPreview);
            grandTotal = grandTotal.add(shopPreview.getShopTotal());
        }

        // 3. Lấy địa chỉ (Handle null nếu user chưa có địa chỉ)
        Address address = addressRepository.findDefaultByUserId(userId);

        return OrderGroupPreviewResponse.builder()
                .shops(shopPreviews)
                .addressId(address != null ? address.getId() : null)
                .grandTotal(grandTotal)
                .build();
    }*/

    /*public ShopOrderPreview calculateShopPreview(Shop shop, List<CartItem> cartItems) {

            BigDecimal shopSubtotal = BigDecimal.ZERO;
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (CartItem cartItem : cartItems) {

                ProductVariant variant = cartItem.getVariant();

                BigDecimal unitPrice = variant.getPrice();
                BigDecimal lineTotal =
                        unitPrice.multiply(
                                BigDecimal.valueOf(cartItem.getQuantity())
                        );

                shopSubtotal = shopSubtotal.add(lineTotal);

                itemResponses.add(
                        OrderItemResponse.builder()
                                .variantId(variant.getId())
                                .productName(variant.getProduct().getName())
                                .productImage(cartItem.getImageUrl())
                                .unitPrice(unitPrice)
                                .quantity(cartItem.getQuantity())
                                .lineTotal(lineTotal)
                                .build()
                );
            }

            // Giả sử shipping cố định
            BigDecimal shippingFee = new BigDecimal("30000");

            BigDecimal shopTotal = shopSubtotal.add(shippingFee);

                    return ShopOrderPreview.builder()
                            .shopId(shop.getId())
                            .shopName(shop.getName())
                            .items(itemResponses)
                            .shopSubtotal(shopSubtotal)
                            .shippingFee(shippingFee)
                            .shopTotal(shopTotal)
                            .build();


    }*/

    public void validateCartItems (List<CartItem> cartItems, Long userId) {
        // 🔥 Validate ownership
        for (CartItem item : cartItems) {
            if (!item.getCart().getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            if (item.isCheckedOut()) {
                throw new AppException(ErrorCode.ALREADY_CHECKED_OUT);
            }
        }
    }

    @Transactional
    public OrderGroupResponse createDraftOrder(PlaceOrderRequest request, HttpServletRequest httpRequestServlet) throws UnsupportedEncodingException {
        //Long userId = SecurityUtils.getCurrentUserId();
        System.out.println("PLACE ORDER REQUEST:" + request);
        Long userId = 1L;
        String cartKey = getCartKey(userId);
        LocalDateTime now = LocalDateTime.now();

        // Collect variants từ DB
        List<ProductVariant> variants = productVariantRepository.findAllWithProductAndShopByVariantIds(request.getSelectedSkuIds());
        if (variants.size() != request.getSelectedSkuIds().size()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // Lấy quantities từ Redis cart (giữ nguyên)
        List<String> skuKeys = request.getSelectedSkuIds().stream().map(String::valueOf).toList();
        List<Object> quantitiesFromRedis =
                redisTemplate.opsForHash().multiGet(cartKey, new ArrayList<>(skuKeys));
        Map<Long, Integer> qtyMap = new HashMap<>();
        for (int i = 0; i < request.getSelectedSkuIds().size(); i++) {
            Object val = quantitiesFromRedis.get(i);
            if (val != null) {
                qtyMap.put(request.getSelectedSkuIds().get(i), Integer.parseInt(val.toString()));
            }
        }

        // Group theo shop (giữ nguyên)
        Map<Shop, List<ProductVariant>> groupedByShop = variants.stream()
                .collect(Collectors.groupingBy(va -> va.getProduct().getShop()));

        OrderGroup orderGroup = new OrderGroup();
        orderGroup.setUser(userRepository.getReferenceById(userId));
        orderGroup.setStatus(OrderGroupStatus.PENDING_PAYMENT);
        orderGroup.setCreatedAt(now);
        orderGroup.setExpiredAt(now.plusMinutes(15));

        BigDecimal grandTotal = BigDecimal.ZERO;
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        List<ProductVariant> productVariantsToUpdate = new ArrayList<>(); // Để saveAll sau
        //List<String> reservedInventoryKeys = new ArrayList<>(); // Để rollback nếu fail
        Map<String, Integer> reservedInventory = new HashMap<>();
        List<Order> orders = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        List<DraftOrderRedis> draftOrderRedisList = new ArrayList<>();

        // Reserve Redis atomic first
        for (Long variantId : request.getSelectedSkuIds()) {
            Integer qty = qtyMap.getOrDefault(variantId, 0);
            if (qty <= 0) continue;

            String invKey = "inventory:" + variantId;

            // On-demand warm-up: Nếu Redis chưa có key → init từ DB
            Boolean exists = redisTemplate.hasKey(invKey);
            if (Boolean.FALSE.equals(exists)) {
                // Tìm variant tương ứng (từ list variants đã query)
                ProductVariant variant = variants.stream()
                        .filter(v -> v.getId().equals(variantId))
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

                // Init Redis với stock hiện tại (lưu ý: dùng current stock, không phải reserved)
                redisTemplate.opsForValue().set(invKey, variant.getStock().toString());
                // Optional: set TTL dài (ví dụ 24h) để tránh key tồn tại vĩnh viễn nếu variant không hot
                redisTemplate.expire(invKey, 24, TimeUnit.HOURS);
            }

            Long newReserved = redisTemplate.opsForValue().decrement(invKey, qty.longValue()); // DECR by qty (atomic)

            if (newReserved == null || newReserved < 0) {
                // Rollback tất cả đã DECR trước đó
                rollbackRedisReserves(reservedInventory);
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            //reservedInventoryKeys.add(invKey); // Track để rollback nếu sync DB fail
            reservedInventory.put(invKey, qty);
        }

        try {
            // Group và tạo orders
            long expireTime = System.currentTimeMillis() + 15 * 60 * 1000;

            for (Map.Entry<Shop, List<ProductVariant>> entry : groupedByShop.entrySet()) {
                Shop shop = entry.getKey();
                List<ProductVariant> items = entry.getValue();

                Order order = new Order();
                order.setOrderGroup(orderGroup);
                order.setShop(shop);
                order.setShopNameSnapshot(shop.getName());
                order.setStatus(OrderStatus.PENDING_PAYMENT);
                order.setCreatedAt(now);
                order.setExpiredAt(orderGroup.getExpiredAt());
                order.setShippingAddress(address);

                BigDecimal shopSubtotal = BigDecimal.ZERO;
                List<DraftOrderItemRedis> draftItems = new ArrayList<>();
                DraftOrderRedis draftOrderRedis = new DraftOrderRedis();
                draftOrderRedis.setShopId(shop.getId());

                for (ProductVariant variant : items) {
                    Integer currentQty = qtyMap.getOrDefault(variant.getId(), 0);
                    if (currentQty <= 0) continue;

                    // Sync reservedStock to MySQL (JPA optimistic sẽ check @Version)
                    int oldReserved = Optional.ofNullable(variant.getReservedStock()).orElse(0);
                    variant.setReservedStock(oldReserved + currentQty);
                    productVariantsToUpdate.add(variant); // Sẽ saveAll sau

                    BigDecimal unitPrice = variant.getPrice();

                    System.out.println("VARIANT PRICE" + variant.getPrice());
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(currentQty));

                    System.out.println("VARIANT TOTAL" + lineTotal);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setVariant(variant);
                    orderItem.setQuantity(currentQty);
                    orderItem.setUnitPrice(unitPrice);
                    orderItem.setSkuSnapshot(variant.getSku());
                    orderItems.add(orderItem);
                    order.add(orderItem);

                    shopSubtotal = shopSubtotal.add(lineTotal);
                    System.out.println("SHOP SUB TOTAL" + shopSubtotal);


                    DraftOrderItemRedis draftItem = DraftOrderItemRedis.builder()
                            .id(variant.getId())
                            .quantity(currentQty)
                            .build();
                    draftItems.add(draftItem);
                }

                if (draftItems.isEmpty()) continue;

                draftOrderRedis.setItems(draftItems);

                // Shipping fee (có thể customize sau)
                BigDecimal shippingFee = new BigDecimal("30000");
                BigDecimal shopTotal = shopSubtotal.add(shippingFee);
                System.out.println("SHOP TOTAL: " + shopTotal);
                order.setTotalAmount(shopTotal);
                order.setShippingFee(shippingFee);

                orders.add(order);
                orderGroup.getOrders().add(order);
                grandTotal = grandTotal.add(shopTotal);



                draftOrderRedisList.add(draftOrderRedis);
            }

            orderGroup.setTotalAmount(grandTotal);
            System.out.println("TOTAL AMOUNT: " + grandTotal);

            // Save DB entities (transactional)
            orderGroupRepository.save(orderGroup);
            orderRepository.saveAll(orders);
            orderItemRepository.saveAll(orderItems);

            // Sync reservedStock (optimistic locking tự động)
            productVariantRepository.saveAll(productVariantsToUpdate); // Nếu conflict → throw OptimisticLockingFailureException

            // Set orderId cho draft Redis
            for (int i = 0; i < orders.size(); i++) {
                DraftOrderRedis draftOrderRedis = draftOrderRedisList.get(i);
                //Sorted Set
                redisTemplate.opsForZSet()
                        .add("order_expiration", orders.get(i).getId().toString(), expireTime);
                for(DraftOrderItemRedis draftOrderItemRedis : draftOrderRedis.getItems()) {
                    redisTemplate.opsForZSet()
                            .add("order_item_expiration", draftOrderItemRedis.getId().toString() + ":" + draftOrderItemRedis.getQuantity(), expireTime);
                }
            }

            // Save Draft to Redis with TTL (15 phút = 900s)

            /*String tempOrderKey = "draft_order_group:" + userId + ":" + orderGroup.getId();
            OrderGroupRedis orderGroupRedis = new OrderGroupRedis();
            orderGroupRedis.setOrderGroupId(orderGroup.getId());
            orderGroupRedis.setUserId(userId);
            orderGroupRedis.setTotalAmount(grandTotal);
            orderGroupRedis.setExpiredAt(now.plusMinutes(15));
            orderGroupRedis.setOrders(draftOrderRedisList);*/



            // Lưu vào Redis (ví dụ dùng redisTemplate.opsForValue().set với TTL)
            //redisCacheService.<OrderGroupRedis>setAsync(tempOrderKey, orderGroupRedis, Duration.ofMinutes(15));

            // Publish event (giữ nguyên)
            //DraftOrderCreatedEvent event = new DraftOrderCreatedEvent(orderGroupRedis, tempOrderKey);
            //eventPublisher.publishEvent(event);

            // Tạo payment URL
            PaymentRequestDTO requestDTO = new PaymentRequestDTO();
            requestDTO.setOrderGroupId(orderGroup.getId());
            requestDTO.setOrderInfo("Thanh toan don hang");
            requestDTO.setAmount(orderGroup.getTotalAmount());
            return OrderGroupResponse.builder()
                    .paymentUrl(vnPayService.createPaymentUrl(requestDTO, httpRequestServlet))
                    .orderGroupId(orderGroup.getId())
                    .addressId(address.getId())
                    .fullAddress(address.getFullAddress())
                    .receiverName(request.getReceiverName())
                    .phoneNumber(request.getPhoneNumber())
                    .grandTotal(grandTotal)
                    .paymentMethod(request.getPaymentMethod())
                    .expiredAt(orderGroup.getExpiredAt())
                    .build();

        } catch (ObjectOptimisticLockingFailureException ex) {
            // Rollback Redis reserves nếu sync DB fail
            rollbackRedisReserves(reservedInventory);
            throw new AppException(ErrorCode.OUT_OF_STOCK); // hoặc message phù hợp
        } catch (Exception ex) {
            // Rollback Redis nếu có lỗi khác
            rollbackRedisReserves(reservedInventory);
            throw ex;
        }
    }

    // Helper method rollback, rollback quantity trong redis
    public void rollbackRedisReserves(Map<String, Integer> reservedInventory) {

        for (Map.Entry<String, Integer> entry : reservedInventory.entrySet()) {

            redisTemplate.opsForValue()
                    .increment(entry.getKey(), entry.getValue());
        }
    }
    /*public void rollbackRedisReserves(List<String> keys, Map<Long, Integer> qtyMap) {
        for (String key : keys) {
            // Parse variantId từ key "inventory:123" → 123
            Long variantId = Long.parseLong(key.split(":")[1]);
            Integer qty = qtyMap.getOrDefault(variantId, 0);
            if (qty > 0) {
                redisTemplate.opsForValue().increment(key, qty.longValue()); // INCR lại
            }
        }
    }*/

    @Transactional
    public void cancelExpiredOrder(List<Long> orderIds) {

        List<Order> orders = orderRepository.findAllByIds(orderIds);

        if (orders.isEmpty()) return;

        for (Order order : orders) {

            order.setStatus(OrderStatus.CANCELLED);

            releaseInventory(order);
        }
    }

    private void releaseInventory(Order order) {
        for (OrderItem item : order.getOrderItems()) {

            Long variantId = item.getVariant().getId();
            int qty = item.getQuantity();

            String invKey = "inventory:" + variantId;

            redisTemplate.opsForValue().increment(invKey, qty);

            ProductVariant variant = item.getVariant();
            variant.setReservedStock(variant.getReservedStock() - qty);
        }
    }

    /*@Transactional
    public OrderGroupResponse createDraftOrder(PlaceOrderRequest request, HttpServletRequest httpServletRequest) {

        Long userId = SecurityUtils.getCurrentUserId();
        //Long userId = 1L;
        String cartKey = getCartKey(userId);

        LocalDateTime now = LocalDateTime.now();

        // collect variants -> saveAll()
        List<ProductVariant> productVariants = new ArrayList<>();

        OrderGroupRedis orderGroupRedis = new OrderGroupRedis();
        List<DraftOrderRedis> draftOrderRedisList = new ArrayList<>();

        List<Order> orders = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Query thông tin Product/Shop từ DB dựa trên list SkuId
        // Lưu ý: Query vào bảng ProductVariant chứ không phải bảng cart_items
        List<ProductVariant> variants = productVariantRepository.findAllWithProductAndShopByVariantIds(request.getSelectedSkuIds());

        if (variants.size() != request.getSelectedSkuIds().size()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        List<String> skuKeys = request.getSelectedSkuIds().stream().map(String::valueOf).toList();

        // Sử dụng hMGet để lấy nhiều field cùng lúc trong Redis cho nhanh
        List<Object> quantitiesFromRedis = redisTemplate.opsForHash().multiGet(cartKey,
               skuKeys.stream().map(String::valueOf).collect(Collectors.toList()));

        // 2. CHUYỂN LIST THÀNH MAP để tra cứu theo ID (Key: SkuId, Value: Quantity)
        // Map này giúp bạn tìm quantity của 1 ID bất kỳ mà không quan tâm index
        Map<Long, Integer> qtyMap = new HashMap<>();
        for (int i = 0; i < request.getSelectedSkuIds().size(); i++) {
            Object val = quantitiesFromRedis.get(i);
            if (val != null) {
                qtyMap.put(request.getSelectedSkuIds().get(i), Integer.parseInt(val.toString()));
            }
        }
        // ==========================
        // 1️⃣ Group theo shop
        // ==========================
        Map<Shop, List<ProductVariant>> groupedByShop =
                variants.stream()
                        .collect(Collectors.groupingBy(
                                va -> va.getProduct().getShop()
                        ));

        // ==========================
        // 2️⃣ Tạo OrderGroup
        // ==========================
        OrderGroup orderGroup = new OrderGroup();
        orderGroup.setUser(userRepository.getReferenceById(userId));
        orderGroup.setStatus(OrderGroupStatus.PENDING_PAYMENT);
        orderGroup.setCreatedAt(now);
        orderGroup.setExpiredAt(now.plusMinutes(15));

        BigDecimal grandTotal = BigDecimal.ZERO;

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        // ==========================
        // 3️⃣ Tạo Order cho từng shop
        // ==========================
        for (Map.Entry<Shop, List<ProductVariant>> entry : groupedByShop.entrySet()) {

            Shop shop = entry.getKey();
            List<ProductVariant> items = entry.getValue();

            Order order = new Order();
            order.setOrderGroup(orderGroup);
            order.setShop(shop);
            order.setShopNameSnapshot(shop.getName());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setCreatedAt(now);
            order.setExpiredAt(orderGroup.getExpiredAt());
            order.setShippingAddress(address);

            BigDecimal shopSubtotal = BigDecimal.ZERO;

            List<DraftOrderItemRedis> draftItems = new ArrayList<>();
            DraftOrderRedis draftOrderRedis = new DraftOrderRedis();
            draftOrderRedis.setShopId(shop.getId());

            for (ProductVariant variant : items) {

                // 3. TRA CỨU TỪ MAP (An toàn, không bị lỗi Index)
                Integer currentQtyInCart = qtyMap.getOrDefault(variant.getId(), 0);
                if (currentQtyInCart <= 0) continue; // Bỏ qua nếu món này không thực sự tồn tại trong giỏ Redis
                int reserved =
                        Optional.ofNullable(variant.getReservedStock()).orElse(0);

                int available = variant.getStock() - reserved;

                if (available < currentQtyInCart) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }

                // 🔥 Reserve stock
                variant.setReservedStock(reserved + currentQtyInCart);
                variant.setVersion(variant.getVersion());
                productVariants.add(variant);

                BigDecimal unitPrice = variant.getPrice();
                BigDecimal lineTotal =
                        unitPrice.multiply(
                                BigDecimal.valueOf(currentQtyInCart)
                        );

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setVariant(variant);
                orderItem.setQuantity(currentQtyInCart);
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSkuSnapshot(variant.getSku());
                orderItems.add(orderItem);

                order.add(orderItem);

                shopSubtotal = shopSubtotal.add(lineTotal);

                DraftOrderItemRedis draftOrderItemRedis = DraftOrderItemRedis.builder()
                        .id(variant.getId())
                        .quantity(currentQtyInCart)
                        .build();
                draftItems.add(draftOrderItemRedis);

            }

            if (draftItems.isEmpty()) {
                continue;
            }

            draftOrderRedis.setItems(draftItems);

            // 🔥 Giả sử shipping cố định
            BigDecimal shippingFee = new BigDecimal("30000");

            BigDecimal shopTotal = shopSubtotal.add(shippingFee);

            order.setTotalAmount(shopTotal);
            order.setShippingFee(shippingFee);
            orders.add(order);
            orderGroup.getOrders().add(order);
            grandTotal = grandTotal.add(shopTotal);

            draftOrderRedisList.add(draftOrderRedis);
        }

        orderGroup.setTotalAmount(grandTotal);

        orderGroupRepository.save(orderGroup);
        orderRepository.saveAll(orders);
        for(int i = 0; i < orders.size(); i++) {
            draftOrderRedisList.get(i).setOrderId(orders.get(i).getId());
        }
        orderItemRepository.saveAll(orderItems);

        String tempOrderKey = "draft_order_group:" + userId + ":" + orderGroup.getId();
        orderGroupRedis.setOrderGroupId(orderGroup.getId());
        orderGroupRedis.setUserId(userId);
        orderGroupRedis.setTotalAmount(grandTotal);
        orderGroupRedis.setExpiredAt(now.plusMinutes(15));
        orderGroupRedis.setOrders(draftOrderRedisList);
        try {
            productVariantRepository.saveAll(productVariants);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        DraftOrderCreatedEvent event =
                new DraftOrderCreatedEvent(orderGroupRedis, tempOrderKey);

        eventPublisher.publishEvent(event);

        return OrderGroupResponse.builder()
                .paymentUrl(vnPayService.createVnpayUrl(orderGroup, httpServletRequest))
                .orderGroupId(orderGroup.getId())
                .addressId(address.getId())
                .fullAddress(address.getFullAddress())
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                .grandTotal(grandTotal)
                .paymentMethod(request.getPaymentMethod())
                .expiredAt(orderGroup.getExpiredAt())
                .build();
    }*/

    @Transactional
    public OrderGroupResponse placeOrder(PlaceOrderRequest request) {
        //TAM COMMENT userId nhe
        //Long userId = SecurityUtils.getCurrentUserId();
        Long userId = 1L;
        String cartKey = getCartKey(userId);

        // 3. Query thông tin Product/Shop từ DB dựa trên list SkuId
        // Lưu ý: Query vào bảng ProductVariant chứ không phải bảng cart_items
        List<ProductVariant> variants = productVariantRepository.findAllWithProductAndShopByVariantIds(request.getSelectedSkuIds());

        // Sử dụng hMGet để lấy nhiều field cùng lúc trong Redis cho nhanh
        List<Object> quantitiesFromRedis = redisTemplate.opsForHash().multiGet(cartKey,
                request.getSelectedSkuIds().stream().map(String::valueOf).collect(Collectors.toList()));

        // 2. CHUYỂN LIST THÀNH MAP để tra cứu theo ID (Key: SkuId, Value: Quantity)
        // Map này giúp bạn tìm quantity của 1 ID bất kỳ mà không quan tâm index
        Map<Long, Integer> qtyMap = new HashMap<>();
        for (int i = 0; i < request.getSelectedSkuIds().size(); i++) {
            Object val = quantitiesFromRedis.get(i);
            if (val != null) {
                qtyMap.put(request.getSelectedSkuIds().get(i), Integer.parseInt(val.toString()));
            }
        }
        // ==========================
        // 1️⃣ Group theo shop
        // ==========================
        Map<Shop, List<ProductVariant>> groupedByShop =
                variants.stream()
                        .collect(Collectors.groupingBy(
                                va -> va.getProduct().getShop()
                        ));

        // ==========================
        // 2️⃣ Tạo OrderGroup
        // ==========================
        OrderGroup orderGroup = new OrderGroup();
        orderGroup.setUser(userRepository.getReferenceById(userId));
        orderGroup.setStatus(OrderGroupStatus.PENDING_PAYMENT);
        orderGroup.setCreatedAt(LocalDateTime.now());
        orderGroup.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        BigDecimal grandTotal = BigDecimal.ZERO;

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        // ==========================
        // 3️⃣ Tạo Order cho từng shop
        // ==========================
        for (Map.Entry<Shop, List<ProductVariant>> entry : groupedByShop.entrySet()) {

            Shop shop = entry.getKey();
            List<ProductVariant> items = entry.getValue();

            Order order = new Order();
            order.setOrderGroup(orderGroup);
            order.setShop(shop);
            order.setShopNameSnapshot(shop.getName());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setCreatedAt(LocalDateTime.now());
            order.setExpiredAt(orderGroup.getExpiredAt());
            order.setShippingAddress(address);

            BigDecimal shopSubtotal = BigDecimal.ZERO;

            for (ProductVariant variant : items) {

                // 3. TRA CỨU TỪ MAP (An toàn, không bị lỗi Index)
                Integer currentQtyInCart = qtyMap.getOrDefault(variant.getId(), 0);
                if (currentQtyInCart <= 0) continue; // Bỏ qua nếu món này không thực sự tồn tại trong giỏ Redis
                int reserved =
                        Optional.ofNullable(variant.getReservedStock()).orElse(0);

                int available = variant.getStock() - reserved;

                if (available < currentQtyInCart) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }

                // 🔥 Reserve stock
                variant.setReservedStock(reserved + currentQtyInCart);

                BigDecimal unitPrice = variant.getPrice();
                BigDecimal lineTotal =
                        unitPrice.multiply(
                                BigDecimal.valueOf(currentQtyInCart)
                        );

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setVariant(variant);
                orderItem.setQuantity(currentQtyInCart);
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSkuSnapshot(variant.getSku());

                order.add(orderItem);

                shopSubtotal = shopSubtotal.add(lineTotal);
            }


            // 🔥 Giả sử shipping cố định
            BigDecimal shippingFee = new BigDecimal("30000");

            BigDecimal shopTotal = shopSubtotal.add(shippingFee);

            order.setTotalAmount(shopTotal);
            order.setShippingFee(shippingFee);

            orderGroup.getOrders().add(order);

            grandTotal = grandTotal.add(shopTotal);


        }

        orderGroup.setTotalAmount(grandTotal);

        //TAM COMMENT DE TEST NHE
        // 4. Đánh dấu CartItem đã thanh toán (Xóa khỏi DB)
        //cartItemRepository.removeFromCart(request.getSelectedSkuIds());

        // Xoa item khoi redis
        for(ProductVariant variant : variants) {
            cartRedisService.removeFromCart(userId, String.valueOf(variant.getId()));
        }

        //TAM COMMENT DE TEST NHE
        //orderGroupRepository.save(orderGroup);

        return OrderGroupResponse.builder()
                .orderGroupId(orderGroup.getId())
                .addressId(address.getId())
                .fullAddress(address.getFullAddress())
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                //.subtotalBeforeDiscount(orderGroup.getTotalAmount())
                //.discountAmount()
                .grandTotal(grandTotal)
                .paymentMethod(request.getPaymentMethod())
                .expiredAt(orderGroup.getExpiredAt())
                .build();
    }

    /*@Transactional
    public OrderGroupResponse placeOrder(PlaceOrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CartItem> cartItems = cartItemRepository.findAllWithProductAndShopByIds(request.getCartItemIds());
        // ==========================
        // 1️⃣ Group theo shop
        // ==========================
        Map<Shop, List<CartItem>> groupedByShop =
                cartItems.stream()
                        .collect(Collectors.groupingBy(
                                ci -> ci.getVariant().getProduct().getShop()
                        ));

        // ==========================
        // 2️⃣ Tạo OrderGroup
        // ==========================
        OrderGroup orderGroup = new OrderGroup();
        orderGroup.setUser(userRepository.getReferenceById(userId));
        orderGroup.setStatus(OrderGroupStatus.PENDING_PAYMENT);
        orderGroup.setCreatedAt(LocalDateTime.now());
        orderGroup.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        BigDecimal grandTotal = BigDecimal.ZERO;

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        // ==========================
        // 3️⃣ Tạo Order cho từng shop
        // ==========================
        for (Map.Entry<Shop, List<CartItem>> entry : groupedByShop.entrySet()) {

            Shop shop = entry.getKey();
            List<CartItem> items = entry.getValue();

            Order order = new Order();
            order.setOrderGroup(orderGroup);
            order.setShop(shop);
            order.setShopNameSnapshot(shop.getName());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setCreatedAt(LocalDateTime.now());
            order.setExpiredAt(orderGroup.getExpiredAt());
            order.setShippingAddress(address);

            BigDecimal shopSubtotal = BigDecimal.ZERO;


            for (CartItem cartItem : items) {

                ProductVariant variant = cartItem.getVariant();

                int reserved =
                        Optional.ofNullable(variant.getReservedStock()).orElse(0);

                int available = variant.getStock() - reserved;

                if (available < cartItem.getQuantity()) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }

                // 🔥 Reserve stock
                variant.setReservedStock(reserved + cartItem.getQuantity());

                cartItem.setCheckedOut(true);

                BigDecimal unitPrice = variant.getPrice();
                BigDecimal lineTotal =
                        unitPrice.multiply(
                                BigDecimal.valueOf(cartItem.getQuantity())
                        );

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setVariant(variant);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSkuSnapshot(variant.getSku());

                order.add(orderItem);

                shopSubtotal = shopSubtotal.add(lineTotal);


            }

            // 🔥 Giả sử shipping cố định
            BigDecimal shippingFee = new BigDecimal("30000");

            BigDecimal shopTotal = shopSubtotal.add(shippingFee);

            order.setTotalAmount(shopTotal);
            order.setShippingFee(shippingFee);

            orderGroup.getOrders().add(order);

            grandTotal = grandTotal.add(shopTotal);


        }

        orderGroup.setTotalAmount(grandTotal);

        // 4. Đánh dấu CartItem đã thanh toán (Xóa khỏi giỏ)
        cartItemRepository.markAsCheckedOut(request.getCartItemIds());

        // Xoa item khoi redis
        for(CartItem cartItem : cartItems) {
            cartRedisService.removeFromCart(userId, String.valueOf(cartItem.getId()));
        }

        orderGroupRepository.save(orderGroup);

        return OrderGroupResponse.builder()
                .orderGroupId(orderGroup.getId())
                .addressId(address.getId())
                .fullAddress(address.getFullAddress())
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                //.subtotalBeforeDiscount(orderGroup.getTotalAmount())
                //.discountAmount()
                .grandTotal(grandTotal)
                .paymentMethod(request.getPaymentMethod())
                .expiredAt(orderGroup.getExpiredAt())
                .build();
    }*/

    /*@Transactional
    public OrderGroupPreviewResponse createDraftOrder(CartItemRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        List<CartItem> cartItems =
                cartItemRepository.findAllById(request.getCartItemIds());

        if (cartItems.isEmpty() ||
                cartItems.size() != request.getCartItemIds().size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 🔥 Validate ownership
        for (CartItem item : cartItems) {
            if (!item.getCart().getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            if (item.isCheckedOut()) {
                throw new AppException(ErrorCode.ALREADY_CHECKED_OUT);
            }
        }

        // ==========================
        // 1️⃣ Group theo shop
        // ==========================
        Map<Shop, List<CartItem>> groupedByShop =
                cartItems.stream()
                        .collect(Collectors.groupingBy(
                                ci -> ci.getVariant().getProduct().getShop()
                        ));

        // ==========================
        // 2️⃣ Tạo OrderGroup
        // ==========================
        OrderGroup orderGroup = new OrderGroup();
        orderGroup.setUser(userRepository.getReferenceById(userId));
        orderGroup.setStatus(OrderGroupStatus.PENDING_PAYMENT);
        orderGroup.setCreatedAt(LocalDateTime.now());
        orderGroup.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        BigDecimal grandTotal = BigDecimal.ZERO;

        List<ShopOrderPreview> shopPreviews = new ArrayList<>();

        // ==========================
        // 3️⃣ Tạo Order cho từng shop
        // ==========================
        for (Map.Entry<Shop, List<CartItem>> entry : groupedByShop.entrySet()) {

            Shop shop = entry.getKey();
            List<CartItem> items = entry.getValue();

            Order order = new Order();
            order.setOrderGroup(orderGroup);
            order.setShop(shop);
            order.setShopNameSnapshot(shop.getName());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setCreatedAt(LocalDateTime.now());
            order.setExpiredAt(orderGroup.getExpiredAt());

            BigDecimal shopSubtotal = BigDecimal.ZERO;
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (CartItem cartItem : items) {

                ProductVariant variant = cartItem.getVariant();

                int reserved =
                        Optional.ofNullable(variant.getReservedStock()).orElse(0);

                int available = variant.getStock() - reserved;

                if (available < cartItem.getQuantity()) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }

                // 🔥 Reserve stock
                variant.setReservedStock(reserved + cartItem.getQuantity());

                cartItem.setCheckedOut(true);

                BigDecimal unitPrice = variant.getPrice();
                BigDecimal lineTotal =
                        unitPrice.multiply(
                                BigDecimal.valueOf(cartItem.getQuantity())
                        );

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setVariant(variant);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSkuSnapshot(variant.getSku());

                order.add(orderItem);

                shopSubtotal = shopSubtotal.add(lineTotal);

                itemResponses.add(
                        OrderItemResponse.builder()
                                .variantId(variant.getId())
                                .productName(variant.getProduct().getName())
                                .productImage(cartItem.getImageUrl())
                                .unitPrice(unitPrice)
                                .quantity(cartItem.getQuantity())
                                .lineTotal(lineTotal)
                                .build()
                );
            }

            // 🔥 Giả sử shipping cố định
            BigDecimal shippingFee = new BigDecimal("30000");

            BigDecimal shopTotal = shopSubtotal.add(shippingFee);

            order.setTotalAmount(shopTotal);
            order.setShippingFee(shippingFee);

            orderGroup.getOrders().add(order);

            grandTotal = grandTotal.add(shopTotal);

            shopPreviews.add(
                    ShopOrderPreview.builder()
                            .shopId(shop.getId())
                            .shopName(shop.getName())
                            .items(itemResponses)
                            .shopSubtotal(shopSubtotal)
                            .shippingFee(shippingFee)
                            .shopTotal(shopTotal)
                            .build()
            );
        }

        orderGroup.setTotalAmount(grandTotal);

        orderGroupRepository.save(orderGroup);

        // ==========================
        // 4️⃣ Lấy địa chỉ mặc định
        // ==========================
        Address address = addressRepository.findDefaultByUserId(userId);

        return OrderGroupPreviewResponse.builder()
                .orderGroupId(orderGroup.getId())
                .shops(shopPreviews)
                .addressId(address.getId())
                .fullAddress(address.getFullAddress())
                .receiverName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .discountAmount(BigDecimal.ZERO)
                .grandTotal(grandTotal)
                .expiredAt(orderGroup.getExpiredAt())
                .build();
    }*/

    /*@Transactional
    public OrderPreviewResponse createDraftOrder(CartItemRequest request) {

        List<CartItem> items = cartItemRepository.findAllById(request.getCartItemIds());

        if (items.isEmpty() || items.size() != request.getCartItemIds().size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        Long userId = SecurityUtils.getCurrentUserId();

        Order order = new Order();
        order.setStatus(OrderStatus.DRAFT);

        order.setCreatedAt(LocalDateTime.now());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : items) {

            if (!cartItem.getCart().getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            if (cartItem.isCheckedOut()) {
                throw new AppException(ErrorCode.ALREADY_CHECKED_OUT);
            }

            ProductVariant variant = cartItem.getVariant();

            int reserved = Optional.ofNullable(variant.getReservedStock()).orElse(0);
            int available = variant.getStock() - reserved;

            if (available < cartItem.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            variant.setReservedStock(reserved + cartItem.getQuantity());

            cartItem.setCheckedOut(true);

            BigDecimal unitPrice = variant.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);

            order.add(orderItem);

            totalPrice = totalPrice.add(
                    unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        order.setTotalPrice(totalPrice);

        return orderMapper.toOrderPreviewResponse(orderRepository.save(order));
    }*/

    /*@Transactional
    public OrderPreviewResponse createDraftOrder(CartItemRequest request) {

        //hiện tại trong Entity ProductVariant đang có @Version private Long version; (Optimistic locking)
        // Hibernate/JPA tự động xử lý version phía sau hậu trường

        //đã thêm validation @NotEmpty(message = "Cart items must not be empty") vào CartItemRequest để tránh user gửi { "cartItemIds": [] }

        // Query CartItem theo ID
        List<CartItem> items = cartItemRepository.findAllById(request.getCartItemIds());

        if (items.size() != request.getCartItemIds().size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // User
        Long userId = getMyInfo().getId();

        // Tạo OrderDraft
        Order order = new Order();
        order.setStatus(OrderStatus.DRAFT);
        order.setPaymentMethod(PaymentMethods.COD);
        order.setCreatedAt(LocalDateTime.now());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem itemReq : items) {

            // đánh dấu cartItem = CHECKED_OUT
            itemReq.setCheckedOut(true);

            // Validate CartItem thuộc user hiện tại
            if (!itemReq.getCart().getUser().getId().equals(userId)) {
                throw new RuntimeException("User id in cart item is not equal to user id");
            }

            // Validate Variant còn đủ stock
            int available = itemReq.getVariant().getStock() - itemReq.getVariant().getReservedStock();

            if (available < itemReq.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            // Reserved stock
            int currentReserved =
                    Optional.ofNullable(itemReq.getVariant().getReservedStock()).orElse(0);

            itemReq.getVariant().setReservedStock(currentReserved + itemReq.getQuantity());

            BigDecimal unitPrice = itemReq.getVariant().getPrice();
            BigDecimal itemTotal =
                    unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            // Tạo OrderItem từ từng CartItem
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(itemReq.getVariant()); // cascade type all
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice); // snapshot

            order.add(item);

            totalPrice = totalPrice.add(itemTotal);
        }

        order.setTotalPrice(totalPrice);

        // Save Order
        // Trả về OrderPreviewResponse
        /*
            Khi confirm payment:
            ✔ Trừ stock thật (stock -= reserved)
            ✔ reservedStock -= quantity
            ✔ Delete cartItem
            ✔ Order -> PAID
            Khi expire:
            ✔ reservedStock -= quantity
            ✔ Order -> CANCELLED
        */
        //return orderMapper.toOrderPreviewResponse(orderRepository.save(order));
    //}


    /*public Order createOrder(PurchaseRequest request) {

        Order order = new Order();
        order.setStatus(OrderStatus.DRAFT);
        order.setPaymentMethod(request.getPaymentMethods());
        order.setCreatedAt(LocalDateTime.now());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getOrderItems()) {

            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow();

            if (variant.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Out of stock");
            }

            variant.setReservedStock(itemReq.getQuantity());

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal itemTotal =
                    unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice);

            order.add(item);

            totalPrice = totalPrice.add(itemTotal);
        }

        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }*/

    /*@Transactional
    public OrderCreationResponse createOrder(PurchaseRequest request) {

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        BigDecimal totalPrice = BigDecimal.ZERO;

        // generate tracking number
        String orderTrackingNumber = generateOrderTrackingNumber();
        order.setOrderTrackingNumber(orderTrackingNumber);

        // populate order with billingAddress and ShippingAddress
        order.setBillingAddress(addressMapper.toAddress(request.getBillingAddress()));
        order.setShippingAddress(addressMapper.toAddress(request.getShippingAddress()));

        Set<OrderItem> items = new HashSet<>();

        for (OrderItemRequest itemReq : request.getOrderItems()) {

            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow();

            int available = variant.getStock() - variant.getReservedStock();

            if (available < itemReq.getQuantity()) {
                throw new RuntimeException("Out of stock");
            }

            variant.setReservedStock(
                    variant.getReservedStock() + itemReq.getQuantity()
            );

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            items.add(item);

            totalPrice = totalPrice.add(itemReq.getUnitPrice());
        }

        order.setOrderItems(items);
        //orderRepository.save(order);

        // populate user with order
        User user = new User();

        // check if this is an existing user
        String userEmail = request.getUserEmail();

        User userFromDB = userRepository.findByEmail(userEmail);
        if (userFromDB != null) {
            // we found them ... let's assign them accordingly
            user = userFromDB;
        }
        user.addOrder(order);

        // save to the database
        userRepository.save(user);

        // return a response
        return orderMapper.toOrderResponse(order);
    }*/

    private String generateOrderTrackingNumber() {
        // generate a random UUID number (UUID version-4)
        // For details see: https://en.wikipedia.org/wiki/Universally_unique_identifier
        //
        return UUID.randomUUID().toString();
    }

}
