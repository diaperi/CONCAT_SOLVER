// document.getElementById('expandButton').addEventListener('click', function () {
//     var videoBox = document.getElementById('videoBox');
//     var video = document.getElementById('popup-video');
//     var closeButton = document.getElementById('closeButton');
//     var myPageDetailRightBox = document.getElementById('myPageDetail_rightBox');
//
//     videoBox.classList.add('fullscreen-video');
//     videoBox.classList.remove('slide-out-left');
//     videoBox.classList.add('slide-in-right');
//     myPageDetailRightBox.classList.remove('visible');
//     myPageDetailRightBox.classList.add('hidden');
//
//     closeButton.style.display = 'block';
//
//     closeButton.addEventListener('click', function () {
//         videoBox.classList.remove('fullscreen-video');
//         videoBox.classList.remove('slide-in-right');
//         videoBox.classList.add('slide-out-left');
//         myPageDetailRightBox.classList.remove('hidden');
//         myPageDetailRightBox.classList.add('visible');
//         closeButton.style.display = 'none';
//     });
// });

// Web Speech API로 gptResponse 읽기 기능 추가
// 숫자를 한자식으로 변환하는 함수
function convertNumberToKorean(text) {
    const numberMap = {
        '0': '공',
        '1': '일',
        '2': '이',
        '3': '삼',
        '4': '사',
        '5': '오',
        '6': '육',
        '7': '칠',
        '8': '팔',
        '9': '구'
    };

    return text.replace(/\d/g, function (match) {
        return numberMap[match];
    });
}

// 이모티콘을 제거하는 함수
function removeEmojis(text) {
    return text.replace(/[\u{1F600}-\u{1F64F}]/gu, '')  // 얼굴 이모티콘 제거
        .replace(/[\u{1F300}-\u{1F5FF}]/gu, '')  // 기타 이모티콘 제거
        .replace(/[\u{1F680}-\u{1F6FF}]/gu, '')  // 교통, 지도 기호 이모티콘 제거
        .replace(/[\u{2600}-\u{26FF}]/gu, '')    // 다양한 기호 이모티콘 제거
        .replace(/[\u{2700}-\u{27BF}]/gu, '');   // 기타 이모티콘 제거
}

// 읽기 버튼 기능 추가
document.getElementById('readBtn').addEventListener('click', function () {
    var gptText = document.getElementById('summary').innerText; // 텍스트 가져오기

    // 이모티콘 제거
    var filteredText = removeEmojis(gptText);

    // 숫자를 한자식으로 변환
    var convertedText = convertNumberToKorean(filteredText);

    if ('speechSynthesis' in window) {  // Web Speech API 지원 여부 확인
        if (window.speechSynthesis.speaking) {
            // 현재 음성 재생 중이면 중지
            window.speechSynthesis.cancel();
        } else {
            // 음성 재생이 안 되고 있으면 새로 읽기 시작
            var speech = new SpeechSynthesisUtterance(convertedText);
            speech.lang = 'ko-KR'; // 한국어 설정
            window.speechSynthesis.speak(speech); // 텍스트 음성으로 읽기
        }
    } else {
        alert('이 브라우저는 음성 합성을 지원하지 않습니다.');
    }
});


// 선택한 요소들에 대한 동작 추가 (기존 코드 유지)
const spans = document.querySelectorAll('.myPageDetail_rightTop > span');
const rightMains = document.querySelectorAll('.myPageDetail_rightMain');

// 휴지통 버튼 클릭 시
document.getElementById('trashBtn').addEventListener('click', function () {
    const videoElement = document.getElementById('myVideo');
    const videoUrl = videoElement.querySelector('source').src;
    const queryParams = new URLSearchParams({videoUrl}).toString();

    fetch(`/myPage/moveToTrash?${queryParams}`, {
        method : 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            console.log('Response Data:', data);
            if (data.success) {
                alert('삭제 성공');
            } else {
                alert('삭제 실패');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred.');
        });
});

// 페이지 로드 시 저장된 상태를 불러옴
// window.addEventListener('load', function () {
//     const savedState = localStorage.getItem('myPageDetailState');
//     if (savedState === 'solution') {
//         showSolution();
//     } else {
//         showConversation();
//     }
// });

// "대화내용" 버튼 클릭 시 첫 번째 화면을 보여주고 두 번째 화면을 숨김
// spans[0].addEventListener('click', function () {
//     showConversation();
//     localStorage.setItem('myPageDetailState', 'conversation'); // 상태 저장
// });
//
// // "해결책" 버튼 클릭 시 두 번째 화면을 보여주고 첫 번째 화면을 숨김
// spans[1].addEventListener('click', function () {
//     showSolution();
//     localStorage.setItem('myPageDetailState', 'solution'); // 상태 저장
// });
//
// function showConversation() {
//     rightMains[0].classList.remove('hidden'); // 첫 번째 화면 보이기
//     rightMains[1].classList.add('hidden'); // 두 번째 화면 숨기기
//     spans[0].classList.add('active'); // "대화내용" 버튼 파란색으로 유지
//     spans[1].classList.remove('active'); // "해결책" 버튼 기본 색상으로
// }
//
// function showSolution() {
//     rightMains[0].classList.add('hidden'); // 첫 번째 화면 숨기기
//     rightMains[1].classList.remove('hidden'); // 두 번째 화면 보이기
//     spans[1].classList.add('active'); // "해결책" 버튼 파란색으로 유지
//     spans[0].classList.remove('active'); // "대화내용" 버튼 기본 색상으로
// }
//
// if (rightMains.length > 1) {
//     rightMains[1].classList.remove('hidden'); // 두 번째 화면 보이기
// } else {
//     console.error('rightMains[1] does not exist.');
// }
//

document.querySelectorAll('.accordion-item').forEach(button => {
    button.addEventListener('click', () => {
        const content = button.nextElementSibling;
        content.style.display = content.style.display === 'block' ? 'none' : 'block';
    });
});
