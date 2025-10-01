package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Basic finders
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    Optional<User> findByEmailAndIsActiveTrue(String email);

    // Check existence
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    // Active users
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();
    Page<User> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<User> findByIsActiveTrueOrderByLastLoginDesc(Pageable pageable);

    // Role-based queries
    List<User> findByRoleAndIsActiveTrueOrderByCreatedAtDesc(User.Role role);
    Page<User> findByRoleAndIsActiveTrueOrderByCreatedAtDesc(User.Role role, Pageable pageable);
    long countByRoleAndIsActiveTrue(User.Role role);

    // Email verification
    Optional<User> findByEmailVerificationToken(String token);
    List<User> findByIsEmailVerifiedFalseAndIsActiveTrueOrderByCreatedAtDesc();

    // Password reset
    Optional<User> findByPasswordResetToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetExpires = null WHERE u.passwordResetExpires < :now")
    void clearExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    // Newsletter subscribers
    List<User> findByNewsletterSubscribedTrueAndIsActiveTrueOrderByCreatedAtDesc();
    Page<User> findByNewsletterSubscribedTrueAndIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    long countByNewsletterSubscribedTrueAndIsActiveTrue();

    // Search functionality
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY u.createdAt DESC")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    // Statistics queries
    long countByIsActiveTrue();
    long countByIsActiveFalse();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.createdAt >= :since")
    long countNewUsers(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.lastLogin >= :since")
    long countActiveUsers(@Param("since") LocalDateTime since);

    // Login tracking
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :loginTime, u.loginCount = u.loginCount + 1 WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    // Account management
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = :active WHERE u.id = :userId")
    void updateActiveStatus(@Param("userId") Long userId, @Param("active") boolean active);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isEmailVerified = true, u.emailVerificationToken = null WHERE u.emailVerificationToken = :token")
    void verifyEmail(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateUserRole(@Param("userId") Long userId, @Param("role") User.Role role);

    // Newsletter management
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.newsletterSubscribed = :subscribed WHERE u.id = :userId")
    void updateNewsletterSubscription(@Param("userId") Long userId, @Param("subscribed") boolean subscribed);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.newsletterSubscribed = :subscribed WHERE u.email = :email")
    void updateNewsletterSubscriptionByEmail(@Param("email") String email, @Param("subscribed") boolean subscribed);

    // User preferences
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.preferredCategories = :categories WHERE u.id = :userId")
    void updatePreferredCategories(@Param("userId") Long userId, @Param("categories") String categories);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.timezone = :timezone WHERE u.id = :userId")
    void updateTimezone(@Param("userId") Long userId, @Param("timezone") String timezone);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.language = :language WHERE u.id = :userId")
    void updateLanguage(@Param("userId") Long userId, @Param("language") String language);

    // Profile updates
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.firstName = :firstName, u.lastName = :lastName WHERE u.id = :userId")
    void updateUserName(@Param("userId") Long userId, @Param("firstName") String firstName, @Param("lastName") String lastName);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.profileImageUrl = :imageUrl WHERE u.id = :userId")
    void updateProfileImage(@Param("userId") Long userId, @Param("imageUrl") String imageUrl);

    // Analytics queries
    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u WHERE u.isActive = true AND u.createdAt >= :since " +
            "GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationsByDate(@Param("since") LocalDateTime since);

    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.isActive = true " +
            "GROUP BY u.role ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByRole();

    @Query("SELECT u.timezone, COUNT(u) FROM User u WHERE u.isActive = true AND u.timezone IS NOT NULL " +
            "GROUP BY u.timezone ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByTimezone();

    @Query("SELECT u.language, COUNT(u) FROM User u WHERE u.isActive = true AND u.language IS NOT NULL " +
            "GROUP BY u.language ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByLanguage();

    // Most active users (by login count)
    List<User> findTop10ByIsActiveTrueOrderByLoginCountDesc();

    // Recently registered users
    List<User> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    // Users who haven't logged in recently
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(u.lastLogin IS NULL OR u.lastLogin < :cutoffDate) " +
            "ORDER BY u.lastLogin ASC NULLS FIRST")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Cleanup operations
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.isActive = false AND u.updatedAt < :cutoffDate")
    void deleteInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerificationToken = null WHERE u.createdAt < :cutoffDate AND u.isEmailVerified = false")
    void clearOldVerificationTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
}