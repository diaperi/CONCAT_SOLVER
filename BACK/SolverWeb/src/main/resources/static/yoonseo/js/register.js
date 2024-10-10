let isEmailVerified = false;

function goToLogin() {
    window.location.href = '/user/login';
}

function goToRegister() {
    window.location.href = '/user/sign';
}

const checkDuplicateId = () => {
    const id = document.getElementById('idInput').value.trim();
    const checkResult = document.getElementById('check-result');

    if (id === '') {
        alert('아이디를 입력해주세요.');
        return;
    }

    $.ajax({
        type: "POST",
        url: "/user/id-check",
        data: { id: id },
        success: function(res) {
            if (res === "ok") {
                checkResult.style.color = "green";
                checkResult.innerHTML = "사용 가능한 아이디입니다.";
            } else if (res === "exists") {
                checkResult.style.color = "red";
                checkResult.innerHTML = "이미 사용 중인 아이디입니다.";
            } else {
                checkResult.style.color = "orange";
                checkResult.innerHTML = "알 수 없는 오류가 발생했습니다.";
            }
        },
        error: function() {
            checkResult.style.color = "red";
            checkResult.innerHTML = "아이디 중복 체크 중 오류가 발생했습니다.";
        }
    });
};

function checkInputs() {
    const name = document.getElementById('nameInput').value.trim();
    const id = document.getElementById('idInput').value.trim();
    const pw = document.getElementById('pwInput').value.trim();
    const pwCheck = document.getElementById('pwCheckInput').value.trim();
    const email = document.getElementById('emailInput').value.trim();

    if (name === '' || id === '' || pw === '' || pwCheck === '' || email === '') {
        alert('모든 항목을 작성해주세요.');
        return false;
    }

    if (pw.length < 6 || pw.length > 20) {
        alert('비밀번호는 6~20자 이어야 합니다.');
        return false;
    }

    if (pw !== pwCheck) {
        alert('비밀번호가 일치하지 않습니다.');
        return false;
    }

    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    if (!emailPattern.test(email)) {
        alert('유효한 이메일 주소를 입력해주세요.');
        return false;
    }

    // 이메일 인증 상태
    if (!isEmailVerified) {
        alert('이메일 인증을 먼저 받아야 합니다.');
        return false;
    }

    return true;
}

// 인증 메일 발송
function sendVerifyEmail() {
    const email = document.getElementById('emailInput').value.trim();

    if (!email) {
        alert('이메일을 입력해주세요.');
        return;
    }

    fetch('/user/email/send?email=' + encodeURIComponent(email), {
        method: 'POST'
    }).then(response => response.text()).then(result => {
        alert(result);
    }).catch(error => {
        console.error('메일 발송 오류:', error);
    });
}

// 인증번호 검증
function verifyEmailCode() {
    const email = document.getElementById('emailInput').value.trim();
    const code = document.getElementById('emailCheckInput').value.trim();
    const emailCheckResult = document.getElementById('emailCheckResult');

    if (!code) return;

    fetch('/user/email/verify?email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code), {
        method: 'POST'
    }).then(response => response.json()).then(isValid => {
        isEmailVerified = isValid;
        const registerUpButton = document.getElementById('registerup');
        registerUpButton.disabled = !isValid;

        if (!isValid) {
            emailCheckResult.style.color = "red";
            emailCheckResult.innerHTML = "인증번호가 일치하지 않습니다.";
        } else {
            emailCheckResult.style.color = "green";
            emailCheckResult.innerHTML = "인증번호가 일치합니다.";
        }
    }).catch(error => {
        console.error('인증번호 확인 오류:', error);
        emailCheckResult.style.color = "red";
        emailCheckResult.innerHTML = "인증번호 확인 중 오류가 발생했습니다.";
    });
}

// 인증번호 입력 시 검증
document.getElementById('emailCheckInput').addEventListener('input', verifyEmailCode);