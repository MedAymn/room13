/**
 * timer.js — Countdown timer for Room 13 game pages.
 *
 * Reads initial remaining seconds from #remainingSeconds hidden input.
 * Updates #timerDisplay every second in MM:SS format.
 * Adds class 'danger' to #timerWidget when < 60 seconds.
 * Auto-submits #timeoutForm when timer reaches 0.
 */
(function () {
    'use strict';

    var remainingInput = document.getElementById('remainingSeconds');
    var timerDisplay   = document.getElementById('timerDisplay');
    var timerWidget    = document.getElementById('timerWidget');
    var timeoutForm    = document.getElementById('timeoutForm');

    if (!remainingInput || !timerDisplay) return;

    var remaining = parseInt(remainingInput.value, 10) || 0;
    var submitting = false;
    var intervalId = null;

    function pad(n) {
        return n < 10 ? '0' + n : String(n);
    }

    function formatTime(secs) {
        var m = Math.floor(secs / 60);
        var s = secs % 60;
        return pad(m) + ':' + pad(s);
    }

    function tick() {
        if (submitting) return;

        if (remaining <= 0) {
            timerDisplay.textContent = '00:00';
            if (timerWidget) timerWidget.classList.add('danger');
            clearInterval(intervalId);
            if (timeoutForm && !submitting) {
                submitting = true;
                timeoutForm.submit();
            }
            return;
        }

        remaining -= 1;
        timerDisplay.textContent = formatTime(remaining);

        if (timerWidget) {
            if (remaining < 60) {
                timerWidget.classList.add('danger');
            } else {
                timerWidget.classList.remove('danger');
            }
        }
    }

    // Initialise display immediately, then start ticking
    timerDisplay.textContent = formatTime(remaining);
    if (timerWidget && remaining < 60) {
        timerWidget.classList.add('danger');
    }

    // Pause the timer while a form in the page is being submitted so we
    // don't double-fire the timeout.
    document.addEventListener('submit', function () {
        submitting = true;
        clearInterval(intervalId);
    });

    intervalId = setInterval(tick, 1000);
}());
