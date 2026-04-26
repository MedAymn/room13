/**
 * keypad.js — On-screen PIN keypad for the enter-pin page.
 *
 * Reads pinLength from #pinLength.
 * Renders entered digits in .entry-box elements.
 * Writes assembled PIN into #pinInput (hidden form field).
 * Submits #pinForm when all digits are entered and confirmed.
 * Also handles keyboard digit input (0–9, Backspace, Enter).
 */
(function () {
    'use strict';

    var pinLengthEl = document.getElementById('pinLength');
    var pinInput    = document.getElementById('pinInput');
    var pinForm     = document.getElementById('pinForm');
    var entryBoxes  = document.querySelectorAll('.entry-box');

    if (!pinLengthEl || !pinInput || !pinForm) return;

    var pinLength = parseInt(pinLengthEl.value, 10) || 3;
    var entered   = [];  // array of digit strings

    // ── Rendering ──────────────────────────────────────────────────────────

    function render() {
        entryBoxes.forEach(function (box, i) {
            if (i < entered.length) {
                box.textContent = entered[i];
                box.classList.add('filled');
            } else {
                box.textContent = '_';
                box.classList.remove('filled');
            }
        });
    }

    // ── Actions ────────────────────────────────────────────────────────────

    function addDigit(d) {
        if (entered.length >= pinLength) return;
        entered.push(String(d));
        render();
    }

    function backspace() {
        if (entered.length === 0) return;
        entered.pop();
        render();
    }

    function confirm() {
        if (entered.length !== pinLength) {
            // Visual shake
            var display = document.querySelector('.pin-entry-display');
            if (display) {
                display.style.transition = 'transform 0.1s';
                display.style.transform  = 'translateX(6px)';
                setTimeout(function () { display.style.transform = 'translateX(-6px)'; }, 100);
                setTimeout(function () { display.style.transform = 'translateX(0)'; }, 200);
            }
            return;
        }
        pinInput.value = entered.join('');
        pinForm.submit();
    }

    // ── On-screen keypad listeners ─────────────────────────────────────────

    var keys = document.querySelectorAll('.key[data-digit]');
    keys.forEach(function (key) {
        key.addEventListener('click', function () {
            addDigit(key.getAttribute('data-digit'));
        });
    });

    var backspaceBtn = document.getElementById('keyBackspace');
    if (backspaceBtn) {
        backspaceBtn.addEventListener('click', function () { backspace(); });
    }

    var confirmBtn = document.getElementById('keyConfirm');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function () { confirm(); });
    }

    // ── Keyboard listeners ────────────────────────────────────────────────

    document.addEventListener('keydown', function (e) {
        if (e.key >= '0' && e.key <= '9') {
            addDigit(e.key);
        } else if (e.key === 'Backspace') {
            e.preventDefault();
            backspace();
        } else if (e.key === 'Enter') {
            e.preventDefault();
            confirm();
        }
    });

    // ── Initial render ────────────────────────────────────────────────────
    render();

}());
