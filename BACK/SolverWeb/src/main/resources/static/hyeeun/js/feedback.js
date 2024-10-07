document.addEventListener('DOMContentLoaded', function() {
    const videoBox = document.getElementById('videoBox');
    const video = document.getElementById('popup-video');
    const closeButton = document.getElementById('closeButton');
    const myPageDetailRightBox = document.getElementById('myPageDetail_rightBox');
    const contentDisplay = document.getElementById('contentDisplay');
    const participantSelect = document.getElementById('participant');
    const conversationBtn = document.getElementById('conversationBtn');
    const feedbackBtn = document.getElementById('feedbackBtn');

    // 비디오 전체 화면 전환 및 닫기 버튼 처리
    document.getElementById('expandButton').addEventListener('click', function() {
        videoBox.classList.add('fullscreen-video');
        videoBox.classList.remove('slide-out-left');
        videoBox.classList.add('slide-in-right');
        myPageDetailRightBox.classList.remove('visible');
        myPageDetailRightBox.classList.add('hidden');
        closeButton.style.display = 'block';
    });

    closeButton.addEventListener('click', function() {
        videoBox.classList.remove('fullscreen-video');
        videoBox.classList.remove('slide-in-right');
        videoBox.classList.add('slide-out-left');
        myPageDetailRightBox.classList.remove('hidden');
        myPageDetailRightBox.classList.add('visible');
        closeButton.style.display = 'none';
    });

    // 대화 내용 불러오기
    conversationBtn.addEventListener('click', function() {
        contentDisplay.textContent = '대화 내용을 불러오는 중입니다...';
        fetch('/myPage/getConversation')
            .then(response => response.json())
            .then(data => {
                contentDisplay.textContent = data.conversation || '대화 내용을 불러오지 못했습니다.';
            })
            .catch(() => {
                contentDisplay.textContent = '대화 내용을 불러오는 중 오류가 발생했습니다.';
            });
    });

    // 피드백 불러오기
    feedbackBtn.addEventListener('click', function() {
        const participant = participantSelect.value;
        contentDisplay.textContent = '피드백을 불러오는 중입니다...';
        fetch(`/myPage/getFeedback?participant=${participant}`)
            .then(response => response.json())
            .then(data => {
                contentDisplay.textContent = data.feedback || '피드백을 불러오지 못했습니다.';
            })
            .catch(() => {
                contentDisplay.textContent = '피드백을 불러오는 중 오류가 발생했습니다.';
            });
    });
});

function showTab(selectedTabId, contentId) {
    // 모든 탭과 콘텐츠를 숨기고 비활성화
    var tabs = document.querySelectorAll('.myPageDetail_rightTop span');
    var contents = document.querySelectorAll('.tabContent');
    tabs.forEach(tab => tab.classList.remove('active'));
    contents.forEach(content => content.classList.add('hidden'));

    // 선택된 탭과 콘텐츠만 활성화
    document.getElementById(selectedTabId).classList.add('active');
    document.getElementById(contentId).classList.remove('hidden');
}

