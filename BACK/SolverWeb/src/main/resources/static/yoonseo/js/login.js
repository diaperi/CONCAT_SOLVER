
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('login_btn').addEventListener('click', function(event) {
        var userId= document.getElementById('userId').value;
        var userPw = document.getElementById('userPw').value;

        if (userPw === '' || userPw === '') {
            alert('아이디와 비밀번호를 입력해주세요.');
            event.preventDefault(); // 폼 제출을 막음
        // } else {
        //     window.location.href = 'hyeeun/mainpage';
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
//카카오간편로그인 javascript키
//ee8f3029ae0cc583e65ab359f007f4c8
//
// Kakao.init('ee8f3029ae0cc583e65ab359f007f4c8');
// function goTokakaoLogin() {
//     Kakao.Auth.authorize({
//       redirectUri: 'http://192.168.45.57:5501/yoonseo/login.html', /*여기가 메인페이지.html로 연결되야할것 같은데 일단 임시로 login으로*/
//     });
//   }
//
//
//   displayToken()
//   function displayToken() {
//     var token = getCookie('authorize-access-token');
//
//     if(token) {
//       Kakao.Auth.setAccessToken(token);
//       Kakao.Auth.getStatusInfo()
//         .then(function(res) {
//           if (res.status === 'connected') {
//             document.getElementById('token-result').innerText
//               = 'login success, token: ' + Kakao.Auth.getAccessToken();
//           }
//         })
//         .catch(function(err) {
//           Kakao.Auth.setAccessToken(null);
//         });
//     }
//   }
//
//   function getCookie(name) {
//     var parts = document.cookie.split(name + '=');
//     if (parts.length === 2) { return parts[1].split(';')[0]; }
//   }