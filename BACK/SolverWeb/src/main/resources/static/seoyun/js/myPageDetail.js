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

let isSpeaking = false; // 읽기 상태 추적 변수
let utterance; // SpeechSynthesisUtterance 객체

// 이모티콘 및 숫자 처리 함수
function preprocessText(text) {
    // 이모티콘 제거
    text = text.replace(/[\p{Emoji_Presentation}\p{Extended_Pictographic}]/gu, "");

    // 숫자를 한글로 변환
    text = text.replace(/\b1\b/g, "일");
    text = text.replace(/\b2\b/g, "이");
    text = text.replace(/\b3\b/g, "삼");
    text = text.replace(/\b4\b/g, "사");
    text = text.replace(/\b5\b/g, "오");
    text = text.replace(/\b6\b/g, "육");
    text = text.replace(/\b7\b/g, "칠");
    text = text.replace(/\b8\b/g, "팔");
    text = text.replace(/\b9\b/g, "구");

    return text;
}

document.getElementById('readBtn').addEventListener('click', function () {
    if (isSpeaking) {
        // 이미 읽고 있으면 정지
        speechSynthesis.cancel();
        isSpeaking = false;
    } else {
        // 읽기 시작
        const textContainer = document.querySelector('.down_right_textBox');
        let textToRead = textContainer.innerText.trim(); // 텍스트 추출

        // 이모티콘 및 숫자 처리
        textToRead = preprocessText(textToRead);

        utterance = new SpeechSynthesisUtterance(textToRead);
        utterance.lang = 'ko-KR'; // 한국어로 설정

        // 읽기 시작 이벤트
        utterance.onstart = function () {
            isSpeaking = true;
        };

        // 읽기 종료 이벤트
        utterance.onend = function () {
            isSpeaking = false;
        };

        speechSynthesis.speak(utterance);
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
