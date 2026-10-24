(function () {
    'use strict';

    var STORAGE_KEY = 'travelgo-theme';
    var root = document.documentElement;

    function savedTheme() {
        try {
            var value = localStorage.getItem(STORAGE_KEY);
            return value === 'light' || value === 'dark' ? value : 'dark';
        } catch (error) {
            return 'dark';
        }
    }

    function iconFor(theme) {
        return theme === 'dark'
            ? '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="4"></circle><path d="M12 2v2m0 16v2M4.93 4.93l1.42 1.42m11.3 11.3 1.42 1.42M2 12h2m16 0h2M4.93 19.07l1.42-1.42m11.3-11.3 1.42-1.42"></path></svg>'
            : '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>';
    }

    function syncButtons(theme) {
        document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
            var nextTheme = theme === 'dark' ? 'light' : 'dark';
            button.innerHTML = iconFor(theme) + '<span>' + (theme === 'dark' ? 'Light mode' : 'Dark mode') + '</span>';
            button.setAttribute('aria-label', 'Switch to ' + nextTheme + ' mode');
            button.setAttribute('title', 'Switch to ' + nextTheme + ' mode');
            button.setAttribute('aria-pressed', theme === 'light' ? 'true' : 'false');
        });
    }

    function applyTheme(theme, persist) {
        root.dataset.theme = theme;
        root.style.colorScheme = theme;
        if (persist) {
            try { localStorage.setItem(STORAGE_KEY, theme); } catch (error) { /* Storage may be disabled. */ }
        }
        syncButtons(theme);
    }

    function createToggle(extraClass) {
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'theme-toggle' + (extraClass ? ' ' + extraClass : '');
        button.dataset.themeToggle = '';
        button.addEventListener('click', function () {
            applyTheme(root.dataset.theme === 'light' ? 'dark' : 'light', true);
        });
        return button;
    }

    applyTheme(savedTheme(), false);

    document.addEventListener('DOMContentLoaded', function () {
        if (document.querySelector('[data-theme-toggle]')) {
            syncButtons(root.dataset.theme);
            return;
        }

        var staffFooter = document.querySelector('.admin-sidebar .sidebar-footer');
        var customerActions = document.querySelector('.cust-user-badge-row');
        var customerTopbar = document.querySelector('.cust-topbar');
        var toggle;

        if (staffFooter) {
            toggle = createToggle('theme-toggle--sidebar');
            staffFooter.insertBefore(toggle, staffFooter.querySelector('form'));
        } else if (customerActions) {
            toggle = createToggle('theme-toggle--topbar');
            customerActions.insertBefore(toggle, customerActions.firstChild);
        } else if (customerTopbar) {
            toggle = createToggle('theme-toggle--topbar');
            customerTopbar.insertBefore(toggle, customerTopbar.lastElementChild);
        } else {
            toggle = createToggle('theme-toggle--floating');
            document.body.appendChild(toggle);
        }

        syncButtons(root.dataset.theme);
    });
}());
