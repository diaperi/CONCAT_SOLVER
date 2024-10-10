document.addEventListener('DOMContentLoaded', function () {
    const downBarButton = document.querySelector('.myPageMain_downBar');
    const upBarButton = document.querySelector('.myPageMain_UpBar');
    const targetSection2 = document.querySelector('.header_navbar');

    downBarButton.addEventListener('click', function () {
        window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});
    });

    upBarButton.addEventListener('click', function () {
        targetSection2.scrollIntoView({behavior: 'smooth'});
    });

    const searchInput = document.querySelector('.myPageMain_input input');
    const originalVideoBoxContent = document.querySelector('.myPageMain_lastVideoBox').innerHTML; // 원래 내용 저장

    searchInput.addEventListener('keyup', function (event) {
        const keyword = event.target.value.trim();

        if (event.key === 'Enter' && keyword) {
            // 검색어가 있을 때만 API 요청
            fetch(`/myPage/searchVideos?keyword=${encodeURIComponent(keyword)}`)
                .then(response => response.json())
                .then(data => updateVideoBox(data, keyword))
                .catch(error => console.error('Error fetching data:', error));
        } else if (keyword === '') {
            // 검색창이 비어있을 때 원래 상태로 복원
            document.querySelector('.myPageMain_lastVideoBox').innerHTML = originalVideoBoxContent;
            addScrollEventToDynamicElements(); // 동적으로 다시 생성된 요소에 이벤트 리스너 추가
        }
    });

    function updateVideoBox(videos, keyword) {
        const videoBox = document.querySelector('.myPageMain_lastVideoBox');
        if (videos.length === 0) {
            videoBox.innerHTML = `<div class="no-results">'${keyword}'에 대한 영상을 찾지 못했어요.</div>`;
        } else {
            videoBox.innerHTML = `
            <div class="myPageMain_videoBox1" id="videoBoxHeader">
                <span>키워드 '${keyword}' 로 찾은 영상이예요</span>
                <span onclick="goToMyPagePop()">전체보기 <i class="fa-solid fa-angles-right"></i></span>
            </div>
            <div class="myPageMain_videoBox2" id="scrollableVideoContainer" style="display: flex; overflow-x: hidden; overflow-y: auto; white-space: nowrap; height: 55vh; scrollbar-width: none; -ms-overflow-style: none;">
                ${videos.map(video => `
                    <div class="myPageMain_videoBox3" id="videoItem_${video.key.split('_')[2]}" style="margin-right: 10px; display: inline-block;">
                        <a href="/myPage/myPageDetail?timestamp=${video.key.split('_')[2] + '_' + video.key.split('_')[3].substring(0, 6)}">
                            <img src="${video.url}" alt="Image">
                        </a>
                        <div class="myPageMain_videoBox4" id="videoMeta_${video.key.split('_')[2]}" style="font-size: 14px;">
                            <span>[ ${video.gptTitle} ]</span>
                            <span>[ 생성시간: ${video.lastModifiedDate} ]</span>
                        </div>
                    </div>
                `).join('')}
            </div>
            <span class="myPageMain_downBar"><i class="fa-solid fa-angles-down"></i></span>
        `;

            // 이벤트 위임을 사용하여 동적으로 생성된 .myPageMain_downBar 요소에도 클릭 이벤트 적용
            document.querySelector('.myPageMain_lastVideoBox').addEventListener('click', function (event) {
                if (event.target.closest('.myPageMain_downBar')) {
                    window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});
                }
            });

            // 스크롤 기능이 적용된 부분이 제대로 동작하도록 업데이트
            const videoBox2 = document.getElementById('scrollableVideoContainer');
            videoBox2.style.whiteSpace = 'nowrap'; // 자식 요소들이 가로로 나열되도록 설정
        }
    }

    function addScrollEventToDynamicElements() {
        const downBarButton = document.querySelector('.myPageMain_downBar');
        if (downBarButton) {
            downBarButton.addEventListener('click', function () {
                window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});
            });
        }
    }

    addScrollEventToDynamicElements(); // 초기 로드 시에도 스크롤 이벤트 리스너 추가
});
