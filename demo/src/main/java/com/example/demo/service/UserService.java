package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .or(() -> userRepository.findByEmailAndIsActiveTrue(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        logger.debug("User loaded: {}", user.getUsername());
        return user;
    }

    // Basic CRUD operations
    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "userByUsername", key = "#username")
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username).orElse(null);
    }

    @Cacheable(value = "userByEmail", key = "#email")
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email).orElse(null);
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public Page<User> getAllActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
    }

    // Registration and authentication
    @Transactional
    @CacheEvict(value = {"user", "userByUsername", "userByEmail", "allUsers"}, allEntries = true)
    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate email verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());

        // Set default values
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());

        return savedUser;
    }

    @Transactional
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndIsActiveTrue(username)
                .or(() -> userRepository.findByEmailAndIsActiveTrue(username));

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Update login statistics
                user.incrementLoginCount();
                userRepository.save(user);
                logger.info("User authenticated successfully: {}", user.getUsername());
                return true;
            }
        }

        logger.warn("Authentication failed for user: {}", username);
        return false;
    }

    // Profile management
    @Transactional
    @CacheEvict(value = {"user", "userByUsername", "userByEmail"}, key = "#userId")
    public User updateUserProfile(Long userId, String firstName, String lastName, String email) {
        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check email uniqueness if changed
        if (!user.getEmail().equals(email) && userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = {"user", "userByUsername", "userByEmail"}, key = "#userId")
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed for user: {}", user.getUsername());
        return true;
    }

    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void updateProfileImage(Long userId, String imageUrl) {
        userRepository.updateProfileImage(userId, imageUrl);
    }

    // Email verification
    @Transactional
    @CacheEvict(value = {"user", "userByEmail"}, allEntries = true)
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            userRepository.verifyEmail(token);
            logger.info("Email verified for token: {}", token);
            return true;
        }
        return false;
    }

    public List<User> getUnverifiedUsers() {
        return userRepository.findByIsEmailVerifiedFalseAndIsActiveTrueOrderByCreatedAtDesc();
    }

    // Password reset
    @Transactional
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetExpires(LocalDateTime.now().plusHours(24)); // 24 hours expiry

            userRepository.save(user);
            logger.info("Password reset initiated for user: {}", user.getUsername());
            return true;
        }
        return false;
    }

    @Transactional
    @CacheEvict(value = {"user", "userByEmail"}, allEntries = true)
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isPasswordResetTokenValid()) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                user.setPasswordResetExpires(null);

                userRepository.save(user);
                logger.info("Password reset completed for user: {}", user.getUsername());
                return true;
            }
        }
        return false;
    }

    // User management (Admin functions)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveUsers(pageable);
        }
        return userRepository.searchUsers(keyword.trim(), pageable);
    }

    @Transactional
    @CacheEvict(value = {"user", "allUsers"}, allEntries = true)
    public void deactivateUser(Long userId) {
        userRepository.updateActiveStatus(userId, false);
        logger.info("User deactivated: {}", userId);
    }

    @Transactional
    @CacheEvict(value = {"user", "allUsers"}, allEntries = true)
    public void activateUser(Long userId) {
        userRepository.updateActiveStatus(userId, true);
        logger.info("User activated: {}", userId);
    }

    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void updateUserRole(Long userId, User.Role role) {
        userRepository.updateUserRole(userId, role);
        logger.info("User role updated: {} to {}", userId, role);
    }

    // Newsletter management
    @Transactional
    public void subscribeToNewsletter(String email) {
        userRepository.updateNewsletterSubscriptionByEmail(email, true);
    }

    @Transactional
    public void unsubscribeFromNewsletter(String email) {
        userRepository.updateNewsletterSubscriptionByEmail(email, false);
    }

    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void updateNewsletterSubscription(Long userId, boolean subscribed) {
        userRepository.updateNewsletterSubscription(userId, subscribed);
    }

    public List<User> getNewsletterSubscribers() {
        return userRepository.findByNewsletterSubscribedTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }

    public Page<User> getNewsletterSubscribers(Pageable pageable) {
        return userRepository.findByNewsletterSubscribedTrueAndIsActiveTrueOrderByCreatedAtDesc(pageable);
    }

    // User preferences
    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void updatePreferredCategories(Long userId, List<Long> categoryIds) {
        String categories = categoryIds != null && !categoryIds.isEmpty()
                ? String.join(",", categoryIds.stream().map(String::valueOf).toArray(String[]::new))
                : "";
        userRepository.updatePreferredCategories(userId, categories);
    }

    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void updateUserTimezone(Long userId, String timezone) {
        userRepository.updateTimezone(userId, timezone);
    }

    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void updateUserLanguage(Long userId, String language) {
        userRepository.updateLanguage(userId, language);
    }

    // Role-based queries
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRoleAndIsActiveTrueOrderByCreatedAtDesc(role);
    }

    public Page<User> getUsersByRole(User.Role role, Pageable pageable) {
        return userRepository.findByRoleAndIsActiveTrueOrderByCreatedAtDesc(role, pageable);
    }

    // Statistics and analytics
    public long getTotalActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }

    public long getTotalInactiveUsers() {
        return userRepository.countByIsActiveFalse();
    }

    public long getNewUsersCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.countNewUsers(since);
    }

    public long getActiveUsersCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.countActiveUsers(since);
    }

    public long getUsersByRoleCount(User.Role role) {
        return userRepository.countByRoleAndIsActiveTrue(role);
    }

    public long getNewsletterSubscribersCount() {
        return userRepository.countByNewsletterSubscribedTrueAndIsActiveTrue();
    }

    public List<Object[]> getUserRegistrationsByDate(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.getUserRegistrationsByDate(since);
    }

    public List<Object[]> getUsersByRole() {
        return userRepository.getUsersByRole();
    }

    public List<Object[]> getUsersByTimezone() {
        return userRepository.getUsersByTimezone();
    }

    public List<Object[]> getUsersByLanguage() {
        return userRepository.getUsersByLanguage();
    }

    public List<User> getMostActiveUsers() {
        return userRepository.findTop10ByIsActiveTrueOrderByLoginCountDesc();
    }

    public List<User> getRecentlyRegisteredUsers() {
        return userRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    }

    public List<User> getInactiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRepository.findInactiveUsers(cutoffDate);
    }

    // Validation methods
    public boolean isUsernameAvailable(String username, Long excludeUserId) {
        if (excludeUserId != null) {
            return !userRepository.existsByUsernameAndIdNot(username, excludeUserId);
        }
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email, Long excludeUserId) {
        if (excludeUserId != null) {
            return !userRepository.existsByEmailAndIdNot(email, excludeUserId);
        }
        return !userRepository.existsByEmail(email);
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Maintenance operations
    @Transactional
    public void cleanupExpiredPasswordResetTokens() {
        userRepository.clearExpiredPasswordResetTokens(LocalDateTime.now());
        logger.info("Expired password reset tokens cleared");
    }

    @Transactional
    public void cleanupOldVerificationTokens(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        userRepository.clearOldVerificationTokens(cutoffDate);
        logger.info("Old email verification tokens cleared");
    }

    @Transactional
    public void deleteInactiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        userRepository.deleteInactiveUsers(cutoffDate);
        logger.info("Inactive users deleted");
    }

    // Bulk operations
    @Transactional
    @CacheEvict(value = {"user", "allUsers"}, allEntries = true)
    public void bulkActivateUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            activateUser(userId);
        }
    }

    @Transactional
    @CacheEvict(value = {"user", "allUsers"}, allEntries = true)
    public void bulkDeactivateUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            deactivateUser(userId);
        }
    }

    @Transactional
    @CacheEvict(value = {"user", "allUsers"}, allEntries = true)
    public void bulkUpdateUserRole(List<Long> userIds, User.Role role) {
        for (Long userId : userIds) {
            updateUserRole(userId, role);
        }
    }

    // Helper methods for user creation
    public User createUser(String username, String email, String password,
                           String firstName, String lastName, User.Role role) {
        User user = new User(username, email, password, firstName, lastName);
        user.setRole(role != null ? role : User.Role.USER);
        return registerUser(user);
    }

    public User createAdminUser(String username, String email, String password,
                                String firstName, String lastName) {
        return createUser(username, email, password, firstName, lastName, User.Role.ADMIN);
    }

    // Initialize default admin user
    @Transactional
    public void initializeDefaultAdmin() {
        if (userRepository.countByRoleAndIsActiveTrue(User.Role.ADMIN) == 0) {
            logger.info("No admin user found, creating default admin...");

            User admin = new User("admin", "admin@newsplatform.com", "admin123");
            admin.setFirstName("News");
            admin.setLastName("Admin");
            admin.setRole(User.Role.ADMIN);
            admin.setIsEmailVerified(true);
            admin.setEmailVerificationToken(null);

            registerUser(admin);
            logger.info("Default admin user created successfully");
        }
    }

    // User activity tracking
    @Transactional
    public void recordUserLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsernameAndIsActiveTrue(username)
                .or(() -> userRepository.findByEmailAndIsActiveTrue(username));

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
        }
    }
}