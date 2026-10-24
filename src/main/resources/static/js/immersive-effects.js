// ==========================================================================
// TravelGO - Cinematic Scroll-Driven Motion & Smooth Parallax Engine
// Features: Cinematic Section Reveal, Coordinated Child Staggers,
//           Multi-Plane Scenic Parallax, Hero Curtain Transition,
//           Refined Public Page Transitions & Crossfade Slideshow
// Note: 100% Native Browser Scrolling Preserved (Zero Virtual Scroll / Wheel Hijacking)
//       Zero Cursor-Following Tilt / Zero Mouse Magnetic Pull / Zero WebGL Overlays
// ==========================================================================

(function () {
    'use strict';

    const prefersReducedMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // --------------------------------------------------------------------------
    // 1. Scroll-Driven Progress Bar & Navbar Scroll State
    // --------------------------------------------------------------------------
    function initScrollProgress() {
        const progressBar = document.getElementById('scroll-progress');
        const navbar = document.querySelector('.navbar-glass, .navbar');

        function onScroll() {
            const scrollTop = window.scrollY || document.documentElement.scrollTop;
            const docHeight = document.documentElement.scrollHeight - window.innerHeight;

            if (progressBar && docHeight > 0) {
                const scrollPercent = Math.min(100, Math.max(0, (scrollTop / docHeight) * 100));
                progressBar.style.width = scrollPercent + '%';
            }

            if (navbar) {
                if (scrollTop > 60) {
                    navbar.classList.add('navbar-scrolled');
                } else {
                    navbar.classList.remove('navbar-scrolled');
                }
            }
        }

        window.addEventListener('scroll', onScroll, { passive: true });
        onScroll();
    }

    // --------------------------------------------------------------------------
    // 2. Scroll-Down Anchor Navigation
    // --------------------------------------------------------------------------
    function initScrollDownAnimation() {
        const scrollDownBtn = document.getElementById('heroScrollDownBtn');
        if (scrollDownBtn) {
            scrollDownBtn.addEventListener('click', function (e) {
                e.preventDefault();
                const targetId = scrollDownBtn.getAttribute('href');
                if (targetId && targetId.startsWith('#')) {
                    const targetEl = document.querySelector(targetId);
                    if (targetEl) {
                        targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
                        return;
                    }
                }
                // Fallback: smooth scroll by 90% of viewport height
                window.scrollBy({ top: window.innerHeight * 0.9, behavior: 'smooth' });
            });
        }
    }

    // --------------------------------------------------------------------------
    // 3. Cinematic Hero UHD Video Background Engine
    // --------------------------------------------------------------------------
    function initHeroVideo() {
        const video = document.querySelector('.hero-video-bg');
        if (!video) return;

        video.muted = true;
        video.defaultMuted = true;

        const playPromise = video.play();
        if (playPromise !== undefined) {
            playPromise.catch(() => {
                const resumePlayback = () => {
                    video.play().catch(() => {});
                    window.removeEventListener('click', resumePlayback);
                    window.removeEventListener('scroll', resumePlayback);
                    window.removeEventListener('touchstart', resumePlayback);
                };
                window.addEventListener('click', resumePlayback, { passive: true, once: true });
                window.addEventListener('scroll', resumePlayback, { passive: true, once: true });
                window.addEventListener('touchstart', resumePlayback, { passive: true, once: true });
            });
        }
    }

    // --------------------------------------------------------------------------
    // 3b. Cinematic Hero Crossfade Slideshow (Static, Crystal-Clear Photos)
    // --------------------------------------------------------------------------
    function initHeroSlideshow() {
        const slideshowContainers = document.querySelectorAll('.hero-slideshow-container');
        if (!slideshowContainers.length) return;

        slideshowContainers.forEach(container => {
            const slides = container.querySelectorAll('.hero-slide');
            const dots = container.querySelectorAll('.hero-slide-dot');
            if (slides.length <= 1) return;

            let currentIndex = 0;
            let slideInterval = null;

            function goToSlide(index) {
                slides.forEach((slide, i) => {
                    if (i === index) {
                        slide.classList.add('active');
                        slide.style.opacity = '1';
                    } else {
                        slide.classList.remove('active');
                        slide.style.opacity = '0';
                    }
                });

                if (dots.length) {
                    dots.forEach((dot, i) => {
                        dot.classList.toggle('active', i === index);
                    });
                }

                currentIndex = index;
            }

            function nextSlide() {
                const next = (currentIndex + 1) % slides.length;
                goToSlide(next);
            }

            dots.forEach((dot, i) => {
                dot.addEventListener('click', (e) => {
                    e.preventDefault();
                    goToSlide(i);
                    resetTimer();
                });
            });

            function startTimer() {
                if (prefersReducedMotion) return;
                slideInterval = setInterval(nextSlide, 6000);
            }

            function resetTimer() {
                if (slideInterval) clearInterval(slideInterval);
                startTimer();
            }

            goToSlide(0);
            startTimer();
        });
    }

    // --------------------------------------------------------------------------
    // 4. CINEMATIC SECTION REVEAL: Bidirectional Scroll-Driven Entrance Engine
    // --------------------------------------------------------------------------
    function initSectionScrollAnimations() {
        const animatedSections = document.querySelectorAll(
            '.scroll-section-animate, .scroll-down-reveal, .reveal-up, .reveal-fade, .reveal-scale, .destinations-showcase-section'
        );

        if (!animatedSections.length) return;

        if (prefersReducedMotion || !('IntersectionObserver' in window)) {
            animatedSections.forEach(sec => {
                sec.classList.add('section-visible', 'revealed', 'is-in-view');
            });
            return;
        }

        const sectionObserver = new IntersectionObserver((entries) => {
            const vh = window.innerHeight || document.documentElement.clientHeight;

            entries.forEach(entry => {
                const el = entry.target;

                if (entry.isIntersecting) {
                    // Entering viewport on downward or upward scroll
                    el.classList.add('section-visible', 'revealed', 'is-in-view');
                } else {
                    // Leaving viewport: Bidirectional behavior
                    // Only remove visible classes if user scrolled UP away from it (it exited out the bottom)
                    // If it scrolled off the top, keep it visible so reading is never disrupted
                    if (entry.boundingClientRect.top > vh * 0.90) {
                        el.classList.remove('section-visible', 'revealed', 'is-in-view');
                    }
                }
            });
        }, {
            threshold: 0.08,
            rootMargin: '0px 0px -20px 0px'
        });

        animatedSections.forEach(sec => sectionObserver.observe(sec));
    }

    // --------------------------------------------------------------------------
    // 5. SCROLL-DRIVEN PARALLAX: Smooth Multi-Plane Depth on Scenic Backgrounds & Cards
    // --------------------------------------------------------------------------
    function initScrollParallax() {
        if (prefersReducedMotion) return;

        const scenicSections = document.querySelectorAll('.section-scenic');
        const showcaseCards = document.querySelectorAll('.destination-showcase-card, .bento-destination-card');

        if (!scenicSections.length && !showcaseCards.length) return;

        // Track only elements currently visible in viewport to maximize 60fps performance
        const activeParallaxElements = new Set();

        const parallaxVisibilityObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    activeParallaxElements.add(entry.target);
                } else {
                    activeParallaxElements.delete(entry.target);
                }
            });
        }, {
            rootMargin: '120px 0px 120px 0px'
        });

        scenicSections.forEach(el => parallaxVisibilityObserver.observe(el));
        showcaseCards.forEach(el => parallaxVisibilityObserver.observe(el));

        let ticking = false;

        function updateParallax() {
            const vh = window.innerHeight;

            activeParallaxElements.forEach(el => {
                const rect = el.getBoundingClientRect();
                // Normalized progress from -0.5 (just entering bottom) to +0.5 (just exiting top)
                const center = rect.top + rect.height / 2;
                const progress = (vh / 2 - center) / (vh + rect.height);

                // Scenic Background Parallax Shift (±45px)
                if (el.classList.contains('section-scenic')) {
                    const bg = el.querySelector('.section-scenic-bg');
                    if (bg) {
                        const shift = (progress * 90).toFixed(1);
                        bg.style.transform = `translate3d(0, ${shift}px, 0)`;
                    }
                }

                // Destination Card Photo Lens Parallax Shift (±18px)
                if ((el.classList.contains('destination-showcase-card') || el.classList.contains('bento-destination-card')) && window.innerWidth >= 768) {
                    const img = el.querySelector('.destination-showcase-img, .bento-destination-img');
                    if (img && !el.matches(':hover')) {
                        const shift = (progress * 36).toFixed(1);
                        // Retain a small overscan while shifting so no card edge
                        // exposes the scenic section background.
                        img.style.transform = `scale(1.08) translate3d(0, ${shift}px, 0)`;
                    }
                }
            });

            ticking = false;
        }

        window.addEventListener('scroll', () => {
            if (!ticking && activeParallaxElements.size > 0) {
                window.requestAnimationFrame(updateParallax);
                ticking = true;
            }
        }, { passive: true });

        // Initial paint
        updateParallax();
    }

    // --------------------------------------------------------------------------
    // 6. HOMEPAGE HERO CURTAIN TRANSITION: Cinematic Theatre-Lift Effect
    // --------------------------------------------------------------------------
    function initHeroCurtainTransition() {
        if (prefersReducedMotion || window.innerWidth < 992) return;

        const hero = document.querySelector('.curtain-hero-sticky');
        const heroContent = document.querySelector('.hero-lower-left');
        if (!hero || !heroContent) return;

        let ticking = false;

        function updateHeroCurtain() {
            const scrollY = window.scrollY;
            const heroHeight = hero.offsetHeight || window.innerHeight;

            if (scrollY <= heroHeight) {
                const progress = Math.min(scrollY / heroHeight, 1);
                // Content glides upward (-65px max) and fades gracefully (1 -> 0.35)
                heroContent.style.transform = `translate3d(0, ${(-progress * 65).toFixed(1)}px, 0)`;
                heroContent.style.opacity = (1 - Math.pow(progress, 1.3) * 0.65).toFixed(2);

                // Active hero slide image subtly recedes for multi-plane depth
                const activeSlide = hero.querySelector('.hero-slide.active');
                if (activeSlide) {
                    const scale = (1 + progress * 0.05).toFixed(3);
                    const translateY = (progress * 35).toFixed(1);
                    activeSlide.style.transform = `scale(${scale}) translate3d(0, ${translateY}px, 0)`;
                }
            } else {
                heroContent.style.transform = 'translate3d(0, -65px, 0)';
                heroContent.style.opacity = '0.35';
            }

            ticking = false;
        }

        window.addEventListener('scroll', () => {
            if (!ticking) {
                window.requestAnimationFrame(updateHeroCurtain);
                ticking = true;
            }
        }, { passive: true });

        updateHeroCurtain();
    }

    // --------------------------------------------------------------------------
    // 7. Refined Cinematic Page Transition Curtain
    // --------------------------------------------------------------------------
    function initPageTransitions() {
        if (prefersReducedMotion) return;

        let curtain = document.querySelector('.page-transition-curtain');
        if (!curtain) {
            curtain = document.createElement('div');
            curtain.className = 'page-transition-curtain transitioning-in';
            curtain.innerHTML = `
                <div class="page-transition-bar"></div>
                <div class="page-transition-content">
                    <div class="page-transition-minimal-brand">
                        <span>TravelGO</span>
                    </div>
                </div>
            `;
            document.body.appendChild(curtain);

            // Animate incoming page reveal quickly (200ms)
            requestAnimationFrame(() => {
                setTimeout(() => {
                    curtain.classList.remove('transitioning-in');
                }, 200);
            });
        }

        // Support browser history bfcache (instant restoration on back/forward)
        window.addEventListener('pageshow', () => {
            if (curtain) {
                curtain.classList.remove('transitioning-out', 'transitioning-in');
            }
        });

        // Intercept only eligible public same-origin navigation
        document.addEventListener('click', (e) => {
            const link = e.target.closest('a');
            if (!link) return;

            const href = link.getAttribute('href');
            if (!href) return;

            // Strict filters: ignore hash anchors, protocols, targets, downloads, modified clicks
            if (
                href.startsWith('#') ||
                href.startsWith('javascript:') ||
                href.startsWith('mailto:') ||
                href.startsWith('tel:') ||
                link.getAttribute('target') === '_blank' ||
                link.hasAttribute('download') ||
                e.ctrlKey || e.metaKey || e.shiftKey || e.altKey ||
                e.defaultPrevented
            ) {
                return;
            }

            // Do not intercept if inside a form or button
            if (link.closest('form') || link.closest('button')) return;

            let targetUrl;
            try {
                targetUrl = new URL(href, window.location.origin);
            } catch (_) {
                return;
            }

            // Same origin check
            if (targetUrl.origin !== window.location.origin) return;

            // Ignore current page click
            if (targetUrl.pathname === window.location.pathname && targetUrl.search === window.location.search) {
                return;
            }

            // Strict protection: DO NOT intercept auth, admin, customer, staff, booking, payment, or logout flows
            const p = targetUrl.pathname;
            if (
                p.startsWith('/auth') ||
                p.startsWith('/admin') ||
                p.startsWith('/customer') ||
                p.startsWith('/staff') ||
                p.includes('/book') ||
                p.includes('/payment') ||
                p.includes('/logout')
            ) {
                return;
            }

            // Public navigation approved: trigger subtle transition
            e.preventDefault();
            curtain.classList.remove('transitioning-in');
            curtain.classList.add('transitioning-out');

            let navigated = false;

            // Fast, responsive navigation (200ms)
            setTimeout(() => {
                navigated = true;
                window.location.href = targetUrl.href;
            }, 200);

            // Safe fallback timeout (350ms): never leave user on blank screen
            setTimeout(() => {
                if (!navigated) {
                    window.location.href = targetUrl.href;
                }
                setTimeout(() => {
                    curtain.classList.remove('transitioning-out');
                }, 400);
            }, 350);
        });
    }

    // --------------------------------------------------------------------------
    // Master Initialization
    // --------------------------------------------------------------------------
    function initAll() {
        initPageTransitions();
        initScrollProgress();
        initScrollDownAnimation();
        initHeroVideo();
        initHeroSlideshow();
        initSectionScrollAnimations();
        initScrollParallax();
        initHeroCurtainTransition();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }

})();
