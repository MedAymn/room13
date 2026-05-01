/**
 * keypad.js — On-screen PIN keypad for the enter-pin page.
 *
 * Reads pinLength from #pinLength.
 * Renders entered digits in .pb elements inside #pinEntryDisplay.
 * Writes assembled PIN into #pinInput (hidden form field).
 * Submits #pinForm when all digits are entered and confirmed.
 * Also handles keyboard digit input (0–9, Backspace, Enter).
 */
(function () {
    'use strict';

    var pinLengthEl = document.getElementById('pinLength');
    var pinInput    = document.getElementById('pinInput');
    var pinForm     = document.getElementById('pinForm');
    var displayEl   = document.getElementById('pinEntryDisplay');

    if (!pinLengthEl || !pinInput || !pinForm || !displayEl) return;

    var entryBoxes = displayEl.querySelectorAll('.pb');
    var pinLength  = parseInt(pinLengthEl.value, 10) || 3;
    var entered    = [];

    function render() {
        entryBoxes.forEach(function (box, i) {
            if (i < entered.length) {
                box.textContent = entered[i];
                box.classList.add('filled');
                box.classList.remove('empty');
            } else {
                box.textContent = '·';
                box.classList.remove('filled');
                box.classList.add('empty');
            }
        });
    }

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
            if (displayEl) {
                displayEl.classList.remove('shake');
                void displayEl.offsetWidth;
                displayEl.classList.add('shake');
                setTimeout(function () { displayEl.classList.remove('shake'); }, 500);
            }
            return;
        }
        pinInput.value = entered.join('');
        pinForm.submit();
    }

    // On-screen keypad listeners
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

    // Keyboard listeners
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

    render();
}());
