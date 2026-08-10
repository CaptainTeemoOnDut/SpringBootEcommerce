package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.component.VNPayUtil;
import com.longvo.demo_identity_service.configuration.VNPayConfig;
import com.longvo.demo_identity_service.dto.request.PaymentRequestDTO;
import com.longvo.demo_identity_service.entity.OrderGroup;
import com.longvo.demo_identity_service.entity.PaymentTransaction;
import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import com.longvo.demo_identity_service.enums.PaymentStatus;
import com.longvo.demo_identity_service.repository.OrderGroupRepository;
import com.longvo.demo_identity_service.repository.OrderRepository;
import com.longvo.demo_identity_service.repository.PaymentTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VNPayService {

    OrderRepository orderRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final VNPayConfig vnPayConfig;
    PaymentTransactionRepository paymentTransactionRepository;

    @NonFinal
    @Value("${vnpay.tmnCode}")
    protected String vnpTmnCode;

    @NonFinal
    @Value("${vnpay.payUrl}")
    protected String vnpPayUrl;

    @NonFinal
    @Value("${vnpay.hashSecret}")
    protected String vnpHashSecret;

    @NonFinal
    @Value("${vnpay.returnUrl}")
    protected String vnpReturnUrl;

    public VNPayService(OrderRepository orderRepository, OrderGroupRepository orderGroupRepository, VNPayConfig vnPayConfig, PaymentTransactionRepository paymentTransactionRepository) {
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.vnPayConfig = vnPayConfig;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    public String createPaymentUrl(PaymentRequestDTO request, HttpServletRequest httpRequest) {
        log.info("PAYMENT REQUEST: " + request);
        OrderGroup orderGroup = orderGroupRepository.findById(request.getOrderGroupId())
                .orElseThrow(() -> new RuntimeException("Order group not found"));

        if (orderGroup.getStatus() != OrderGroupStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Order group is not PENDING_PAYMENT");
        }

        BigDecimal amount = orderGroup.getTotalAmount();
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        String vnpTxnRef = System.currentTimeMillis() + "_" + orderGroup.getId();

        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderGroup(orderGroup)
                .paymentMethod(PaymentMethods.VNPAY)
                .status(PaymentStatus.PENDING_PAYMENT)
                .vnpTxnRef(vnpTxnRef)
                .totalAmount(amount)
                .build();

        paymentTransactionRepository.save(paymentTransaction);

        String vnp_IpAddr = httpRequest.getRemoteAddr();

        Map<String, String> vnpParams = new HashMap<>();

        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", vnp_IpAddr);
        //String vnp_TxnRef = VNPayUtil.getRandomNumber(8);
        /*log.info("Payment Request: {}", request);

        OrderGroup orderGroup = orderGroupRepository.findById(Long.valueOf(request.getOrderGroupId())).orElse(null);
        if (orderGroup == null) {
            throw new RuntimeException("Order group not found");
        }

        if (orderGroup.getStatus() != OrderGroupStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Order group is not PENDING_PAYMENT");
        }

        long vnpAmount = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderGroup(orderGroup)
                .paymentMethod(PaymentMethods.VNPAY)
                .status(PaymentStatus.PENDING_PAYMENT)
                .vnpTxnRef(vnp_TxnRef)
                .totalAmount(BigDecimal.valueOf(vnpAmount))
                .transactionStatus(PaymentStatus.PENDING_PAYMENT.toString())
                .build();
        paymentTransactionRepository.save(paymentTransaction);

        String vnp_TxnRef = paymentTransaction.getId().toString();

        String vnp_IpAddr = httpRequest.getRemoteAddr();

        Map<String, String> vnpParams = new HashMap<>();



        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");

        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
        vnpParams.put("vnp_OrderType", "other");

        vnpParams.put("vnp_Locale", "vn");

        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

        vnpParams.put("vnp_IpAddr", vnp_IpAddr);*/

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate = formatter.format(cld.getTime());

        vnpParams.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);

        String vnp_ExpireDate = formatter.format(cld.getTime());

        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {

            for (String fieldName : fieldNames) {

                String fieldValue = vnpParams.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {

                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    query.append('&');
                    hashData.append('&');
                }
            }

            hashData.deleteCharAt(hashData.length() - 1);

            query.deleteCharAt(query.length() - 1);

            String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

            query.append("&vnp_SecureHash=");
            query.append(secureHash);


            return vnPayConfig.getPayUrl() + "?" + query;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

    /*public String createVnpayUrl(long amount, String txnRef, HttpServletRequest request) throws UnsupportedEncodingException {
        String vnp_Version = VNPayConfig.vnp_Version;
        String vnp_Command = VNPayConfig.vnp_Command;
        String vnp_OrderInfo = txnRef; // Để đơn giản như bản Nodejs bạn test thành công
        String orderType = "other";
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY yêu cầu nhân 100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // Tạo ngày tạo và ngày hết hạn (giống Nodejs)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút (thay vì 1 ngày cho an toàn)
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 1. Sắp xếp tham số
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnp_Params.get(fieldName);

            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build Hash Data (Giá trị thô - KHÔNG ENCODE)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);

                // Build Query (CÓ ENCODE)
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // CHỈ THÊM DẤU & NẾU CHƯA PHẢI THAM SỐ CUỐI CÙNG
                if (i < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
// Tính toán SecureHash dựa trên chuỗi hashData vừa build
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;

        // 1. Sắp xếp tham số theo alphabet
        /*List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build Hash Data (Dùng giá trị THÔ)
                hashData.append(fieldName).append('=').append(fieldValue);

                // Build Query (CÓ ENCODE)
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 2. Tính toán SecureHash
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;*/
    //}


    /*public String createVnpayUrl(HttpServletRequest req) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = 1000000; // 10,000 VND * 100
        String vnp_TxnRef = VNPAYConfig.getRandomNumber(8);
        String vnp_IpAddr = VNPAYConfig.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPAYConfig.hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpayUrl + "?" + queryUrl;

        return ResponseEntity.ok(paymentUrl);
    }*/

    /*public String createVnpayUrl(OrderGroup order,String clientIp ) {
        clientIp = "127.0.0.1";
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put(
                "vnp_Amount",
                order.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .toBigInteger()
                        .toString()
        );
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getId().toString());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        String query = buildQuery(params);
        String secureHash = hmacSHA512(vnpHashSecret, query);

        return vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }*/
/*
    public String createVnpayUr(OrderGroup orderGroup, String clientIp) {
        clientIp = "127.0.0.1";
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", orderGroup.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .toBigInteger()
                .toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderGroup.getId().toString());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderGroup.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", clientIp);

        params.put("vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {

                // 1. Build Hash Data: GIỮ NGUYÊN giá trị gốc, không encode
                if (!hashData.isEmpty()) {
                    hashData.append("&");
                }
                hashData.append(fieldName).append("=").append(fieldValue);

                // 2. Build Query String: Encode và thay thế + bằng %20
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append("=");
                // Quan trọng nhất ở dòng dưới đây
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8).replace("+", "%20"));
            }
        }

        /*for (String fieldName : fieldNames) {

            String fieldValue = params.get(fieldName);

            if (!hashData.isEmpty()) {
                hashData.append("&");
                query.append("&");
            }

            String valueForHash = fieldValue;

            if ("vnp_ReturnUrl".equals(fieldName)) {
                valueForHash = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)
                        .replace("+", "%20");
            }

            hashData.append(fieldName)
                    .append("=")
                    .append(valueForHash);

            String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            // query encode value
            query.append(fieldName)
                    .append("=")
                    .append(encodedValue);
        }*/

       /* String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());

        log.info("Hash data = {}", hashData);
        log.info("Query = {}", query);
        log.info("SecureHash = {}", secureHash);

        return vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }*/

    /*public void verifyCallBack(Map<String, String> params ){
        String orderGroupId = params.get("vnp_TxnRef");

        OrderGroup orderGroup = orderGroupRepository.findById(Long.valueOf(orderGroupId))
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        Long vnpAmount = Long.valueOf(params.get("vnp_Amount")) / 100;

        BigDecimal amount = new BigDecimal(vnpAmount);

        if(verifyAmount(orderGroup, amount)) {
            throw new RuntimeException("Payment amount mismatch");
        }

        if(isPaid(orderGroup)) {
            throw new RuntimeException("Order is paid");
        }

        if(verifySignature(params)){
            throw new RuntimeException("Signature mismatch");
        }
    }*/
/*
    public boolean verifySignature(Map<String, String> params) {

        String receivedHash = params.remove("vnp_SecureHash");

        String query = buildQuery(params);
        String calculatedHash = hmacSHA512(vnpHashSecret, query);

        return receivedHash.equalsIgnoreCase(calculatedHash);
    }

    public boolean verifyAmount(OrderGroup orderGroup, BigDecimal amount) {

        if (!orderGroup.getTotalAmount().equals(amount)) {
            return false;
        }
        return true;
    }

    //Idempotent callback
    public boolean isPaid(OrderGroup orderGroup) {

        return orderGroup.getStatus() == OrderGroupStatus.PAID;
    }

    private String buildQuery(Map<String, String> params) {

        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry ->
                        URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                                + "=" +
                                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
                )
                .collect(Collectors.joining("&"));
    }

    private String hmacSHA512(String key, String data) {

        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA512", e);
        }
    }



}*/
