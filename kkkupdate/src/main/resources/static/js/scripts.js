// Add any client-side JavaScript functionality here
document.addEventListener('DOMContentLoaded', function () {
    // Проверка загрузки Bootstrap
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap not loaded!');
        return; // Прекращаем выполнение если Bootstrap не загружен
    }

    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Handle form submissions with fetch API
    document.querySelectorAll('form[data-ajax="true"]').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            const formData = new FormData(this);

            // Добавляем CSRF-токен если он есть
            const csrfToken = document.querySelector('meta[name="_csrf"]');
            const headers = {};
            if (csrfToken) {
                headers['X-CSRF-TOKEN'] = csrfToken.content;
            }

            fetch(this.action, {
                method: this.method,
                body: formData,
                headers: headers
            }).then(response => {
                if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    return response.json();
                }
            }).then(data => {
                if (data && data.success) {
                    if (data.redirect) {
                        window.location.href = data.redirect;
                    }
                }
            }).catch(error => {
                console.error('Error:', error);
                // Отображаем ошибку пользователю
                alert('Request failed. Please try again.');
            });
        });
    });
});