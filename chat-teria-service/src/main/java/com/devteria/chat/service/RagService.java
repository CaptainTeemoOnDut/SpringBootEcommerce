package com.devteria.chat.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final VectorStore vectorStore;

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // Insert documents có ID cố định để tránh duplicate khi re-index
    public void saveDocuments() {
        List<Document> docs = List.of(
                new Document(
                        "doc-policy-return",
                        """
						CHÍNH SÁCH TRẢ HÀNG & HOÀN TIỀN
						- Phạm vi: Người mua, Người bán, Đơn vị vận chuyển.
						- Điều kiện: Hàng hỏng, thiếu, nhái, hư hỏng do vận chuyển, sai mô tả, hoặc Trả hàng COM (đối với hội viên VIP).
						- Thời hạn gửi yêu cầu: Trong vòng 15 ngày kể từ khi giao thành công (thực phẩm tươi sống/đông lạnh trong 24 giờ).
						- Quy trình: Tạo yêu cầu trên app, liên kết tài khoản ngân hàng/ví điện tử, đóng gói nguyên vẹn và BẮT BUỘC quay video/chụp ảnh làm bằng chứng.
						- Thời gian xử lý: Người bán phản hồi trong 02 ngày. Hệ thống đưa ra quyết định xử lý cuối cùng.
						""",
                        Map.of("category", "policy", "title", "Chính sách trả hàng")),
                new Document(
                        "doc-faq-account-lock",
                        """
						HƯỚNG DẪN MỞ KHÓA TÀI KHOẢN
						- Nguyên nhân khóa: Hệ thống phát hiện vi phạm chính sách (tạo nhiều tài khoản, lạm dụng mã giảm giá...).
						- Cách xử lý: Gửi Form Yêu Cầu Khôi Phục Tài Khoản bao gồm: Tên đăng nhập/SĐT, Ảnh CCCD/CMND chính chủ, Ảnh màn hình thông báo lỗi.
						- Thời gian phản hồi: 24h - 48h làm việc qua Email.
						""",
                        Map.of("category", "account", "title", "Mở khóa tài khoản")),
                new Document(
                        "doc-faq-email-change",
                        """
						LỖI KHÔNG THỂ THÊM HOẶC THAY ĐỔI EMAIL
						- Nguyên nhân: Email đã được liên kết với tài khoản khác, chưa nhập mã OTP, hoặc sai định dạng email.
						- Cách khắc phục:
						1. Vào Tôi > Thiết lập tài khoản > Hồ sơ > Email để cập nhật.
						2. Kiểm tra hòm thư Spam/Rác để lấy mã OTP.
						3. Nếu không truy cập được Email cũ, chọn 'Thử phương thức khác' để xác thực qua SMS/Khuôn mặt.
						""",
                        Map.of("category", "account", "title", "Lỗi đổi email")));

        vectorStore.add(docs);
    }

    // Search có cấu hình Top-K và Ngưỡng độ tương đồng (Similarity Threshold)
    public String search(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(2) // Lấy top 2 kết quả
                .similarityThreshold(0.65) // Ngưỡng độ tương đồng
                .build();
        List<Document> results = vectorStore.similaritySearch(request);

        if (results.isEmpty()) {
            return ""; // Không tìm thấy context phù hợp
        }

        return results.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
    }
}
