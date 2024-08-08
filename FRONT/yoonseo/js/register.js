// JavaScript 코드
// document.getElementById('nextButton').addEventListener('click', function() {
//     window.location.href = 'sign2.html'; // 이동할 HTML 파일 경로
// });

// JavaScript 코드
// document.addEventListener('DOMContentLoaded', function() {
//     document.getElementById('nextButton').addEventListener('click', function() {
//         window.location.href = 'sign.html'; // 이동할 HTML 파일 경로
//     });
// });
//

function goToLogin() {
    // login.html로 이동합니다.
    window.location.href = 'login.html';
}
function goToSign() {
    // register.html로 이동
    window.location.href = 'sign.html';
}


// 아이디 중복 확인 (여기서는 간단히 아이디가 'test'일 경우 중복으로 간주)
function checkDuplicate() {
    var id = document.getElementById('idInput').value.trim();

    // 여기서는 간단히 아이디가 'test'인 경우만 중복으로 간주
    if (id === 'test') {
        document.getElementById('idValidation').innerText = '이미 사용 중인 아이디입니다.';
    } else {
        document.getElementById('idValidation').innerText = ' 사용 가능한 아이디입니다.';
    }
}
function checkInputs() {
    var name = document.getElementById('nameInput').value.trim();
    var id = document.getElementById('idInput').value.trim();
    var pw = document.getElementById('pwInput').value.trim();
    var pwCheck = document.getElementById('pwCheckInput').value.trim();
    var email = document.getElementById('emailInput').value.trim();

    //항목 작성시
    if (name === '' || id === '' || pw === '' || pwCheck === '' || email === '') {
        alert('모든 항목을 작성해주세요.'); // **이걸 띄울건지는 미정
        return;
    }
     
    // 아이디 중복 확인 (여기서는 간단히 아이디가 'test'일 경우 중복으로 간주)

     // 비밀번호 길이 확인
     if (pw.length < 6 || pw.length > 20) {
        alert('비밀번호는 6~20자 이어야 합니다.');
        return;
    }

    // 비밀번호 확인
    if (pw !== pwCheck) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    // 이메일 형식 검사 (간단한 형식 검사)
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    if (!emailPattern.test(email)) {
        alert('유효한 이메일 주소를 입력해주세요.');
        return;
    }
    window.location.href = 'sign.html'; }