// ==========================================================================
// TravelGO - Master Interaction & Animation Engine
// Features: IntersectionObserver reveals, 3D card tilt, magnetic buttons,
//           animated counters, frosted navbar transform & mobile drawer
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // --------------------------------------------------------------------------
    // 1. Navbar Scroll Transformation & Active Route Highlighting
    // --------------------------------------------------------------------------
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        const updateNavbar = () => {
            if (window.scrollY > 20) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        };
        window.addEventListener('scroll', updateNavbar, { passive: true });
        updateNavbar();
    }

    // Highlight active link matching current pathname
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link, .mobile-nav-link');
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        }
    });

    // --------------------------------------------------------------------------
    // 2. Mobile Drawer Navigation
    // --------------------------------------------------------------------------
    const navToggle = document.querySelector('.nav-toggle');
    const drawer = document.querySelector('.mobile-nav-drawer');
    const backdrop = document.querySelector('.mobile-nav-backdrop');
    const drawerClose = document.querySelector('.mobile-drawer-close');

    function openMobileMenu() {
        if (navToggle) navToggle.classList.add('open');
        if (drawer) drawer.classList.add('active');
        if (backdrop) backdrop.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeMobileMenu() {
        if (navToggle) navToggle.classList.remove('open');
        if (drawer) drawer.classList.remove('active');
        if (backdrop) backdrop.classList.remove('active');
        document.body.style.overflow = '';
    }

    if (navToggle) {
        navToggle.addEventListener('click', (e) => {
            e.stopPropagation();
            if (drawer && drawer.classList.contains('active')) {
                closeMobileMenu();
            } else {
                openMobileMenu();
            }
        });
    }

    if (drawerClose) {
        drawerClose.addEventListener('click', closeMobileMenu);
    }

    if (backdrop) {
        backdrop.addEventListener('click', closeMobileMenu);
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeMobileMenu();
    });

    // Close mobile menu when a nav link is clicked
    document.querySelectorAll('.mobile-nav-link').forEach(link => {
        link.addEventListener('click', closeMobileMenu);
    });

    // Note: Cinematic scroll-driven reveals & staggers are unified in immersive-effects.js

    // --------------------------------------------------------------------------
    // 5. Animated Number Counters
    // --------------------------------------------------------------------------
    const counterElements = document.querySelectorAll('.hero-stat-number, .stat-value');
    if (counterElements.length > 0 && 'IntersectionObserver' in window) {
        const counterObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const el = entry.target;
                    const text = el.textContent.trim();
                    const match = text.match(/([0-9,.]+)/);

                    if (match) {
                        const rawNumber = parseFloat(match[0].replace(/,/g, ''));
                        const prefix = text.slice(0, match.index);
                        const suffix = text.slice(match.index + match[0].length);
                        const isDecimal = match[0].includes('.');

                        animateValue(el, 0, rawNumber, 1200, prefix, suffix, isDecimal);
                    }
                    counterObserver.unobserve(el);
                }
            });
        }, { threshold: 0.4 });

        counterElements.forEach(el => counterObserver.observe(el));
    }

    function animateValue(element, start, end, duration, prefix = '', suffix = '', isDecimal = false) {
        const startTime = performance.now();
        const step = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            // Ease-out expo
            const eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
            const current = start + (end - start) * eased;

            const formatted = isDecimal ? current.toFixed(1) : Math.round(current).toLocaleString();
            element.textContent = `${prefix}${formatted}${suffix}`;

            if (progress < 1) {
                requestAnimationFrame(step);
            }
        };
        requestAnimationFrame(step);
    }

    // --------------------------------------------------------------------------
    // 6. Scroll-To-Top Button
    // --------------------------------------------------------------------------
    const scrollBtn = document.querySelector('.scroll-top-btn');
    if (scrollBtn) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 450) {
                scrollBtn.classList.add('visible');
            } else {
                scrollBtn.classList.remove('visible');
            }
        }, { passive: true });

        scrollBtn.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // --------------------------------------------------------------------------
    // 7. Auto-Dismiss Alert Notifications
    // --------------------------------------------------------------------------
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.45s ease, transform 0.45s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(() => alert.remove(), 450);
        }, 5000);
    });
});
