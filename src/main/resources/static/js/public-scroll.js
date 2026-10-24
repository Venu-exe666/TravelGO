// TravelGO secondary public pages: native, accessible scroll motion.
(function () {
    'use strict';

    if (!document.body.classList.contains('public-inner-page')) return;

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const canObserve = 'IntersectionObserver' in window;
    document.documentElement.classList.add('public-motion-ready');

    function prepareMotionGroups() {
        document.querySelectorAll('[data-motion-group]').forEach(group => {
            const children = Array.from(group.children).filter(child => !child.hasAttribute('data-motion-ignore'));
            children.forEach((child, index) => {
                if (!child.hasAttribute('data-motion')) child.setAttribute('data-motion', 'up');
                child.style.setProperty('--public-motion-delay', `${Math.min(index, 6) * 85}ms`);
            });
        });
    }

    function initReveals() {
        prepareMotionGroups();
        const elements = document.querySelectorAll('[data-motion]');

        if (reduceMotion || !canObserve) {
            elements.forEach(element => element.classList.add('is-motion-visible'));
            return;
        }

        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (!entry.isIntersecting) return;
                entry.target.classList.add('is-motion-visible');
                observer.unobserve(entry.target);
            });
        }, {
            threshold: 0.12,
            rootMargin: '0px 0px -10% 0px'
        });

        elements.forEach(element => observer.observe(element));
    }

    function initAccurateSmoothLinks() {
        document.querySelectorAll('[data-scroll-link]').forEach(link => {
            link.addEventListener('click', event => {
                const targetSelector = link.getAttribute('href');
                if (!targetSelector || !targetSelector.startsWith('#')) return;
                const target = document.querySelector(targetSelector);
                if (!target) return;

                event.preventDefault();
                target.scrollIntoView({
                    behavior: reduceMotion ? 'auto' : 'smooth',
                    block: 'start'
                });
            });
        });
    }

    function initScrollProgress() {
        const progress = document.getElementById('scroll-progress');
        if (!progress) return;

        let frameRequested = false;
        const update = () => {
            const scrollable = Math.max(document.documentElement.scrollHeight - window.innerHeight, 1);
            const value = Math.min(1, Math.max(0, window.scrollY / scrollable));
            progress.style.width = `${(value * 100).toFixed(2)}%`;
            frameRequested = false;
        };

        const requestUpdate = () => {
            if (frameRequested) return;
            frameRequested = true;
            window.requestAnimationFrame(update);
        };

        window.addEventListener('scroll', requestUpdate, { passive: true });
        window.addEventListener('resize', requestUpdate, { passive: true });
        update();
    }

    function initParallax() {
        if (reduceMotion || window.innerWidth < 769 || !canObserve) return;

        const background = document.querySelector('[data-parallax-bg]');
        const media = Array.from(document.querySelectorAll('[data-parallax-media]'));
        const activeMedia = new Set();
        let frameRequested = false;

        const visibilityObserver = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) activeMedia.add(entry.target);
                else activeMedia.delete(entry.target);
            });
        }, { rootMargin: '100px 0px' });

        media.forEach(element => visibilityObserver.observe(element));

        const update = () => {
            const viewportHeight = window.innerHeight;

            if (background) {
                const pageProgress = Math.min(1, window.scrollY / Math.max(document.documentElement.scrollHeight - viewportHeight, 1));
                background.style.setProperty('--public-bg-shift', `${(pageProgress * 28).toFixed(1)}px`);
            }

            activeMedia.forEach(element => {
                const rect = element.getBoundingClientRect();
                const centerOffset = (rect.top + rect.height / 2 - viewportHeight / 2) / viewportHeight;
                const shift = Math.max(-14, Math.min(14, centerOffset * -20));
                element.style.setProperty('--public-media-shift', `${shift.toFixed(1)}px`);
            });

            frameRequested = false;
        };

        const requestUpdate = () => {
            if (frameRequested) return;
            frameRequested = true;
            window.requestAnimationFrame(update);
        };

        window.addEventListener('scroll', requestUpdate, { passive: true });
        window.addEventListener('resize', requestUpdate, { passive: true });
        update();
    }

    function initFaqBehavior() {
        const questions = document.querySelectorAll('.public-faq-item');
        questions.forEach(question => {
            question.addEventListener('toggle', () => {
                if (!question.open) return;
                questions.forEach(other => {
                    if (other !== question) other.removeAttribute('open');
                });
            });
        });
    }

    function init() {
        initReveals();
        initAccurateSmoothLinks();
        initScrollProgress();
        initParallax();
        initFaqBehavior();
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();
})();
