document.addEventListener('DOMContentLoaded', function() {
    const sendBtn = document.getElementById('sendCertificationCodeBtn');
    const verifyBtn = document.getElementById('verifyCodeBtn');

    // 알림 메시지를 처리하는 함수
    function showAlert(message) {
        alert(message);
    }

    // 인증 코드 전송 버튼
    if (sendBtn) {
        sendBtn.addEventListener('click', function () {
            const phoneNumber = document.getElementById('userPhone').value;
            const userId = document.getElementById('userId').value;

            if (!phoneNumber) {
                showAlert('전화번호를 입력해주세요.');
                return;
            }

            if (!userId) {
                showAlert('userId를 입력해주세요.');
                return;
            }

            // 전화번호 형식 검사
            let regex = /^(010)(\d{4})(\d{4})$/;
            if (!regex.test(phoneNumber)) {
                showAlert("잘못된 전화번호 형식입니다.");
                return;
            }

            // 인증 코드 전송 요청
            fetch('/sms/sendVerificationCode', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    userId: userId,
                    phoneNumber: phoneNumber
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message === "인증 코드 전송, 전화번호 저장 성공") {
                        showAlert("인증 코드가 전송되었습니다.");

                        // 인증 코드 전송 성공 후, 전화번호 저장 요청
                        savePhoneNumber(phoneNumber);
                    } else {
                        showAlert(data.message || "인증 코드 전송에 실패했습니다.");
                    }
                })
                // .catch(error => {
                //     console.error('Error:', error.message || error);
                //     showAlert('인증 코드 전송 중 오류가 발생했습니다.');
                // });
        });
    }

    // 인증 코드 확인 버튼
    if (verifyBtn) {
        verifyBtn.addEventListener('click', function () {
            const phoneNumber = document.getElementById('userPhone').value;
            const verificationCode = document.getElementById('verificationCode').value;

            if (!phoneNumber) {
                showAlert('전화번호를 입력해주세요.');
                return;
            }

            if (!verificationCode) {
                showAlert('인증번호를 입력해주세요.');
                return;
            }

            // 인증 코드 검증 요청
            fetch('/sms/verifyCode', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    phoneNumber: phoneNumber,
                    verificationCode: verificationCode  // 입력한 인증 코드 전송
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        sessionStorage.setItem('isVerified', 'Y');
                        showAlert('인증이 성공했습니다.');
                    } else {
                        showAlert('인증 코드가 일치하지 않습니다.');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showAlert('인증 코드 검증에 실패했습니다.');
                });
        })
    }

    const verifyCodeInput = document.getElementById('verificationCode');
    const userId = document.getElementById('userId').value;

    // 서버에서 인증 상태를 조회
    fetch(`/sms/checkVerificationStatus?userId=${userId}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === "Y") {
                verifyCodeInput.placeholder = '인증이 완료된 사용자입니다.';
            } else {
                verifyCodeInput.placeholder = '인증번호를 입력하세요';
            }
        })
        .catch(error => {
            console.error('Error fetching verification status:', error);
            verifyCodeInput.placeholder = '인증번호를 입력하세요';
        });
});