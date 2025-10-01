// Theme handling
document.addEventListener('DOMContentLoaded', function() {
    const themeToggleBtn = document.createElement('button');
    themeToggleBtn.className = 'theme-toggle btn btn-link';
    themeToggleBtn.innerHTML = '<i class="fas fa-moon"></i>';
    themeToggleBtn.setAttribute('aria-label', 'Toggle dark mode');

    // Insert the button in the navbar
    const navbar = document.querySelector('.navbar-nav');
    if (navbar) {
        const li = document.createElement('li');
        li.className = 'nav-item';
        li.appendChild(themeToggleBtn);
        navbar.appendChild(li);
    }

    // Check for saved theme preference or default to 'light'
    const currentTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', currentTheme);
    updateThemeIcon(currentTheme);

    // Theme toggle handler
    themeToggleBtn.addEventListener('click', function() {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateThemeIcon(newTheme);
    });

    function updateThemeIcon(theme) {
        const icon = themeToggleBtn.querySelector('i');
        if (theme === 'dark') {
            icon.className = 'fas fa-sun';
        } else {
            icon.className = 'fas fa-moon';
        }
    }
});
