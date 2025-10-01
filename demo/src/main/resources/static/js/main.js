/**
 * Main JavaScript for News Platform
 * Handles theme switching, responsive behavior, search, and general interactions
 */

(function() {
    'use strict';

    // Configuration
    const CONFIG = {
        THEME_KEY: 'news-platform-theme',
        SEARCH_DEBOUNCE: 300,
        SCROLL_THRESHOLD: 300,
        ANIMATION_DURATION: 300
    };

    // Global state
    let currentTheme = 'light';
    let scrollPosition = 0;
    let isScrolling = false;

    // Initialize everything when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        initializeTheme();
        initializeNavigation();
        initializeSearch();
        initializeScrollEffects();
        initializeShareButtons();
        initializeLazyLoading();
        initializeBreakingNewsTicker();
        initializeNewsletterForm();
        initializeTooltips();

        console.log('News Platform initialized successfully');
    });

    // Theme Management
    function initializeTheme() {
        const themeToggle = document.getElementById('themeToggle');
        const themeIcon = document.getElementById('themeIcon');

        // Load saved theme or default to light
        const savedTheme = localStorage.getItem(CONFIG.THEME_KEY) || 'light';
        setTheme(savedTheme);

        if (themeToggle) {
            themeToggle.addEventListener('click', function() {
                const newTheme = currentTheme === 'light' ? 'dark' : 'light';
                setTheme(newTheme);
            });
        }

        function setTheme(theme) {
            currentTheme = theme;
            document.documentElement.setAttribute('data-theme', theme);
            localStorage.setItem(CONFIG.THEME_KEY, theme);

            if (themeIcon) {
                themeIcon.className = theme === 'light' ? 'fas fa-moon' : 'fas fa-sun';
            }

            // Update theme color meta tag
            const themeColorMeta = document.querySelector('meta[name="theme-color"]');
            if (themeColorMeta) {
                themeColorMeta.content = theme === 'light' ? '#2563eb' : '#1e293b';
            }
        }
    }

    // Navigation Enhancement
    function initializeNavigation() {
        const navbar = document.querySelector('.navbar');
        const navbarToggler = document.querySelector('.navbar-toggler');
        const navbarCollapse = document.querySelector('.navbar-collapse');

        // Add scroll effect to navbar
        window.addEventListener('scroll', debounce(function() {
            const scrolled = window.scrollY > 50;

            if (navbar) {
                navbar.classList.toggle('scrolled', scrolled);
                if (scrolled) {
                    navbar.style.background = 'rgba(37, 99, 235, 0.95)';
                    navbar.style.backdropFilter = 'blur(10px)';
                } else {
                    navbar.style.background = '';
                    navbar.style.backdropFilter = '';
                }
            }
        }, 10));

        // Close mobile menu when clicking outside
        document.addEventListener('click', function(e) {
            if (navbarCollapse && navbarCollapse.classList.contains('show')) {
                if (!navbarCollapse.contains(e.target) && !navbarToggler.contains(e.target)) {
                    const bsCollapse = new bootstrap.Collapse(navbarCollapse, {
                        hide: true
                    });
                }
            }
        });

        // Close mobile menu when clicking on nav links
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (navbarCollapse && navbarCollapse.classList.contains('show')) {
                    const bsCollapse = new bootstrap.Collapse(navbarCollapse, {
                        hide: true
                    });
                }
            });
        });
    }

    // Enhanced Search Functionality
    function initializeSearch() {
        const searchForm = document.querySelector('form[action="/search"]');
        const searchInput = searchForm?.querySelector('input[name="q"]');
        const searchSuggestions = createSearchSuggestions();

        if (!searchInput) return;

        let searchTimeout;
        let currentSuggestions = [];

        searchInput.addEventListener('input', debounce(function() {
            const query = this.value.trim();

            if (query.length >= 2) {
                fetchSearchSuggestions(query);
            } else {
                hideSuggestions();
            }
        }, CONFIG.SEARCH_DEBOUNCE));

        searchInput.addEventListener('focus', function() {
            if (this.value.trim().length >= 2) {
                showSuggestions();
            }
        });

        searchInput.addEventListener('blur', function() {
            setTimeout(hideSuggestions, 150);
        });

        searchInput.addEventListener('keydown', function(e) {
            handleSearchKeyboard(e);
        });

        function createSearchSuggestions() {
            const suggestions = document.createElement('div');
            suggestions.className = 'search-suggestions';
            suggestions.style.cssText = `
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                background: white;
                border: 1px solid #e2e8f0;
                border-radius: 0.5rem;
                box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
                z-index: 1000;
                max-height: 300px;
                overflow-y: auto;
                display: none;
            `;

            searchForm.style.position = 'relative';
            searchForm.appendChild(suggestions);
            return suggestions;
        }

        function fetchSearchSuggestions(query) {
            // Simulate API call - replace with actual endpoint
            const mockSuggestions = [
                'Technology news',
                'Sports updates',
                'Political developments',
                'Entertainment news',
                'Business trends'
            ].filter(item => item.toLowerCase().includes(query.toLowerCase()));

            displaySuggestions(mockSuggestions);
        }

        function displaySuggestions(suggestions) {
            currentSuggestions = suggestions;
            searchSuggestions.innerHTML = '';

            if (suggestions.length === 0) {
                hideSuggestions();
                return;
            }

            suggestions.forEach((suggestion, index) => {
                const item = document.createElement('div');
                item.className = 'suggestion-item';
                item.style.cssText = `
                    padding: 0.75rem;
                    cursor: pointer;
                    border-bottom: 1px solid #f1f5f9;
                    transition: background-color 0.15s ease;
                `;
                item.textContent = suggestion;
                item.setAttribute('data-index', index);

                item.addEventListener('mouseenter', function() {
                    this.style.backgroundColor = '#f8fafc';
                });

                item.addEventListener('mouseleave', function() {
                    this.style.backgroundColor = '';
                });

                item.addEventListener('click', function() {
                    searchInput.value = suggestion;
                    hideSuggestions();
                    searchForm.submit();
                });

                searchSuggestions.appendChild(item);
            });

            showSuggestions();
        }

        function showSuggestions() {
            searchSuggestions.style.display = 'block';
        }

        function hideSuggestions() {
            searchSuggestions.style.display = 'none';
        }

        function handleSearchKeyboard(e) {
            const items = searchSuggestions.querySelectorAll('.suggestion-item');
            let selectedIndex = Array.from(items).findIndex(item =>
                item.style.backgroundColor === '#f8fafc'
            );

            switch (e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                    highlightSuggestion(items, selectedIndex);
                    break;

                case 'ArrowUp':
                    e.preventDefault();
                    selectedIndex = Math.max(selectedIndex - 1, 0);
                    highlightSuggestion(items, selectedIndex);
                    break;

                case 'Enter':
                    if (selectedIndex >= 0 && items[selectedIndex]) {
                        e.preventDefault();
                        items[selectedIndex].click();
                    }
                    break;

                case 'Escape':
                    hideSuggestions();
                    break;
            }
        }

        function highlightSuggestion(items, index) {
            items.forEach((item, i) => {
                item.style.backgroundColor = i === index ? '#f8fafc' : '';
            });
        }
    }

    // Scroll Effects and Animations
    function initializeScrollEffects() {
        const scrollToTopBtn = document.getElementById('scrollToTop');

        // Show/hide scroll to top button
        window.addEventListener('scroll', throttle(function() {
            const scrolled = window.scrollY > CONFIG.SCROLL_THRESHOLD;

            if (scrollToTopBtn) {
                scrollToTopBtn.classList.toggle('show', scrolled);
            }

            // Update scroll position
            scrollPosition = window.scrollY;
        }, 16)); // ~60fps

        // Scroll to top functionality
        if (scrollToTopBtn) {
            scrollToTopBtn.addEventListener('click', function(e) {
                e.preventDefault();
                smoothScrollTo(0, 800);
            });
        }

        // Smooth scroll for anchor links
        document.querySelectorAll('a[href^="#"]').forEach(link => {
            link.addEventListener('click', function(e) {
                const targetId = this.getAttribute('href');
                const target = document.querySelector(targetId);

                if (target) {
                    e.preventDefault();
                    smoothScrollToElement(target);
                }
            });
        });
    }

    // Share Functionality
    function initializeShareButtons() {
        const shareButtons = document.querySelectorAll('.share-btn');

        shareButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const url = this.getAttribute('data-url') || window.location.href;
                const title = this.getAttribute('data-title') || document.title;

                if (navigator.share) {
                    // Use native Web Share API if available
                    navigator.share({
                        title: title,
                        url: url
                    }).catch(err => console.log('Share cancelled'));
                } else {
                    // Fallback to clipboard
                    copyToClipboard(url);
                    showNotification('Link copied to clipboard!');
                }
            });
        });
    }

    // Lazy Loading for Images
    function initializeLazyLoading() {
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        const src = img.getAttribute('data-src') || img.src;

                        if (src) {
                            const newImg = new Image();
                            newImg.onload = function() {
                                img.src = src;
                                img.classList.remove('loading');
                                img.classList.add('loaded');
                            };
                            newImg.src = src;
                        }

                        observer.unobserve(img);
                    }
                });
            }, {
                rootMargin: '50px'
            });

            document.querySelectorAll('img[data-src], img[loading="lazy"]').forEach(img => {
                img.classList.add('loading');
                imageObserver.observe(img);
            });
        }
    }

    // Breaking News Ticker Enhancement
    function initializeBreakingNewsTicker() {
        const ticker = document.getElementById('breakingNewsTicker');
        if (!ticker) return;

        // Pause animation on hover
        ticker.addEventListener('mouseenter', function() {
            this.style.animationPlayState = 'paused';
        });

        ticker.addEventListener('mouseleave', function() {
            this.style.animationPlayState = 'running';
        });

        // Touch pause for mobile
        ticker.addEventListener('touchstart', function() {
            this.style.animationPlayState = 'paused';
        });

        ticker.addEventListener('touchend', function() {
            setTimeout(() => {
                this.style.animationPlayState = 'running';
            }, 2000);
        });
    }

    // Newsletter Form
    function initializeNewsletterForm() {
        const newsletterForm = document.querySelector('.newsletter-form');

        if (newsletterForm) {
            newsletterForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const email = this.querySelector('input[type="email"]').value;

                // Validate email
                if (!isValidEmail(email)) {
                    showNotification('Please enter a valid email address.', 'error');
                    return;
                }

                // Simulate API call
                showNotification('Subscribing...', 'info');

                setTimeout(() => {
                    showNotification('Successfully subscribed to newsletter!', 'success');
                    this.reset();
                }, 1000);
            });
        }
    }

    // Tooltips Enhancement
    function initializeTooltips() {
        // Initialize Bootstrap tooltips
        if (typeof bootstrap !== 'undefined') {
            const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl);
            });
        }
    }

    // Add WebSocket connection for real-time news updates
    const newsSocket = new WebSocket('ws://localhost:8080/ws/news');

    newsSocket.onopen = () => {
        console.log('Connected to the news WebSocket');
    };

    newsSocket.onmessage = (event) => {
        const newsUpdate = JSON.parse(event.data);
        displayNewsUpdate(newsUpdate);
    };

    newsSocket.onclose = () => {
        console.log('Disconnected from the news WebSocket');
    };

    function displayNewsUpdate(newsUpdate) {
        // Example: Append the news update to a news feed container
        const newsFeed = document.getElementById('news-feed');
        const newsItem = document.createElement('div');
        newsItem.className = 'news-item';
        newsItem.textContent = `${newsUpdate.title}: ${newsUpdate.content}`;
        newsFeed.prepend(newsItem);
    }

    // Utility Functions
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    function throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    function smoothScrollTo(to, duration) {
        const start = window.scrollY;
        const change = to - start;
        const startTime = performance.now();

        function animateScroll(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const easing = easeInOutCubic(progress);

            window.scrollTo(0, start + (change * easing));

            if (progress < 1) {
                requestAnimationFrame(animateScroll);
            }
        }

        requestAnimationFrame(animateScroll);
    }

    function smoothScrollToElement(element, offset = 80) {
        const elementTop = element.getBoundingClientRect().top + window.scrollY;
        const targetPosition = elementTop - offset;

        smoothScrollTo(targetPosition, 800);
    }

    function easeInOutCubic(t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    function copyToClipboard(text) {
        if (navigator.clipboard && window.isSecureContext) {
            return navigator.clipboard.writeText(text);
        } else {
            // Fallback method
            const textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            textArea.style.top = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();

            return new Promise((resolve, reject) => {
                document.execCommand('copy') ? resolve() : reject();
                textArea.remove();
            });
        }
    }

    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    function showNotification(message, type = 'info', duration = 3000) {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${getNotificationColor(type)};
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 0.5rem;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
            z-index: 10000;
            transform: translateX(100%);
            transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            max-width: 300px;
            font-weight: 500;
        `;
        notification.textContent = message;

        // Add to DOM
        document.body.appendChild(notification);

        // Animate in
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 10);

        // Auto remove
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, duration);

        // Click to dismiss
        notification.addEventListener('click', () => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        });
    }

    function getNotificationColor(type) {
        const colors = {
            success: '#10b981',
            error: '#ef4444',
            warning: '#f59e0b',
            info: '#3b82f6'
        };
        return colors[type] || colors.info;
    }

    // Performance monitoring
    function logPerformanceMetrics() {
        if ('performance' in window) {
            window.addEventListener('load', () => {
                setTimeout(() => {
                    const perfData = performance.getEntriesByType('navigation')[0];
                    console.log('Performance Metrics:', {
                        pageLoadTime: Math.round(perfData.loadEventEnd - perfData.fetchStart),
                        domContentLoaded: Math.round(perfData.domContentLoadedEventEnd - perfData.fetchStart),
                        firstByte: Math.round(perfData.responseStart - perfData.fetchStart)
                    });
                }, 1000);
            });
        }
    }

    // Initialize performance monitoring
    logPerformanceMetrics();

    // Expose utilities globally for debugging
    window.NewsUtils = {
        debounce,
        throttle,
        smoothScrollTo,
        smoothScrollToElement,
        copyToClipboard,
        showNotification,
        isValidEmail
    };

})();