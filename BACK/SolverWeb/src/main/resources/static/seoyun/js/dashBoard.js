document.addEventListener('DOMContentLoaded', function () {
    const monthNames = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"];

    let today = new Date();
    let currentMonth = today.getMonth();
    let currentYear = today.getFullYear();
    let currentDate = today.getDate(); // Get current date

    const monthYear = document.getElementById("month-year");
    const calendarBody = document.querySelector("#calendar-body tbody");

    const prevButton = document.getElementById("prev");
    const nextButton = document.getElementById("next");


    function showCalendar(month, year) {
        let firstDay = (new Date(year, month)).getDay();
        let daysInMonth = new Date(year, month + 1, 0).getDate();

        calendarBody.innerHTML = "";
        monthYear.innerHTML = `${monthNames[month]} ${year}`;

        let date = 1;

        for (let i = 0; i < 6; i++) {
            let row = document.createElement("tr");

            for (let j = 0; j < 7; j++) {
                let cell = document.createElement("td");
                if (i === 0 && j < firstDay) {
                    cell.textContent = "";
                } else if (date > daysInMonth) {
                    cell.textContent = "";
                } else {
                    cell.textContent = date;
                    cell.dataset.date = `${year}-${month + 1}-${date}`; // 날짜 데이터 설정
                    if (j === 0) {
                        cell.classList.add("sunday");
                    }
                    if (date === currentDate && month === today.getMonth() && year === today.getFullYear()) {
                        cell.classList.add("today"); // Highlight today's date
                    }
                    // Capture the current values of date, month, and year
                    const capturedDate = date;
                    const capturedMonth = month;
                    const capturedYear = year;
                    date++;
                }
                row.appendChild(cell);
            }
            calendarBody.appendChild(row);
        }
    }


    prevButton.addEventListener('click', function () {
        currentYear = (currentMonth === 0) ? currentYear - 1 : currentYear;
        currentMonth = (currentMonth === 0) ? 11 : currentMonth - 1;
        showCalendar(currentMonth, currentYear);
    });

    nextButton.addEventListener('click', function () {
        currentYear = (currentMonth === 11) ? currentYear + 1 : currentYear;
        currentMonth = (currentMonth + 1) % 12;
        showCalendar(currentMonth, currentYear);
    });


    showCalendar(currentMonth, currentYear);
});


const scrollArrow = document.querySelector('.scroll-arrow');
const arrowIcon = scrollArrow.querySelector('i');
const topElement = document.querySelector('.moreDashBoard_div1');
const downElement = document.querySelector('.moreDashBoard_top_right_box1');

scrollArrow.addEventListener('click', function () {
    if (arrowIcon.classList.contains('fa-angles-down')) {
        // 아래로 스크롤
        downElement.scrollIntoView({behavior: 'smooth'});
    } else {
        // 위로 스크롤
        topElement.scrollIntoView({behavior: 'smooth'});
    }
});

window.addEventListener('scroll', function () {
    // 현재 스크롤 위치가 화면의 중간을 지났는지 확인
    if (window.scrollY > window.innerHeight / 2) {
        // 버튼을 위로 가는 화살표로 변경
        arrowIcon.classList.remove('fa-angles-down');
        arrowIcon.classList.add('fa-angles-up');
    } else {
        // 버튼을 아래로 가는 화살표로 변경
        arrowIcon.classList.remove('fa-angles-up');
        arrowIcon.classList.add('fa-angles-down');
    }
});
document.addEventListener('DOMContentLoaded', function () {
    const element = document.querySelector('.moreDashBoard_div1');
    if (element) {
        // 사용자가 스크롤할 때 애니메이션이 동작하도록 하기 위해 Intersection Observer를 사용할 수도 있습니다.
        // element.classList.add('fade-in-up');

        // 페이지 로드 시 애니메이션을 적용하려면 아래 코드 사용
        setTimeout(() => {
            element.classList.add('fade-in-up');
        }, 100); // 약간의 지연을 줘서 애니메이션이 부드럽게 시작되도록 설정
    }
});


//********************* 명언, 충고, 이미지 배열 ************************
document.addEventListener("DOMContentLoaded", function () {
    // 명언, 충고, 이미지 배열 설정
    const quotes = [
        "가족은 우리가 세상에서 받을 수 있는 가장 큰 축복입니다.",
        "가족은 사랑이 시작되고 끝나는 곳입니다.",
        "가족은 인생의 나침반이며 우리를 올바른 길로 인도해줍니다.",
        "가족이란 사랑이 함께 모여 사는 것입니다.",
        "가족은 우리를 보호하는 안전한 울타리입니다.",
        "행복한 가정은 천국의 한 조각입니다.",
        "가족은 당신의 힘과 용기를 얻는 곳입니다.",
        "가족은 결코 포기하지 않는 사랑의 예입니다.",
        "가족은 서로를 지지하는 기둥입니다.",
        "사랑은 집을 만들고, 가족은 그 집을 살게 합니다.",
        "가족은 함께 웃고 함께 우는 사람들입니다.",
        "가족은 인생에서 가장 중요한 단어 중 하나입니다.",
        "가족은 당신이 사랑하고 신뢰할 수 있는 사람들입니다.",
        "가족은 당신의 첫 번째 팀입니다.",
        "가족이란 서로를 끝없이 사랑하는 것에 관한 것입니다.",
        "가족은 당신의 첫 번째 스승입니다.",
        "가족은 당신이 누구인지, 어디에서 왔는지 기억하게 해줍니다.",
        "가족은 당신의 첫 번째 사랑입니다.",
        "가족은 당신이 사랑하는 법을 배우는 곳입니다.",
        "가족은 세상에서 가장 안전한 항구입니다.",
        "가족은 당신의 첫 번째 기쁨입니다.",
        "가족은 당신의 첫 번째 친구들입니다.",
        "가족은 우리의 첫 번째 집입니다.",
        "가족은 당신의 첫 번째 사랑의 원천입니다.",
        "가족은 당신의 가장 큰 보물입니다.",
        "가족은 당신이 결코 혼자가 아니라는 것을 상기시켜줍니다.",
        "가족은 세상에서 가장 강한 연결 고리입니다.",
        "가족은 우리가 세상을 살아가는 이유입니다.",
        "가족은 당신이 어떤 상황에서도 믿을 수 있는 사람들입니다.",
        "가족은 인생의 가장 큰 선물입니다.",
        "가족은 우리가 돌아갈 수 있는 가장 따뜻한 곳입니다.",
        "가족은 당신을 받아주는 첫 번째 사람들이자, 끝까지 남는 사람들입니다.",
        "가족은 세상에서 가장 위대한 선물입니다.",
        "가족은 인생에서 가장 소중한 것들 중 하나입니다.",
        "가족은 우리의 가장 큰 힘의 원천입니다.",
        "가족은 우리가 보호받는 유일한 곳입니다.",
        "가족은 우리가 돌아갈 수 있는 가장 안전한 곳입니다.",
        "가족은 우리의 첫 번째 사랑의 교사입니다.",
        "가족은 우리의 첫 번째 기쁨의 원천입니다.",
        "가족은 우리의 첫 번째 힘의 원천입니다.",
        "가족은 인생에서 가장 큰 축복입니다.",
        "가족은 우리가 어떤 일이 있어도 지켜줄 사람들입니다.",
        "가족은 세상에서 가장 큰 행복의 원천입니다.",
        "가족은 인생에서 가장 중요한 것입니다.",
        "가족은 우리가 세상에서 가장 큰 행복을 찾을 수 있는 곳입니다.",
        "가족은 우리의 첫 번째 위안의 원천입니다.",
        "가족은 우리의 첫 번째 지원군입니다.",
        "가족은 우리가 세상을 살아갈 이유입니다.",
        "가족은 우리의 첫 번째 사랑의 예입니다.",
        "가족은 우리의 첫 번째 기쁨의 예입니다.",
        "가족은 우리가 보호받을 수 있는 첫 번째 사람들입니다.",
        "가족은 세상에서 가장 큰 기쁨의 원천입니다.",
        "가족은 인생에서 가장 소중한 보물입니다.",
        "가족은 우리의 첫 번째 지지자들입니다.",
        "가족은 우리가 결코 혼자가 아니라는 것을 상기시켜줍니다.",
        "가족은 우리의 첫 번째 사랑의 근원입니다.",
        "가족은 세상에서 가장 큰 사랑의 원천입니다.",
        "가족은 인생에서 가장 중요한 것입니다.",
        "가족은 우리가 세상에서 가장 큰 기쁨을 찾을 수 있는 곳입니다.",
        "가족은 우리의 첫 번째 행복의 원천입니다.",
        "가족은 우리의 첫 번째 보호막입니다.",
        "가족은 세상에서 가장 큰 축복의 원천입니다.",
        "가족은 인생에서 가장 중요한 연결 고리입니다.",
        "가족은 우리가 어떤 상황에서도 믿을 수 있는 사람들입니다.",
        "가족은 우리의 첫 번째 사랑의 기둥입니다.",
        "가족은 우리가 세상을 살아갈 수 있는 이유입니다.",
        "가족은 우리의 첫 번째 우정의 장소입니다.",
        "가족은 세상에서 가장 강력한 지원군입니다.",
        "가족은 우리가 어떤 일이 있어도 돌아갈 수 있는 곳입니다.",
        "가족은 우리의 첫 번째 사랑의 지지자들입니다.",
        "가족은 세상에서 가장 큰 사랑의 예입니다.",
        "가족은 우리가 세상을 살아가는 데 필요한 사랑의 원천입니다.",
        "가족은 우리의 첫 번째 행복의 장소입니다.",
        "가족은 세상에서 가장 큰 기쁨의 근원입니다.",
        "가족은 우리의 첫 번째 보호자들입니다.",
        "가족은 세상에서 가장 큰 위안의 원천입니다.",
        "가족은 우리의 첫 번째 사랑의 보호자입니다.",
        "가족은 세상에서 가장 큰 힘의 원천입니다.",
        "가족은 우리의 첫 번째 기쁨의 보호막입니다.",
        "가족은 세상에서 가장 큰 행복의 근원입니다.",
        "가족은 우리의 첫 번째 사랑의 장소입니다.",
        "가족은 세상에서 가장 큰 지지자들입니다.",
        "가족은 우리의 첫 번째 보호의 기둥입니다.",
        "가족은 세상에서 가장 큰 축복의 기둥입니다.",
        "가족은 우리의 첫 번째 사랑의 지지군입니다.",
        "가족은 세상에서 가장 큰 행복의 보호자들입니다.",
        "가족은 우리의 첫 번째 위안의 기둥입니다.",
        "가족은 세상에서 가장 큰 사랑의 지지자들입니다.",
        "가족은 우리의 첫 번째 행복의 지지자들입니다.",
        "가족은 세상에서 가장 큰 기쁨의 보호막입니다.",
        "가족은 우리의 첫 번째 사랑의 지지자들입니다.",
        "가족은 세상에서 가장 큰 보호의 기둥입니다.",
        "가족은 우리의 첫 번째 사랑의 원천입니다.",
        "가족은 세상에서 가장 큰 행복의 장소입니다.",
        "가족은 우리의 첫 번째 보호막입니다.",
        "가족은 세상에서 가장 큰 축복의 보호자들입니다.",
        "가족은 우리의 첫 번째 행복의 장소입니다.",
        "가족은 세상에서 가장 큰 사랑의 보호막입니다.",
        "가족은 우리의 첫 번째 기쁨의 지지자들입니다.",
        "가족은 세상에서 가장 큰 행복의 지지자들입니다."
    ];


    const advices = [
        "함께 저녁 식사를 준비해보세요.",
        "집 근처 공원에서 산책을 즐기세요.",
        "가족 사진을 찍고 앨범을 만들어보세요.",
        "집에서 영화 밤을 계획하세요.",
        "가족과 함께 보드 게임을 즐기세요.",
        "집에서 쿠키나 케이크를 구워보세요.",
        "함께 방을 꾸며보세요.",
        "추억을 담은 사진을 보며 대화하세요.",
        "가족의 취미를 함께 배워보세요.",
        "함께 그림을 그리거나 색칠을 해보세요.",
        "퍼즐을 함께 맞춰보세요.",
        "가족 대화를 위한 질문지를 만들어보세요.",
        "서로를 위한 작은 선물을 만들어보세요.",
        "함께 도서관을 방문해보세요.",
        "가족이 좋아하는 책을 읽고 토론하세요.",
        "함께 노래방에서 노래를 불러보세요.",
        "가족 요가나 스트레칭을 해보세요.",
        "함께 집을 청소하거나 정리해보세요.",
        "가족 영화제작 프로젝트를 해보세요.",
        "옛날 가족 비디오를 함께 시청하세요.",
        "함께 자전거를 타고 외출해보세요.",
        "정원을 가꾸거나 식물을 심어보세요.",
        "함께 일기를 쓰고 공유해보세요.",
        "저녁에 별을 관찰해보세요.",
        "가족 인터뷰를 해보세요.",
        "가족 여행 계획을 세워보세요.",
        "함께 새로운 레시피를 시도해보세요.",
        "가족과 함께 자선 활동에 참여하세요.",
        "함께 운동을 해보세요.",
        "하루 동안 가족의 핸드폰을 끄고 지내보세요.",
        "함께 새로운 언어를 배워보세요.",
        "가족 사진 대회를 열어보세요.",
        "함께 장난감을 정리해보세요.",
        "서로에게 편지를 써보세요.",
        "서로의 장점을 칭찬하는 시간을 가져보세요.",
        "집에서 캠핑을 해보세요.",
        "저녁에 가족과 함께 춤을 춰보세요.",
        "가족의 취미를 서로에게 소개해보세요.",
        "집에서 스파나 마사지 세션을 해보세요.",
        "함께 여행 다큐멘터리를 시청하세요.",
        "가족과 함께 DIY 프로젝트를 해보세요.",
        "함께 자원봉사를 계획해보세요.",
        "가족과 함께 낚시를 시도해보세요.",
        "가족 회의 시간을 가져보세요.",
        "서로의 꿈이나 목표를 공유해보세요.",
        "가족과 함께 농장에서 체험 활동을 해보세요.",
        "가족의 전통 음식을 만들어보세요.",
        "가족과 함께 미술관이나 박물관을 방문하세요.",
        "함께 추억의 장소를 방문해보세요.",
        "저녁에 가족과 함께 그림자 놀이를 해보세요.",
        "집에서 가족 극장을 만들어보세요.",
        "서로의 버킷리스트를 작성해보세요.",
        "함께 음악을 만들어보세요.",
        "가족과 함께 자연 체험을 해보세요.",
        "동네에서 보물찾기를 해보세요.",
        "가족과 함께 찜질방이나 사우나를 가보세요.",
        "가족과 함께 노래를 만들어보세요.",
        "함께 집 안에서 탐험 놀이를 해보세요.",
        "각자 좋아하는 영화를 추천해보세요.",
        "가족의 애완동물을 위한 시간을 가져보세요.",
        "함께 스크랩북을 만들어보세요.",
        "가족과 함께 과거의 사진을 재현해보세요.",
        "아침 식사를 함께 준비해보세요.",
        "가족과 함께 간단한 과학 실험을 해보세요.",
        "함께 물놀이를 즐겨보세요.",
        "함께 저녁에 촛불을 켜고 대화해보세요.",
        "가족과 함께 간단한 도전 과제를 해보세요.",
        "함께 신문이나 잡지를 만들어보세요.",
        "가족과 함께 지역 행사에 참여해보세요.",
        "함께 벼룩시장이나 중고 서점에 가보세요.",
        "가족과 함께 전시회를 방문해보세요.",
        "가족과 함께 도보 여행을 해보세요.",
        "함께 마을의 명소를 탐방해보세요.",
        "가족과 함께 요리를 서로 가르쳐보세요.",
        "함께 지역 축제에 참여해보세요.",
        "가족과 함께 쿠킹 클래스에 참가해보세요.",
        "함께 마을의 도서관을 방문해보세요.",
        "가족과 함께 오리엔티어링을 해보세요.",
        "함께 간단한 수공예품을 만들어보세요.",
        "가족과 함께 모래성 쌓기 놀이를 해보세요.",
        "함께 텃밭을 가꾸어보세요.",
        "가족과 함께 새벽 조깅을 해보세요.",
        "함께 가족 일기를 써보세요.",
        "가족과 함께 패션쇼를 열어보세요.",
        "함께 가족 응원가를 만들어보세요.",
        "가족과 함께 하이킹을 가보세요.",
        "함께 페인트볼 게임을 해보세요.",
        "가족과 함께 일일 템플 스테이를 해보세요.",
        "함께 유기농 시장을 방문해보세요.",
        "가족과 함께 공방 체험을 해보세요.",
        "함께 지역 식당을 방문해보세요.",
        "가족과 함께 공연을 준비해보세요.",
        "함께 DIY 가구를 만들어보세요.",
        "가족과 함께 조깅을 해보세요.",
        "함께 도서관에서 책을 빌려 읽어보세요.",
        "가족과 함께 수영을 즐겨보세요.",
        "함께 지역 마을 탐방을 해보세요.",
        "가족과 함께 야외 바베큐를 해보세요.",
        "함께 자전거 타기를 해보세요.",
        "가족과 함께 동물원이나 아쿠아리움을 방문해보세요."
    ];


    const images = [
        "/seoyun/img/beach-4182974_1280.jpg",
        "/seoyun/img/blanket-4972062_1280.jpg",
        "/seoyun/img/family-1404827_1280.jpg",
        "/seoyun/img/family-1517192_1280.jpg",
        "/seoyun/img/love-826936_1280.jpg",
        "/seoyun/img/pexels-dariaboymaha-1683975.jpg",
        "/seoyun/img/pexels-delcho-dichev-1124200-1129615.jpg",
        "/seoyun/img/pexels-elina-sazonova-1914982.jpg",
        "/seoyun/img/pexels-emma-bauso-1183828-2253879.jpg",
        "/seoyun/img/pexels-hannah-nelson-390257-1456951.jpg",
        "/seoyun/img/pexels-ingo-1694649.jpg",
        "/seoyun/img/pexels-panditwiguna-1128316.jpg",
        "/seoyun/img/pexels-panditwiguna-1128318.jpg",
        "/seoyun/img/pexels-pixabay-236164.jpg",
        "/seoyun/img/pexels-pixabay-39691.jpg",
        "/seoyun/img/pexels-pixabay-160994.jpg",
        "/seoyun/img/pexels-pixabay-236164.jpg",
        "/seoyun/img/pexels-pixabay-302083.jpg",
        "/seoyun/img/picnic-6957307_1280.jpg",
        "/seoyun/img/team-spirit-2447163_1280.jpg"

    ];


    // Function to get a random element from an array
    function getRandomElement(arr) {
        return arr[Math.floor(Math.random() * arr.length)];
    }

    // Set a random image
    document.getElementById('animalImage').src = getRandomElement(images);

    // Set a random quote
    document.getElementById('quote').textContent = getRandomElement(quotes);

    // Set a random advice
    document.getElementById('advice').textContent = getRandomElement(advices);
});




