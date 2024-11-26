document.addEventListener('DOMContentLoaded', function () {
    const monthNames = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"];

    let today = new Date();
    let currentMonth = today.getMonth();
    let currentYear = today.getFullYear();
    let currentDate = today.getDate();

    const monthYear = document.getElementById("month-year");
    const calendarBody = document.querySelector("#calendar-body tbody");

    const prevButton = document.getElementById("prev");
    const nextButton = document.getElementById("next");

    // 날짜 클릭 이벤트 리스너 추가
    function addDayClickListeners() {
        document.querySelectorAll('#calendar-body td').forEach(day => {
            day.addEventListener('click', function () {
                const selectedDate = this.getAttribute('data-date').replace(/-/g, ''); // YYYYMMDD 형식으로 변환

                console.log(`Selected date: ${selectedDate}`);

                // Ajax 요청을 통해 서버에 데이터 전송
                $.ajax({
                    type       : "GET",
                    url        : "/api/moreDashBoard/process-dialogue",
                    data       : {date: selectedDate},
                    contentType: "application/json",
                    success    : function (response) {
                        console.log('Server response received:', response);

                        // 원본 텍스트 파일의 내용을 표시
                        const topSpan = document.querySelector('.moreDashBoard_down3_openBox_top_span');
                        const downSpan = document.querySelector('.moreDashBoard_down3_openBox_down_span');

                        if (response.original) {
                            topSpan.innerHTML = response.original.replace(/\n/g, "<br>");
                        } else {
                            topSpan.innerHTML = "원본 텍스트가 없습니다.";
                        }

                        if (response.result) {
                            downSpan.innerHTML = response.result.replace(/\n/g, "<br>");
                        } else {
                            downSpan.innerHTML = "결과를 처리하는 중 오류가 발생했습니다.";
                        }
                    },
                    error      : function (error) {
                        console.error('Error during Ajax request:', error);
                        alert('대화 재구성 중 오류가 발생했습니다.');
                    }
                });
            });
        });
    }

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
                    cell.dataset.date = `${year}-${String(month + 1).padStart(2, '0')}-${String(date).padStart(2, '0')}`; // 날짜 데이터 설정
                    if (j === 0) {
                        cell.classList.add("sunday");
                    }
                    if (date === currentDate && month === today.getMonth() && year === today.getFullYear()) {
                        cell.classList.add("today"); // Highlight today's date
                    }
                    date++;
                }
                row.appendChild(cell);
            }
            calendarBody.appendChild(row);
        }

        // 날짜 셀에 클릭 이벤트 리스너 추가
        addDayClickListeners();
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
const downElement = document.querySelector('.moreDashBoard_down');

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
    const element = document.querySelector('.moreDashBoard_top_left_top');
    if (element) {
        // 사용자가 스크롤할 때 애니메이션이 동작하도록 하기 위해 Intersection Observer를 사용할 수도 있습니다.
        // element.classList.add('fade-in-up');

        // 페이지 로드 시 애니메이션을 적용하려면 아래 코드 사용
        setTimeout(() => {
            element.classList.add('fade-in-up');
        }, 100); // 약간의 지연을 줘서 애니메이션이 부드럽게 시작되도록 설정
    }
});


$(document).ready(function () {
    let startDate = ""; // 초기 시작 날짜
    let endDate = ""; // 초기 종료 날짜
    let currentPage = 1; // 현재 페이지
    const pageSize = 5; // 한 페이지에 표시할 항목 수

    // 현재 날짜를 기본 값으로 설정
    function getDefaultDate() {
        const today = new Date();
        return today.toISOString().slice(0, 10).replace(/-/g, ""); // YYYYMMDD 형식
    }

    // 페이지 로드 시 초기 GPT 제목 데이터를 가져옴
    loadGptTitles(currentPage, pageSize);

    function loadGptTitles(page, size) {
        if (!startDate) startDate = getDefaultDate(); // 동적 기본 시작 날짜
        if (!endDate) endDate = getDefaultDate(); // 동적 기본 종료 날짜

        $.ajax({
            url    : getApiUrl(),
            type   : 'GET',
            data   : {
                startDate: startDate,
                endDate  : endDate,
                page     : page,
                size     : size
            },
            success: function (response) {
                const dateListContainer = $("#date-list-container");
                dateListContainer.empty(); // 기존 데이터를 비웁니다.

                if (response && response.length > 0) {
                    let minDate = response[0].date;
                    let maxDate = response[0].date;

                    response.forEach(function (item) {
                        const title = item.title || "제목 없음";
                        const date = item.date || "날짜 없음";
                        const time = item.time || "시간 없음";

                        // 날짜 범위를 계산하여 상단에 표시
                        if (date < minDate) minDate = date;
                        if (date > maxDate) maxDate = date;

                        const dateItemHtml = `
                            <div class="moreDashBoard_top_right5_dateList1 gpt-title-item"
                                 data-gpt-file-key="gpt_response_${date}_${time}.txt">
                                <span>-</span>
                                <span>${date} ${time}</span>
                                <span>${title}</span>
                            </div>
                        `;

                        dateListContainer.append(dateItemHtml);
                    });

                    // 상단의 날짜 범위 갱신
                    $("#date-range-display").text(formatDate(minDate) + " ~ " + formatDate(maxDate));
                } else {
                    dateListContainer.append("<div>해당 날짜 범위에 데이터가 없습니다. 다른 날짜를 선택하세요.</div>");
                }
            },
            error  : function (xhr, status, error) {
                console.error(`Error fetching GPT titles. Status: ${xhr.status}, Error: ${error}`);
                $("#date-list-container").append("<div>데이터를 가져오는 중 오류가 발생했습니다. 다시 시도해주세요.</div>");
            }
        });
    }

    // 화살표 클릭 핸들러
    $("#prev-date-range").on("click", function () {
        if (currentPage > 1) {
            currentPage--;
            loadGptTitles(currentPage, pageSize);
        } else {
            console.warn("Already at the first page.");
        }
    });

    $("#next-date-range").on("click", function () {
        currentPage++;
        loadGptTitles(currentPage, pageSize);
    });

    function formatDate(dateString) {
        if (dateString && dateString.length === 8) {
            return dateString.substring(0, 4) + "." + dateString.substring(4, 6) + "." + dateString.substring(6, 8);
        } else {
            console.error("Invalid date format:", dateString);
            return "날짜 없음";
        }
    }

    function getApiUrl() {
        return `/api/moreDashBoard/gpt-titles-by-date`;
    }
});


// ********* 제목에 따른 감정 분석 결과 **************
$(document).ready(function () {
    $(document).on('click', '.gpt-title-item', function () {
        const gptFileKey = $(this).data('gpt-file-key'); // GPT 파일 키 가져오기

        if (gptFileKey) {
            // 비동기 요청으로 emotion/async 컨트롤러 호출
            $.ajax({
                url    : "/moreDashBoard/emotion/async", // 비동기 메서드 URL
                type   : "GET",
                data   : {gptFileKey: gptFileKey}, // gptFileKey 전달
                success: function (response) {
                    // 서버 응답 처리
                    console.log("감정 분석이 성공적으로 수행되었습니다.");
                    updateEmotionAnalysisResult(response);
                },
                error  : function (xhr, status, error) {
                    console.error("감정 분석 요청 중 오류가 발생했습니다: ", error);
                }
            });
        } else {
            console.warn("gptFileKey가 비어 있습니다. 요청을 생략합니다.");
        }

        function updateEmotionAnalysisResult(response) {
            // 서버에서 반환된 데이터를 UI에 반영하는 로직 구현
            if (response.participant1EmotionChartImageUrl) {
                $('#participant1EmotionChart').attr('src', response.participant1EmotionChartImageUrl);
            }
            if (response.participant2EmotionChartImageUrl) {
                $('#participant2EmotionChart').attr('src', response.participant2EmotionChartImageUrl);
            }
            if (response.textEmotionAnalysisResult) {
                $('.moreDashBoard_top_right4 > span').html(response.textEmotionAnalysisResult);
            }
        }
    });


    function fetchEmotionAnalysis(gptFileKey) {
        $.ajax({
            url    : `/api/moreDashBoard/fetch-emotion-analysis`,
            type   : 'GET',
            data   : {gptFileKey: gptFileKey},
            success: function (response) {
                if (response && response.participant1Image && response.participant2Image) {
                    $('#participant1EmotionChart').attr('src', response.participant1Image);
                    $('#participant2EmotionChart').attr('src', response.participant2Image);
                } else {
                    console.error("감정 분석 결과를 가져오는 데 실패했습니다.");
                }
            },
            error  : function (xhr, status, error) {
                console.error("서버에서 감정 분석 결과를 가져오는 중 오류 발생:", error);
            }
        });
    }

    function formatDateFromText(dateTimeText) {
        // dateTimeText가 "YYYYMMDD HHmmss" 형식이라면, 이를 원하는 형식으로 포맷
        const date = dateTimeText.split(' ')[0]; // "YYYYMMDD" 부분만 추출
        return date.substring(0, 4) + "." + date.substring(4, 6) + "." + date.substring(6, 8);
    }
});


document.addEventListener('DOMContentLoaded', function () {
    const timestampElement = document.getElementById('formattedDate');
    let timestamp = timestampElement.textContent;

    // YYYYMMDD_HHmmss 형식을 YYYY.MM.DD 형식으로 변환
    if (timestamp && timestamp.length >= 8) {
        const formattedDate = timestamp.substring(0, 4) + '.' + timestamp.substring(4, 6) + '.' + timestamp.substring(6, 8);
        timestampElement.textContent = formattedDate;
    }
});


//********************* 명언, 충고, 이미지 배열 ************************
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
        "/seoyun/img/free-icon-dog-13749111.png",
        "/seoyun/img/free-icon-elderly-8217128.png",
        "/seoyun/img/free-icon-family-841196.png",
        "/seoyun/img/free-icon-family-2219802.png",
        "/seoyun/img/free-icon-family-2219867.png",
        "/seoyun/img/free-icon-family-2773062.png",
        "/seoyun/img/free-icon-family-3460565.png",
        "/seoyun/img/free-icon-family-3460694.png",
        "/seoyun/img/free-icon-family-3677058.png",
        "/seoyun/img/free-icon-family-4259760.png",
        "/seoyun/img/free-icon-family-4547175.png",
        "/seoyun/img/free-icon-family-5615775.png",
        "/seoyun/img/free-icon-family-picture-1316138.png",
        "/seoyun/img/free-icon-home-4891934.png",
        "/seoyun/img/free-icon-home-5615735.png",
        "/seoyun/img/free-icon-home-6581537.png",
        "/seoyun/img/free-icon-home-6581554.png",
        "/seoyun/img/free-icon-house-3677090.png"
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

