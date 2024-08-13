function goToLogin() {
    // login.html로 이동
    window.location.href = 'login.html';
}

function goToRegister() {
    // register.html로 이동
    window.location.href = 'register.html';
}



document.addEventListener("DOMContentLoaded", function() {
    const $agreementForm = document.querySelector('.agreement-form2');
    const $selectAll = $agreementForm.querySelector('.select-all');
    const $listInput = $agreementForm.querySelectorAll('.list input');
    const $selectAllMkt = $agreementForm.querySelector('.select-all-marketing');
    const $mandatoryInputs = $agreementForm.querySelectorAll('.item:not(.inner) input');
    const $optionalInputs = $agreementForm.querySelectorAll('.item.inner input');
    const $submitButton = $agreementForm.querySelector('.submit-button');

    // 체크박스 전체 선택 및 해제 기능
    const toggleCheckbox = (allBox, itemBox) => {
        allBox.addEventListener('change', () => {
            itemBox.forEach((item) => {
                item.checked = allBox.checked;
            });
        });
    };

    // 전체 동의 체크박스 제어
    toggleCheckbox($selectAll, $listInput);


    // 개별 체크박스 상태 변경 시 전체 동의 체크박스 상태 변경
    $listInput.forEach((item) => {
        item.addEventListener('change', () => {
            const allMandatoryChecked = Array.from($mandatoryInputs).every(i => i.checked);
            const allChecked = Array.from($listInput).every(i => i.checked);
            $selectAll.checked = allChecked;
            $selectAllMkt.checked = Array.from($optionalInputs).every(i => i.checked);
        });
    });

    // 가입 버튼 클릭 시 필수 항목 확인
    $submitButton.addEventListener('click', () => {
        const allMandatoryChecked = Array.from($mandatoryInputs).every(input => input.checked);
        if (allMandatoryChecked) {
            // 필수 항목 모두 체크 시 로그인 화면으로 이동.
            window.location.href = 'login.html'; 
        } else {
            // 필수 항목 체크 안 된 경우 경고창 표시
            // alert("모든 필수 항목에 동의해야 합니다.");
            const modal = document.getElementById('myModal');
            modal.style.display = 'block';

            // 모달 닫기 버튼 이벤트 처리
            document.querySelector('.modal_close').addEventListener('click', function() {
                // 모달 닫기
                document.querySelector('.modal').style.display = 'none';
            });
        }
    });
});

