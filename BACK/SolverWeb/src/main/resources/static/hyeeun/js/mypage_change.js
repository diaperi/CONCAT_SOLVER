document.getElementById('email').addEventListener('input', function() {
    var email = document.getElementById('email').value;
    var emailCheckIcon = document.getElementById('email_check_icon');
    var emailCheckMessage = document.getElementById('email_check_message');
    
    // AJAX를 사용하여 서버에 이메일 중복 검사 요청을 보냅니다.
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "js/check_email.php", true);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                if (response.exists) {
                    emailCheckIcon.style.visibility = 'hidden'; // 이메일이 중복이면 체크박스 아이콘 숨김
                    emailCheckMessage.style.display = 'block'; // 경고 메시지 표시
                } else {
                    emailCheckIcon.style.borderColor = 'limegreen'; // 이메일이 중복이 아니면 네모의 색깔을 연두색으로 설정
                    emailCheckIcon.style.backgroundColor = 'limegreen'; // 배경색을 연두색으로 설정
                    emailCheckIcon.style.visibility = 'visible'; // 체크박스 아이콘 표시
                    emailCheckMessage.style.display = 'none'; // 경고 메시지 숨김
                }
            }
        }
    };
    xhr.send(JSON.stringify({ email: email }));
});

document.getElementById('confirm_password').addEventListener('input', function() {
    var password = document.getElementById('password').value;
    var confirmPassword = document.getElementById('confirm_password').value;
    var passwordCheckIcon = document.getElementById('password_check_icon');
    var passwordCheckMessage = document.getElementById('password_check_message');
    
    if (password === confirmPassword) {
        passwordCheckIcon.style.borderColor = 'limegreen'; // 비밀번호가 일치하면 네모의 색깔을 연두색으로 설정
        passwordCheckIcon.style.backgroundColor = 'limegreen'; // 배경색을 연두색으로 설정
        passwordCheckIcon.style.visibility = 'visible'; // 체크박스 아이콘 표시
        passwordCheckMessage.style.display = 'none'; // 경고 메시지 숨김
    } else {
        passwordCheckIcon.style.visibility = 'hidden'; // 비밀번호가 일치하지 않으면 체크박스 아이콘 숨김
        passwordCheckMessage.style.display = 'block'; // 경고 메시지 표시
    }
});

function cancelUpdate() {
    // 모든 입력 필드를 초기화
    document.getElementById('mypage_change_updateForm').reset();
    var emailCheckIcon = document.getElementById('email_check_icon');
    var emailCheckMessage = document.getElementById('email_check_message');
    var passwordCheckIcon = document.getElementById('password_check_icon');
    var passwordCheckMessage = document.getElementById('password_check_message');
    emailCheckIcon.style.visibility = 'hidden'; // 초기화 시 체크박스 아이콘 숨김
    emailCheckMessage.style.display = 'none'; // 경고 메시지 숨김
    passwordCheckIcon.style.visibility = 'hidden'; // 초기화 시 체크박스 아이콘 숨김
    passwordCheckMessage.style.display = 'none'; // 경고 메시지 숨김
}

function saveUpdate() {
    // 데이터 저장 로직 구현
    alert('회원정보가 저장되었습니다.');
}

function openConfirmationPopup() {
    var popup = document.getElementById("confirmationPopup");
    popup.style.display = "flex"; // 팝업 창을 보이게 설정
}

function closeConfirmationPopup() {
    var popup = document.getElementById("confirmationPopup");
    popup.style.display = "none"; // 팝업 창을 숨김
}

function confirmDelete() {
    alert("회원 탈퇴가 완료되었습니다.");
    // 실제 회원 탈퇴 처리 로직을 여기에 추가할 수 있습니다.
    closeConfirmationPopup(); // 팝업을 닫습니다.
}









