document.getElementById('expandButton').addEventListener('click', function () {
    var videoBox = document.getElementById('videoBox');
    var video = document.getElementById('popup-video');
    var closeButton = document.getElementById('closeButton');
    var myPageDetailRightBox = document.getElementById('myPageDetail_rightBox');

    videoBox.classList.add('fullscreen-video');
    videoBox.classList.remove('slide-out-left');
    videoBox.classList.add('slide-in-right');
    myPageDetailRightBox.classList.remove('visible');
    myPageDetailRightBox.classList.add('hidden');

    closeButton.style.display = 'block';

    closeButton.addEventListener('click', function () {
        videoBox.classList.remove('fullscreen-video');
        videoBox.classList.remove('slide-in-right');
        videoBox.classList.add('slide-out-left');
        myPageDetailRightBox.classList.remove('hidden');
        myPageDetailRightBox.classList.add('visible');
        closeButton.style.display = 'none';
    });
});
const spans = document.querySelectorAll('.myPageDetail_rightTop > span');
const rightMains = document.querySelectorAll('.myPageDetail_rightMain');

// 페이지 로드 시 저장된 상태를 불러옴
window.addEventListener('load', function () {
    const savedState = localStorage.getItem('myPageDetailState');
    if (savedState === 'solution') {
        showSolution();
    } else {
        showConversation();
    }
});

// "대화내용" 버튼 클릭 시 첫 번째 화면을 보여주고 두 번째 화면을 숨김
spans[0].addEventListener('click', function () {
    showConversation();
    localStorage.setItem('myPageDetailState', 'conversation'); // 상태 저장
});

// "해결책" 버튼 클릭 시 두 번째 화면을 보여주고 첫 번째 화면을 숨김
spans[1].addEventListener('click', function () {
    showSolution();
    localStorage.setItem('myPageDetailState', 'solution'); // 상태 저장
});

function showConversation() {
    rightMains[0].classList.remove('hidden'); // 첫 번째 화면 보이기
    rightMains[1].classList.add('hidden'); // 두 번째 화면 숨기기
    spans[0].classList.add('active'); // "대화내용" 버튼 파란색으로 유지
    spans[1].classList.remove('active'); // "해결책" 버튼 기본 색상으로
}

function showSolution() {
    rightMains[0].classList.add('hidden'); // 첫 번째 화면 숨기기
    rightMains[1].classList.remove('hidden'); // 두 번째 화면 보이기
    spans[1].classList.add('active'); // "해결책" 버튼 파란색으로 유지
    spans[0].classList.remove('active'); // "대화내용" 버튼 기본 색상으로
}

if (rightMains.length > 1) {
    rightMains[1].classList.remove('hidden'); // 두 번째 화면 보이기
} else {
    console.error('rightMains[1] does not exist.');
}

document.getElementById('trashBtn').addEventListener('click', function () {
    const videoElement = document.getElementById('popup-video');
    const videoUrl = videoElement.querySelector('source').src;

    // 세션 스토리지에서 userDTO 가져옴
    const userDTO = JSON.parse(sessionStorage.getItem('userDTO'));

    if (!userDTO || !userDTO.userId) {
        alert('User data is not available');
        return;
    }

    const queryParams = new URLSearchParams({
        videoUrl: videoUrl,
        userId: userDTO.userId
    }).toString();

    fetch(`/yuna/trash?${queryParams}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            console.log('Response Data:', data);
            if (data.success) {
                alert('Video moved to trash successfully!');
            } else {
                alert('Failed to move video to trash.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred.');
        });
});
