document.addEventListener("DOMContentLoaded", () => {
    const slider = document.querySelector(".slider");

    let isDragging = false;
    let startX = 0;
    let currentTranslate = 0;
    let prevTranslate = 0;

    const setSliderPosition = () => {
        slider.style.transform = `translateX(${currentTranslate}px)`;
    };

    const handleMouseDown = (e) => {
        isDragging = true;
        startX = e.clientX;
        slider.style.cursor = "grabbing";
    };

    const handleMouseMove = (e) => {
        if (!isDragging) return;
        const currentX = e.clientX;
        const deltaX = currentX - startX;
        currentTranslate = prevTranslate + deltaX;
        setSliderPosition();
    };

    const handleMouseUp = () => {
        isDragging = false;
        prevTranslate = currentTranslate;
        slider.style.cursor = "grab";
    };

    slider.addEventListener("mousedown", handleMouseDown);
    slider.addEventListener("mousemove", handleMouseMove);
    slider.addEventListener("mouseup", handleMouseUp);
    slider.addEventListener("mouseleave", handleMouseUp); // 마우스가 영역을 벗어나면 드래그 중단
});


// **************************힐링 영상*****************************
// YouTube API 키 (반드시 발급받은 키로 대체해야 함)
const API_KEY = "AIzaSyCtNiXXND6UJg-Tmn4u1bWUaH0dP9X9jqw";

// 키워드 배열
const SEARCH_KEYWORDS = ["힐링 음악", "명상 음악", "힐링 영상"];
const MAX_RESULTS = 10; // 각 키워드 당 가져올 동영상 수

// 각 키워드로 검색된 동영상 저장
let allVideos = [];

// YouTube 검색 및 동영상 가져오기
async function fetchVideosForKeyword(keyword) {
    const response = await fetch(`https://www.googleapis.com/youtube/v3/search?part=snippet&q=${encodeURIComponent(keyword)}&type=video&maxResults=${MAX_RESULTS}&order=viewCount&key=${API_KEY}`);
    const data = await response.json();
    const videoItems = data.items;

    if (videoItems && videoItems.length > 0) {
        return videoItems.map((item) => item.id.videoId); // 동영상 ID 목록 반환
    } else {
        console.error(`No videos found for keyword: ${keyword}`);
        return [];
    }
}

// 랜덤으로 동영상 하나 선택
function getRandomVideo() {
    const randomIndex = Math.floor(Math.random() * allVideos.length);
    return allVideos[randomIndex];
}

// YouTube IFrame Player API 로드 및 자동 재생 설정
function loadYouTubePlayer(videoId) {
    const playerScript = document.createElement("script");
    playerScript.src = "https://www.youtube.com/iframe_api";
    document.body.appendChild(playerScript);

    window.onYouTubeIframeAPIReady = () => {
        new YT.Player("player", {
            videoId   : videoId,
            playerVars: {
                autoplay      : 1, // 자동 재생
                mute          : 0, // 음소거 해제
                controls      : 1, // 재생 컨트롤러 표시
                rel           : 0, // 관련 동영상 표시 안 함
                modestbranding: 1, // YouTube 로고 최소화
                enablejsapi   : 1, // JS API 활성화
                origin        : window.location.origin
            }
        });
    };
}

// 모든 키워드에 대해 동영상 가져오기
async function fetchAllVideos() {
    for (const keyword of SEARCH_KEYWORDS) {
        const videos = await fetchVideosForKeyword(keyword);
        allVideos = [...allVideos, ...videos]; // 가져온 동영상 ID를 전체 목록에 추가
    }

    if (allVideos.length > 0) {
        const randomVideoId = getRandomVideo(); // 랜덤 동영상 ID 선택
        loadYouTubePlayer(randomVideoId); // YouTube 플레이어에 로드
    } else {
        // myVideo 변수가 유효한지 확인
        if (!myVideo || myVideo === "") {
            document.body.innerHTML = "<h1>동영상을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.</h1>";
        }
    }
}

// 페이지 로드 시 실행
window.onload = async () => {
    await fetchAllVideos();
};
