
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('login_btn').addEventListener('click', function(event) {
        var userId= document.getElementById('userId').value;
        var userPw = document.getElementById('userPw').value;

        if (userPw === '' || userPw === '') {
            alert('아이디와 비밀번호를 입력해주세요.');
            event.preventDefault(); // 폼 제출을 막음
            return;
        // } else {
        //     window.location.href = 'hyeeun/mainpage';
        }
    });
});

// sns 로그인
document.getElementById("google-login").addEventListener("click", function() {
    window.location.href = `https://accounts.google.com/o/oauth2/auth?client_id=31911528315-rdtqb9sviamqv3c870fbni1sfqg80957.apps.googleusercontent.com&redirect_uri=http://localhost:8098/snslogin/oauth2/code/google&response_type=code&scope=email%20profile`;
});

document.getElementById("kakao-login").addEventListener("click", function() {
    window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=b0b82cb476ddfaa6ad5516b1b59d82ac&redirect_uri=http://localhost:8098/snslogin/oauth2/code/kakao&response_type=code&scope=account_email,profile_nickname`;
});

document.getElementById("naver-login").addEventListener("click", function() {
    window.location.href = `https://nid.naver.com/oauth2.0/authorize?client_id=KeB2I8Od5D0f8tGxtz9h&redirect_uri=http://localhost:8098/snslogin/oauth2/code/naver&response_type=code&scope=name,email`;
});


//카카오 하나만 해보기
// function goToKakaoLogin() {
//     window.location.href = '/snslogin/oauth2/code/kakao';
// }
//
//
// function goToNaverLogin() {
//     window.location.href = 'http://www.naver.com';
// }
//
// function goToGoogleLogin() {
//     window.location.href = 'http://www.google.com';
// }
//
// function goToAppleLogin() {
//     window.location.href = 'http://www.apple.com';
// }
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