// Dark mode toggle
function initThemeToggle() {
    const navbar = document.querySelector('.navbar-nav');
    if (!navbar) return;

    // Create theme toggle button
    const li = document.createElement('li');
    li.className = 'nav-item ms-2';
    const button = document.createElement('button');
    button.className = 'btn btn-outline-primary btn-sm rounded-circle';
    button.innerHTML = '<i class="fas fa-moon"></i>';
    button.setAttribute('id', 'themeToggle');
    button.setAttribute('title', 'Toggle dark mode');
    li.appendChild(button);
    navbar.appendChild(li);

    // Check initial theme
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme === 'dark');

    // Add click handler
    button.addEventListener('click', () => {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        const newTheme = isDark ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateThemeIcon(!isDark);
    });
}

function updateThemeIcon(isDark) {
    const icon = document.querySelector('#themeToggle i');
    if (!icon) return;
    icon.className = isDark ? 'fas fa-sun' : 'fas fa-moon';
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', initThemeToggle);
