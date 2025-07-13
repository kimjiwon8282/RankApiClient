// --- 이메일 중복 확인 버튼 ---
document.getElementById("checkEmailBtn").addEventListener("click", async function () {
    const email = document.getElementById("emailInput").value.trim();
    const msgBox = document.getElementById("emailCheckMsg");
    msgBox.style.color = "blue";
    msgBox.textContent = "";
    if (!email) {
        msgBox.style.color = "red";
        msgBox.textContent = "이메일을 입력하세요.";
        return;
    }
    try {
        const res = await fetch(`/api/user/email-exists?email=${encodeURIComponent(email)}`);
        const data = await res.json();
        if (data.exists) {
            msgBox.style.color = "red";
            msgBox.textContent = "이미 사용 중인 이메일입니다.";
            document.getElementById("emailInput").focus();
        } else {
            msgBox.style.color = "green";
            msgBox.textContent = "사용 가능한 이메일입니다!";
        }
    } catch (err) {
        msgBox.style.color = "red";
        msgBox.textContent = "서버 오류. 다시 시도하세요.";
    }
});

// --- 인증코드 발송 버튼 ---
document.getElementById("sendAuthCodeBtn").addEventListener("click", async function () {
    const email = document.getElementById("emailInput").value.trim();
    const msg = document.getElementById("authCodeMsg");
    msg.style.color = "green";
    msg.textContent = "";
    if (!email) {
        msg.style.color = "red";
        msg.textContent = "이메일을 먼저 입력하세요.";
        return;
    }
    try {
        const res = await fetch(`/api/user/send-auth-code`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: "email=" + encodeURIComponent(email)
        });
        const text = await res.text();
        if (res.ok) {
            msg.style.color = "green";
            msg.textContent = "이메일로 인증코드가 전송되었습니다!";
        } else {
            msg.style.color = "red";
            msg.textContent = "발송 실패: " + text;
        }
    } catch (err) {
        msg.style.color = "red";
        msg.textContent = "서버 오류. 다시 시도하세요.";
    }
});

// --- 인증코드 검증 버튼 ---
document.getElementById("verifyAuthCodeBtn").addEventListener("click", async function () {
    const email = document.getElementById("emailInput").value.trim();
    const code = document.getElementById("authCodeInput").value.trim();
    const msg = document.getElementById("authCodeMsg");
    msg.style.color = "green";
    msg.textContent = "";
    if (!email || !code) {
        msg.style.color = "red";
        msg.textContent = "이메일과 인증코드를 모두 입력하세요.";
        return;
    }
    try {
        const res = await fetch(`/api/user/verify-auth-code`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, code })
        });
        const text = await res.text();
        if (res.ok) {
            msg.style.color = "green";
            msg.textContent = "이메일 인증 성공!";
        } else {
            msg.style.color = "red";
            msg.textContent = "실패: " + text;
        }
    } catch (err) {
        msg.style.color = "red";
        msg.textContent = "서버 오류. 다시 시도하세요.";
    }
});

// --- 회원가입 폼 제출 ---
document.getElementById("signupForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    const email = document.getElementById("emailInput").value.trim();
    const nickname = document.getElementById("nicknameInput").value.trim();
    const password = document.getElementById("passwordInput").value;
    const msgBox = document.getElementById("emailCheckMsg");
    msgBox.style.color = "blue";
    msgBox.textContent = "";

    if (!email || !nickname || !password) {
        msgBox.style.color = "red";
        msgBox.textContent = "모든 항목을 입력하세요.";
        return;
    }

    try {
        const res = await fetch("/signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, nickname, password })
        });

        if (res.ok) {
            alert("회원가입이 완료되었습니다! 로그인 해주세요.");
            window.location.href = "/login";
        } else {
            const text = await res.text();
            msgBox.style.color = "red";
            msgBox.textContent = "회원가입 실패: " + text;
        }
    } catch (err) {
        msgBox.style.color = "red";
        msgBox.textContent = "서버 오류. 다시 시도하세요.";
    }
});
