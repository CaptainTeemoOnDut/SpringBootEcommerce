package com.longvo.demo_identity_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.longvo.demo_identity_service.enums.ReviewMediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ReviewMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "public_id")
    String publicId;

    @Column(name = "public_url", nullable = false)
    String publicUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    ReviewMediaType mediaType;


    @Column(name = "created_at")
    @CreationTimestamp
    Date createdAt;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnore
    Review review;

}
