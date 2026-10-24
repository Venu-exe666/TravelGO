(function () {
    'use strict';

    var pendingForm = null;
    var pendingSubmitter = null;
    var approvedForms = new WeakSet();

    function roleName() {
        var sidebar = document.querySelector('.admin-sidebar');
        if (sidebar && sidebar.classList.contains('sidebar--admin')) return 'admin';
        if (sidebar && sidebar.classList.contains('sidebar--visa')) return 'visa';
        return 'package';
    }

    function confirmationFor(form) {
        if (form.dataset.confirm === 'false') return null;
        if (form.dataset.confirm) {
            return {
                title: form.dataset.confirmTitle || 'Are you sure?',
                message: form.dataset.confirm,
                label: form.dataset.confirmLabel || 'Yes, continue'
            };
        }

        var action = (form.getAttribute('action') || '').toLowerCase();
        var id = form.id || '';
        var rules = [
            { match: '/delete', message: 'This item will be permanently removed. This action cannot be undone.', label: 'Yes, delete it' },
            { match: '/toggle-status', message: 'This will change the current access or availability status.', label: 'Yes, change status' },
            { match: '/approve', message: 'This record will be approved and moved to its next workflow stage.', label: 'Yes, approve it' },
            { match: '/reject', message: 'This record will be rejected and linked workflow changes may also be applied.', label: 'Yes, reject it' },
            { match: '/issue-tickets', message: 'Ticket and booking details will be released to the customer.', label: 'Yes, send tickets' },
            { match: '/verify-all', message: 'All submitted documents will be marked as verified.', label: 'Yes, verify all' },
            { match: '/process', message: 'Payment will be treated as received and embassy processing will begin.', label: 'Yes, start processing' },
            { match: '/reply', message: 'Your reply will be sent and this inquiry will be marked as resolved.', label: 'Yes, send reply' },
            { match: '/settings/status', message: 'This changes public website availability and may affect every visitor.', label: 'Yes, apply changes' }
        ];

        if (id === 'updateForm') {
            return { title: 'Are you sure?', message: 'The payment status and its linked booking workflow may change.', label: 'Yes, update status' };
        }

        for (var i = 0; i < rules.length; i += 1) {
            if (action.indexOf(rules[i].match) !== -1) {
                return { title: 'Are you sure?', message: rules[i].message, label: rules[i].label };
            }
        }
        return null;
    }

    function createDialog() {
        var overlay = document.createElement('div');
        overlay.className = 'staff-confirm-overlay staff-confirm--' + roleName();
        overlay.hidden = true;
        overlay.innerHTML =
            '<section class="staff-confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="staffConfirmTitle" aria-describedby="staffConfirmMessage">' +
                '<div class="staff-confirm-icon" aria-hidden="true">?</div>' +
                '<div class="staff-confirm-copy">' +
                    '<h3 id="staffConfirmTitle">Are you sure?</h3>' +
                    '<p id="staffConfirmMessage"></p>' +
                '</div>' +
                '<div class="staff-confirm-actions">' +
                    '<button type="button" class="staff-confirm-cancel">No, go back</button>' +
                    '<button type="button" class="staff-confirm-accept">Yes, continue</button>' +
                '</div>' +
            '</section>';
        document.body.appendChild(overlay);

        var cancelButton = overlay.querySelector('.staff-confirm-cancel');
        var acceptButton = overlay.querySelector('.staff-confirm-accept');

        function closeDialog() {
            overlay.hidden = true;
            document.body.classList.remove('staff-confirm-open');
            pendingForm = null;
            pendingSubmitter = null;
        }

        cancelButton.addEventListener('click', closeDialog);
        overlay.addEventListener('click', function (event) {
            if (event.target === overlay) closeDialog();
        });
        acceptButton.addEventListener('click', function () {
            var form = pendingForm;
            var submitter = pendingSubmitter;
            closeDialog();
            if (!form) return;
            approvedForms.add(form);
            if (form.requestSubmit) form.requestSubmit(submitter || undefined);
            else form.submit();
        });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !overlay.hidden) closeDialog();
        });
        return overlay;
    }

    document.addEventListener('DOMContentLoaded', function () {
        var dialog = createDialog();

        document.querySelectorAll('[onsubmit*="confirm("], [onclick*="confirm("]').forEach(function (element) {
            element.removeAttribute(element.hasAttribute('onsubmit') ? 'onsubmit' : 'onclick');
        });

        document.addEventListener('submit', function (event) {
            var form = event.target;
            if (!(form instanceof HTMLFormElement)) return;
            if (approvedForms.has(form)) {
                approvedForms.delete(form);
                return;
            }
            var config = confirmationFor(form);
            if (!config) return;

            event.preventDefault();
            event.stopImmediatePropagation();
            pendingForm = form;
            pendingSubmitter = event.submitter || null;
            dialog.querySelector('#staffConfirmTitle').textContent = config.title;
            dialog.querySelector('#staffConfirmMessage').textContent = config.message;
            dialog.querySelector('.staff-confirm-accept').textContent = config.label;
            dialog.hidden = false;
            document.body.classList.add('staff-confirm-open');
            dialog.querySelector('.staff-confirm-cancel').focus();
        }, true);
    });
}());
