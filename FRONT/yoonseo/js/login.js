// document.addEventListener('DOMContentLoaded', function() {
//     document.getElementById('btn').addEventListener('click', function() {
//         window.location.href = 'register.html'; // 메인페이지와 연결해야함. 일단 임시로 register.html과 연결함.
//     });
// });
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('btn').addEventListener('click', function(event) {
        var username = document.getElementById('username').value;
        var password = document.getElementById('password').value;

        if (username === '' || password === '') {
            alert('아이디와 비밀번호를 입력해주세요.');
            event.preventDefault(); // 폼 제출을 막음
        } else {
            window.location.href = 'register.html'; // 메인 페이지와 연결해야 함. 일단 임시로 register.html과 연결함.
        }
    });
});


//카카오 하나만 해보기
function goToKakaoLogin() {
    window.location.href = 'http://www.kakao.com';
}


function goToNaverLogin() {
    window.location.href = 'http://www.naver.com';
}

function goToGoogleLogin() {
    window.location.href = 'http://www.google.com';
}

function goToAppleLogin() {
    window.location.href = 'http://www.apple.com';
}
// function goToNaverLogin() {
//     // 네이버 로그인 페이지로 이동하는 코드 작성
//     console.log('');
// }