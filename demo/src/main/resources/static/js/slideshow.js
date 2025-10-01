/**
 * Slideshow JavaScript for News Platform
 * Handles fullscreen slideshow with auto-play, navigation, and keyboard controls
 */

let slideIndex = 0;
let slideTimer;

/**
 * Navigate to specific slide by index
 * @param {number} n - Slide index (0-based)
 */
function currentSlide(n) {
    clearTimeout(slideTimer);
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');

    // Remove active class from all slides and dots
    slides.forEach(slide => slide.classList.remove('active'));
    dots.forEach(dot => dot.classList.remove('active'));

    // Set new slide index
    slideIndex = n;

    // Add active class to current slide and dot
    if (slides[slideIndex]) {
        slides[slideIndex].classList.add('active');
    }
    if (dots[slideIndex]) {
        dots[slideIndex].classList.add('active');
    }

    // Restart auto-play timer
    slideTimer = setTimeout(autoSlide, 5000);
}

/**
 * Navigate to next slide
 */
function nextSlide() {
    clearTimeout(slideTimer);
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');

    // Remove active class from all
    slides.forEach(slide => slide.classList.remove('active'));
    dots.forEach(dot => dot.classList.remove('active'));

    // Increment index with wrap-around
    slideIndex++;
    if (slideIndex >= slides.length) {
        slideIndex = 0;
    }

    // Add active class to new slide
    if (slides[slideIndex]) {
        slides[slideIndex].classList.add('active');
    }
    if (dots[slideIndex]) {
        dots[slideIndex].classList.add('active');
    }

    // Restart auto-play timer
    slideTimer = setTimeout(autoSlide, 5000);
}

/**
 * Navigate to previous slide
 */
function prevSlide() {
    clearTimeout(slideTimer);
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');

    // Remove active class from all
    slides.forEach(slide => slide.classList.remove('active'));
    dots.forEach(dot => dot.classList.remove('active'));

    // Decrement index with wrap-around
    slideIndex--;
    if (slideIndex < 0) {
        slideIndex = slides.length - 1;
    }

    // Add active class to new slide
    if (slides[slideIndex]) {
        slides[slideIndex].classList.add('active');
    }
    if (dots[slideIndex]) {
        dots[slideIndex].classList.add('active');
    }

    // Restart auto-play timer
    slideTimer = setTimeout(autoSlide, 5000);
}

/**
 * Auto-advance to next slide
 */
function autoSlide() {
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');

    // Safety check
    if (slides.length === 0) return;

    // Remove active class from all
    slides.forEach(slide => slide.classList.remove('active'));
    dots.forEach(dot => dot.classList.remove('active'));

    // Increment index with wrap-around
    slideIndex++;
    if (slideIndex >= slides.length) {
        slideIndex = 0;
    }

    // Add active class to new slide
    if (slides[slideIndex]) {
        slides[slideIndex].classList.add('active');
    }
    if (dots[slideIndex]) {
        dots[slideIndex].classList.add('active');
    }

    // Schedule next auto-advance
    slideTimer = setTimeout(autoSlide, 5000);
}

/**
 * Initialize slideshow when DOM is ready
 */
document.addEventListener('DOMContentLoaded', function() {
    const slides = document.querySelectorAll('.slide');

    // Start auto-play if slides exist
    if (slides.length > 0) {
        slideTimer = setTimeout(autoSlide, 5000);
    }

    // Pause slideshow on hover
    const slideshowContainer = document.querySelector('.fullscreen-slideshow');
    if (slideshowContainer) {
        slideshowContainer.addEventListener('mouseenter', () => {
            clearTimeout(slideTimer);
        });

        slideshowContainer.addEventListener('mouseleave', () => {
            slideTimer = setTimeout(autoSlide, 5000);
        });
    }

    // Keyboard navigation
    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowLeft') {
            prevSlide();
        } else if (e.key === 'ArrowRight') {
            nextSlide();
        } else if (e.key === 'Escape') {
            clearTimeout(slideTimer);
        }
    });

    // Touch swipe support for mobile
    let touchStartX = 0;
    let touchEndX = 0;

    if (slideshowContainer) {
        slideshowContainer.addEventListener('touchstart', (e) => {
            touchStartX = e.changedTouches[0].screenX;
        }, { passive: true });

        slideshowContainer.addEventListener('touchend', (e) => {
            touchEndX = e.changedTouches[0].screenX;
            handleSwipe();
        }, { passive: true });
    }

    function handleSwipe() {
        const swipeThreshold = 50;
        const swipeDistance = touchEndX - touchStartX;

        if (Math.abs(swipeDistance) > swipeThreshold) {
            if (swipeDistance > 0) {
                prevSlide();
            } else {
                nextSlide();
            }
        }
    }
});