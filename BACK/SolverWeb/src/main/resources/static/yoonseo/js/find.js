function goToLogin() {
    // login.html로 이동합니다.
    window.location.href = "/user/login";
}

// const emailInput = document.getElementById('email-input');
// const emailButton = document.getElementById('find-email-Button');

// emailInput.addEventListener('input', function() {
//     if (emailInput.value.trim() !== '') {
//         emailButton.disabled = false;
//     } else {
//         emailButton.disabled = true;
//     }
// });

// document.getElementById('find-email-Button').addEventListener('click', function() {
//     // 이메일 전송 로직 추가 가능

//     // 모달 열기
//     document.querySelector('.modal').style.display = 'block';
// });

// document.querySelector('.modal_close').addEventListener('click', function() {
//     // 모달 닫기
//     document.querySelector('.modal').style.display = 'none';
// });

// // 모달 외부 클릭 시 닫기
// window.onclick = function(event) {
//     if (event.target.classList.contains('modal')) {
//         document.querySelector('.modal').style.display = 'none';
//     }
// }
const emailInput = document.getElementById('email-input');
const emailButton = document.getElementById('find-email-Button');
const modal = document.querySelector('.modal');
const modalCloseBtn = document.querySelector('.modal_close');
const modalContent = document.querySelector('.enroll_box');

// 이메일 유효성 검사 패턴
const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

// 이메일 입력란의 입력 내용 변화 감지
emailInput.addEventListener('input', function() {
    if (emailInput.value.trim() !== '') {
        emailButton.disabled = false;
    } else {
        emailButton.disabled = true;
    }
});

// '이메일로 받기' 버튼 클릭 시 처리
emailButton.addEventListener('click', function() {
    const email = emailInput.value.trim();

    // 이메일 유효성 검사
    if (!emailPattern.test(email)) {
        alert('유효한 이메일 주소를 입력해주세요.');
        return;
    }

    // 여기에 이메일을 처리하는 로직을 추가하면 됩니다.
    // 예를 들어, 이메일 발송 등의 처리를 수행할 수 있습니다.

    // AJAX 요청을 통해 이메일 전송
    fetch(`/send-email?email=${encodeURIComponent(email)}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답에 문제가 발생했습니다.');
            }
            return response.text();
        })
        .then(result => {
            console.log(result);
            if (result.includes("Email sent")) {
                modalContent.querySelector('p.menu_msg').textContent = '이메일을 발송했습니다.';
                modalContent.querySelector('p.menu_msg2').textContent = '이메일에서 계정 정보를 확인해주세요.';
                modal.style.display = 'block';
            } else {
                alert("회원 정보를 찾을 수 없습니다.");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("문제가 발생했습니다. 나중에 다시 시도해 주세요.");
        });

    
    // 모달 팝업 열기
    modal.style.display = 'block';
});

// 모달 닫기 버튼 클릭 시 처리
modalCloseBtn.addEventListener('click', function() {
    modal.style.display = 'none';
});

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    if (event.target === modal) {
        modal.style.display = 'none';
    }
};
