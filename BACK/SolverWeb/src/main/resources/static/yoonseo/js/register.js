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
    // login.html로 이동
    window.location.href = '/user/login';
}

function goToRegister() {
    // login.html로 이동
    window.location.href = '/user/sign';
}

// 아이디 중복 확인 (여기서는 간단히 아이디가 'test'일 경우 중복으로 간주) 1번수정
// function checkDuplicate() {
//     const userId = document.getElementById('idInput').value;
//
//     fetch(`/api/check-userid?userId=${userId}`)
//         .then(response => response.json())
//         .then(isDuplicate => {
//             const validationMessage = document.getElementById('idValidation');
//             if (isDuplicate) {
//                 validationMessage.textContent = "이미 사용 중인 아이디입니다.";
//             } else {
//                 validationMessage.textContent = "사용 가능한 아이디입니다.";
//             }
//         })
//         .catch(error => console.error('Error:', error));
// }
//한번더 수정
// function checkDuplicate() {
//     const userId = document.getElementById('idInput').value;
//
//     fetch(`/api/check-userid?userId=${userId}`)
//         .then(response => {
//             if (response.status === 409) {
//                 return response.text().then(message => {
//                     document.getElementById('idValidation').textContent = message;
//                 });
//             } else {
//                 return response.text().then(message => {
//                     document.getElementById('idValidation').textContent = message;
//                 });
//             }
//         })
//         .catch(error => console.error('Error:', error));
// }
// const idCheck = () => {
//     const userId = document.getElementById("idInput").value;
//     const checkResult = document.getElementById("check-result");
//     console.log("입력값: ", userId);
//
//     $.ajax({
//         type   : "post",
//         url    : "/user/id-check",
//         data   : {
//             "userId": userId
//         },
//         success: function (res) {
//             console.log("요청성공", res);
//             if (res === "ok") {
//                 console.log("사용가능한 아이디");
//                 checkResult.style.color = "green";
//                 checkResult.innerHTML = "사용가능한 아이디입니다.";
//             } else {
//                 console.log("이미 사용중인 아이디");
//                 checkResult.style.color = "red";
//                 checkResult.innerHTML = "이미 사용중인 아이디입니다.";
//             }
//         },
//         error  : function (err) {
//             console.log("에러발생", err);
//         }
//     });
// }
const checkDuplicateId = () => {
    const id = document.getElementById('idInput').value.trim();  // 입력된 아이디 가져오기
    const checkResult = document.getElementById('check-result'); // 결과를 표시할 div

    if (id === '') { // 입력값이 비어 있는지 확인
        alert('아이디를 입력해주세요.');
        return;
    }

    $.ajax({
        type: "POST", // HTTP 요청 방식
        url: "/user/id-check", // 요청할 URL
        data: {
            "id": id // 서버로 전송할 데이터
        },
        success: function(res) {
            if (res === "ok") { // 서버 응답이 "ok"일 경우
                checkResult.style.color = "green";
                checkResult.innerHTML = "사용 가능한 아이디입니다.";
            } else if (res === "exists") { // 서버 응답이 "exists"일 경우
                checkResult.style.color = "red";
                checkResult.innerHTML = "이미 사용 중인 아이디입니다.";
            } else {
                checkResult.style.color = "orange";
                checkResult.innerHTML = "알 수 없는 오류가 발생했습니다.";
            }
        },
        error: function(err) {
            console.error("오류 발생:", err); // 에러 발생 시 콘솔에 출력
            checkResult.style.color = "red";
            checkResult.innerHTML = "아이디 중복 체크 중 오류가 발생했습니다.";
        }
    });
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
        return false;
    }
     

     // 비밀번호 길이 확인
     if (pw.length < 6 || pw.length > 20) {
        alert('비밀번호는 6~20자 이어야 합니다.');
         return false;
    }

    // 비밀번호 확인
    if (pw !== pwCheck) {
        alert('비밀번호가 일치하지 않습니다.');
        return false;
    }

    // 이메일 형식 검사 (간단한 형식 검사)
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    if (!emailPattern.test(email)) {
        alert('유효한 이메일 주소를 입력해주세요.');
        return false;
    }

    window.location.href = '/user/login'; }