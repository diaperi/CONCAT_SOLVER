// document.addEventListener('DOMContentLoaded', function() {
//     document.getElementById('btn').addEventListener('click', function() {
//         window.location.href = 'register.html'; // 메인페이지와 연결해야함. 일단 임시로 register.html과 연결함.
//     });
// });
// document.addEventListener('DOMContentLoaded', function() {
//     document.getElementById('btn').addEventListener('click', function(event) {
//         var username = document.getElementById('username').value;
//         var password = document.getElementById('password').value;
//
//         if (username === '' || password === '') {
//             alert('아이디와 비밀번호를 입력해주세요.');
//             event.preventDefault(); // 폼 제출을 막음
//         } else {
//             window.location.href = 'register.html'; // 메인 페이지와 연결해야 함. 일단 임시로 register.html과 연결함.
//         }
//     });
// });


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

// Kakao.init('ee8f3029ae0cc583e65ab359f007f4c8');
// function goTokakaoLogin() {
//     Kakao.Auth.authorize({
//       redirectUri: 'http://192.168.45.57:5501/hyeeun/mainpage.html', /*여기가 메인페이지.html로 연결되야할것 같은데 일단 임시로 login으로*/
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
// document.getElementById("loginForm").addEventListener("submit", function(event) {
//     event.preventDefault();
//     const userId = document.getElementById("idInput").value.trim();
//     const userPw = document.getElementById("pwInput").value.trim();
//
//     if (!userId || !userPw) {
//         return;
//     }
//
//     $.ajax({
//         type: "POST",
//         url: "/user/login",
//         data: {
//             "userId": userId,
//             "userPw": userPw
//         },
//         success: function(res) {
//             if (res === "ok") {
//                 window.location.href = "/user/find"; // 로그인 성공 시 이동할 페이지
//             } else {
//
//             }
//         },
//         error: function(err) {
//             console.log("에러 발생", err);
//         }
//     });
// });
// document.getElementById("loginForm").addEventListener("submit", function(event) {
//     event.preventDefault();
//     const username = document.getElementById("username").value.trim();
//     const password = document.getElementById("password").value.trim();
//
//     if (!username || !password) {
//         return;
//     }
//
//     $.ajax({
//         type: "POST",
//         url: "/user/login",
//         data: {
//             "username": username,
//             "password": password
//         },
//         success: function(res) {
//             if (res === "ok") {
//                 window.location.href = "/user/find"; // 로그인 성공 시 이동할 페이지
//             } else {
//
//             }
//         }
//     });
// });
document.getElementById("loginForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!username || !password) {
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    $.ajax({
        type: "POST",
        url: "/user/login",
        data: {
            "username": username,
            "password": password
        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken);
        },
        success: function(res) {
            if (res === "ok") {
                window.location.href = "/user/find"; // 로그인 성공 시 이동할 페이지
            } else {
                // Handle login failure
            }
        },
        error: function(err) {
            console.log("에러 발생", err);
        }
    });
});

