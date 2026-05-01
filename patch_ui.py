import os
import re

def process_game():
    with open('extracted_ui/game.html', 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Add thymeleaf
    content = content.replace('<html lang="en">', '<html xmlns:th="http://www.thymeleaf.org" lang="en">')
    
    # 1. Navbar
    content = content.replace(
        '<span><a class="exit" href="menu.html" onclick="return confirm(\'Leave the room? Your progress will be lost.\');">← Leave the room</a></span>',
        '<span><a class="exit" th:href="@{/menu}" onclick="return confirm(\'Leave the room? Your progress will be lost.\');">← Leave the room</a></span>'
    )
    
    # 2. Agent
    content = content.replace(
        '<div class="v">Kai <span style="color:var(--ink-3)">// kai_07</span></div>',
        '<div class="v"><span th:text="${player.username}">Kai</span> <span style="color:var(--ink-3)">// kai_07</span></div>'
    )
    
    # 3. Level
    content = content.replace(
        '<div class="v"><span class="chip" id="levelChip">Operative</span></div>',
        '<div class="v"><span class="chip" id="levelChip" th:text="${level}">Operative</span></div>'
    )
    
    # 4. Attempts -> we'll hide attempts for now if they aren't explicitly tracked, or keep static. Original just had Riddle [1/3]
    content = content.replace(
        '<div class="meta-block">\n        <div class="k">Attempts left</div>',
        '<div class="meta-block" style="display:none;">\n        <div class="k">Attempts left</div>'
    )
    
    # 5. Best score
    content = content.replace(
        '<div class="v big">2,840</div>',
        '<div class="v big" th:text="${player.bestScore}">2,840</div>'
    )
    
    # 6. Current score
    content = content.replace(
        '<div class="v big" style="color:var(--accent)" id="liveScore">1,210</div>',
        '<div class="v big" style="color:var(--accent)" id="liveScore" th:text="${currentScore}">1,210</div>'
    )
    content = content.replace(
        '<div style="color:var(--ink-3); font-size:12px; margin-top:6px;">×2 multiplier · 1 hint used</div>',
        '<div style="color:var(--ink-3); font-size:12px; margin-top:6px;"><span th:if="${lastSavedAt != null}" th:text="\'Saved at \' + ${lastSavedAt}"></span></div>'
    )
    
    # 7. Save btn
    content = content.replace(
        '<button class="r13-r13-btn" id="saveBtn" style="width: 100%; justify-content: center; color: rgb(0, 0, 0)">Save session</button>',
        '<form th:action="@{/game/save}" method="post"><button type="submit" class="r13-btn" id="saveBtn" style="width: 100%; justify-content: center;">Save session</button></form>'
    )
    # Fix the typo r13-r13-btn -> r13-btn throughout the file
    content = content.replace('r13-r13-btn', 'r13-btn')
    
    # 8. Riddle Title
    content = content.replace(
        '<div class="doc-head">\n        <span>Document 0214 · Wing C</span>\n        <span id="docCode">Operative · Riddle A</span>\n      </div>',
        '<div class="doc-head">\n        <span>Riddle <span th:text="${riddleNumber}">1</span> of <span th:text="${riddleCount}">3</span></span>\n        <span id="docCode" th:text="${level} + \' · Riddle\'">Operative · Riddle A</span>\n      </div>'
    )
    
    # 9. Riddle Question Content
    content = content.replace(
        '<p>The cleaner came on the <b>seventh</b> day, as he always did, and emptied bin <b>two</b> without lifting his eyes.</p>\n        <p>By then I had counted <b>thirty-one</b> footsteps in the corridor — the same as last Tuesday, the same as the Tuesday before.</p>\n        <p>He left his cart by the elevator, room <b>nine</b>, where the panel hums when the cameras blink.</p>\n        <p>If you are reading this, you already know what time the lights cut out.</p>\n\n        <div class="exhibit" aria-label="Photograph found with the document">\n          <div class="ph" role="img" aria-label="Three combination dials"></div>\n          <div class="cap">\n            <span class="lab">Photograph · attached</span>\n            <p style="margin:0;">Three dials. Visible row reads <b>0&nbsp;1&nbsp;2</b>. The dials were stiff — they hadn\'t been moved since the cleaner left. <i>Don\'t trust what\'s on top.</i></p>\n          </div>\n        </div>',
        '<p th:text="${riddle.question}">Question goes here...</p>\n<div th:if="${errorMessage != null}" style="color:var(--warn); margin-bottom: 12px; font-size: 14px;" th:text="${errorMessage}"></div>\n<form th:if="${!autoSolved}" th:action="@{/game/answer}" method="post" style="display:flex; gap:10px; margin-top:20px;">\n<input type="text" name="answer" placeholder="Enter answer..." style="flex:1; background:transparent; border:1px solid var(--rule); color:var(--ink); padding:8px 12px;" required autofocus/>\n<button type="submit" class="r13-btn primary">Submit</button>\n</form>'
    )
    
    # 10. Hint Box
    content = content.replace(
        '<div class="feedback hint" id="hintBox" style="display:none;">\n        <span class="lab">Hint received · −30s</span>\n        <p>Numbers spelled out are not always written. Count the <i>spelled</i> numerals, in order of appearance — that is your sequence. Ignore digits hidden in metadata.</p>\n      </div>',
        '<div class="feedback hint" id="hintBox" th:if="${hint != null}">\n        <span class="lab">Hint received</span>\n        <p th:text="${hint}">Hint goes here...</p>\n      </div>'
    )
    
    # 11. Solve Box
    content = content.replace(
        '<div class="feedback solve" id="solveBox" style="display:none;">\n        <span class="lab">Auto-solved · −90s · ½ score</span>\n        <p>The PIN is <b>7 · 2 · 3 · 9</b>. The numerals are spelled in narrative order: <i>seventh, two, thirty-one (3+1), nine</i>.</p>\n      </div>',
        '<div class="feedback solve" id="solveBox" th:if="${autoSolved}">\n        <span class="lab">Auto-solved</span>\n        <p>The answer is <b th:text="${riddle.answer}">X</b>. <span th:text="${riddle.explanation}">...</span></p>\n      </div>'
    )
    
    # 12. Actions
    content = content.replace(
        '<button class="r13-btn" id="hintBtn" style="color: rgb(0, 0, 0)">Ask for a hint <span class="pen">−30s</span></button>\n        <button class="r13-btn" id="solveBtn" style="color: rgb(0, 0, 0)">Auto-solve <span class="pen">−90s · ½ score</span></button>\n        <a class="r13-btn primary" href="enter-pin.html?level=MEDIUM" style="margin-left:auto;">Enter the code →</a>',
        '<form th:if="${!autoSolved}" th:action="@{/game/hint}" method="post">\n            <button type="submit" class="r13-btn" th:disabled="${hintUsed}">Ask for a hint <span class="pen">−30s</span></button>\n        </form>\n        <form th:if="${!autoSolved}" th:action="@{/game/solve}" method="post">\n            <button type="submit" class="r13-btn">Auto-solve <span class="pen">−90s · ½ score</span></button>\n        </form>\n        <form th:if="${autoSolved}" th:action="@{/game/next}" method="post" style="margin-left:auto;">\n            <button type="submit" class="r13-btn primary">Next Riddle →</button>\n        </form>'
    )
    
    # 13. Code / PIN display
    content = content.replace(
        '<div class="pin-r13-row" id="pinRow">\n          <div class="pin-box">·</div>\n          <div class="pin-box">·</div>\n          <div class="pin-box">·</div>\n          <div class="pin-box">·</div>\n        </div>',
        '<div class="pin-row" id="pinRow" th:style="\'display:grid; gap:6px; margin-top:8px; grid-template-columns: repeat(\' + ${revealedDigitsDisplay.size()} + \', 1fr);\'">\n          <div class="pin-box" th:each="d : ${revealedDigitsDisplay}" th:text="${d}">·</div>\n        </div>'
    )
    
    # 14. Timer integration
    content = content.replace(
        '<div class="timer" id="timer">03:42</div>',
        '<div class="timer" id="timerDisplay">00:00</div>'
    )
    
    # Hide JS tweaks and add original hidden timer setup
    content = content.replace(
        '<div class="tweaks">',
        '<form id="timeoutForm" th:action="@{/game/timeout}" method="post" style="display:none"></form>\n<input type="hidden" id="remainingSeconds" th:value="${remainingSeconds}"/>\n<div class="tweaks" style="display:none;">'
    )
    
    # Replace the embedded script with a cleaner script that uses timer.js
    script_regex = re.compile(r'<script>.*?</script>', re.DOTALL)
    content = script_regex.sub('<script th:src="@{/static/js/timer.js}"></script>', content)

    with open('src/main/webapp/WEB-INF/templates/game.html', 'w', encoding='utf-8') as f:
        f.write(content)


def process_enter_pin():
    with open('extracted_ui/enter-pin.html', 'r', encoding='utf-8') as f:
        content = f.read()
    
    content = content.replace('<html lang="en">', '<html xmlns:th="http://www.thymeleaf.org" lang="en">')
    content = content.replace('r13-r13-row', 'pin-row')
    content = content.replace('r13-r13-btn', 'r13-btn')
    
    # Update Form
    content = content.replace(
        '<form class="lock-panel" id="pinForm" method="post" action="/auth/submit-pin">',
        '<form class="lock-panel" id="pinForm" method="post" th:action="@{/game/submit-pin}">'
    )
    
    # Display error
    content = content.replace(
        '<h2>Lock mechanism</h2>',
        '<h2>Lock mechanism</h2>\n        <div th:if="${pinError != null}" style="color:var(--warn); margin-bottom: 12px; font-size: 14px;" th:text="${pinError}"></div>'
    )
    
    # Code display
    content = content.replace(
        '<div class="pin-row" id="pinRow">\n          <div class="pin-box">7</div>\n          <div class="pin-box">2</div>\n          <div class="pin-box">3</div>\n          <div class="pin-box">?</div>\n        </div>',
        '<div class="pin-row" id="pinRow" th:style="\'display:grid; gap:6px; margin-top:8px; grid-template-columns: repeat(\' + ${pinLength} + \', 1fr);\'">\n          <div class="pin-box" th:each="d : ${revealedDigitsDisplay}" th:text="${d}">·</div>\n        </div>'
    )
    
    content = content.replace(
        '<input type="password" id="pinInput" placeholder="Enter code" class="pin-input" readonly />',
        '<input type="text" name="pin" id="pinInput" placeholder="Enter code" class="pin-input" required readonly />'
    )
    
    # Timer integration
    content = content.replace(
        '<div class="timer" id="timer">01:14</div>',
        '<div class="timer" id="timerDisplay">00:00</div>\n<form id="timeoutForm" th:action="@{/game/timeout}" method="post" style="display:none"></form>\n<input type="hidden" id="remainingSeconds" th:value="${remainingSeconds}"/>'
    )
    
    content = content.replace(
        '<span class="chip" id="levelChip">Operative</span>',
        '<span class="chip" id="levelChip" th:text="${level}">Operative</span>'
    )
    
    # Timer logic inside JS
    content = content.replace(
        'let remaining = 74, total = 240, h = null;',
        ''
    )
    content = content.replace(
        'function fmt(s) { return String(Math.floor(s/60)).padStart(2,\'0\') + \':\' + String(s%60).padStart(2,\'0\'); }',
        ''
    )
    content = content.replace(
        'function paint() {\n    const t = document.getElementById(\'timer\'), b = document.getElementById(\'timerBar\');\n    t.textContent = fmt(remaining);\n    t.classList.toggle(\'warn\', remaining < 60);\n    b.style.width = Math.max(0, (remaining/total)*100) + \'%\';\n    b.style.background = remaining < 60 ? \'var(--warn)\' : \'var(--accent)\';\n  }',
        ''
    )
    content = content.replace(
        'function start() { if (h) clearInterval(h); h = setInterval(() => { if (remaining<=0) {clearInterval(h);return;} remaining--; paint(); }, 1000); }',
        ''
    )
    content = content.replace(
        'start();',
        ''
    )

    with open('src/main/webapp/WEB-INF/templates/enter-pin.html', 'w', encoding='utf-8') as f:
        f.write(content)

def process_win():
    with open('extracted_ui/win.html', 'r', encoding='utf-8') as f:
        content = f.read()
    
    content = content.replace('<html lang="en">', '<html xmlns:th="http://www.thymeleaf.org" lang="en">')
    content = content.replace('r13-r13-row', 'pin-row')
    content = content.replace('r13-r13-btn', 'r13-btn')
    
    content = content.replace(
        '<div class="v"><span class="chip">Operative</span></div>',
        '<div class="v"><span class="chip" th:text="${finalLevel}">Operative</span></div>'
    )
    
    content = content.replace(
        '<div class="v big" style="color:var(--good)">2,840</div>',
        '<div class="v big" style="color:var(--good)" th:text="${finalScore}">2,840</div>'
    )
    
    content = content.replace(
        '<a class="r13-btn primary" href="menu.html">Return to desk →</a>',
        '<a class="r13-btn primary" th:href="@{/menu}">Return to desk →</a>'
    )

    with open('src/main/webapp/WEB-INF/templates/win.html', 'w', encoding='utf-8') as f:
        f.write(content)


def process_gameover():
    with open('extracted_ui/game-over.html', 'r', encoding='utf-8') as f:
        content = f.read()
    
    content = content.replace('<html lang="en">', '<html xmlns:th="http://www.thymeleaf.org" lang="en">')
    content = content.replace('r13-r13-row', 'pin-row')
    content = content.replace('r13-r13-btn', 'r13-btn')
    
    content = content.replace(
        '<a class="r13-btn primary" href="menu.html">Return to desk →</a>',
        '<a class="r13-btn primary" th:href="@{/menu}">Return to desk →</a>'
    )

    with open('src/main/webapp/WEB-INF/templates/gameover.html', 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == "__main__":
    process_game()
    process_enter_pin()
    process_win()
    process_gameover()
    print("Done")
