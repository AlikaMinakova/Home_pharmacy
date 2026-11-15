package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.OrderStatus;
import ru.home_pharmacy.home_pharmacy.entity.Review;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.repository.CustomerOrderRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.ReviewRepository;
import ru.home_pharmacy.home_pharmacy.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final CustomerOrderRepository orderRepository;

    /** Добавляет или обновляет отзыв покупателя о лекарстве (один отзыв на лекарство). */
    public void addOrUpdate(String username, Long medicationId, Integer rating, String comment) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

        // отзыв можно оставить только если покупатель выкупил это лекарство
        boolean purchased = orderRepository.existsByCustomerPhoneAndMedicationIdAndStatus(
                username, medicationId, OrderStatus.PURCHASED);
        if (!purchased) {
            throw new RuntimeException("Отзыв можно оставить только после покупки этого лекарства.");
        }

        Review review = reviewRepository.findByUserIdAndMedicationId(user.getId(), medicationId)
                .orElseGet(() -> Review.builder()
                        .user(user)
                        .medication(medicationRepository.findById(medicationId)
                                .orElseThrow(() -> new RuntimeException("Лекарство не найдено: " + medicationId)))
                        .createdAt(LocalDateTime.now())
                        .build());
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviews(Long medicationId) {
        return reviewRepository.findAllByMedicationIdOrderByCreatedAtDesc(medicationId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long medicationId) {
        return reviewRepository.findAverageRating(medicationId);
    }

    @Transactional(readOnly = true)
    public Long getCount(Long medicationId) {
        return reviewRepository.countByMedicationId(medicationId);
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(String username, Long medicationId) {
        if (username == null) {
            return false;
        }
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && reviewRepository.existsByUserIdAndMedicationId(user.getId(), medicationId);
    }
}