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
                    cell.addEventListener('click', function () {
                        openPopup(capturedDate, capturedMonth, capturedYear);
                    });
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

    // 페이지 로드 시 초기 GPT 제목 데이터를 가져옴
    loadGptTitles(currentPage, pageSize);

    function loadGptTitles(page, size) {
        // startDate와 endDate가 빈 값일 때 기본 값 설정 (예: 오늘 날짜)
        if (!startDate) {
            startDate = "20240801"; // 기본 시작 날짜 (예시)
        }
        if (!endDate) {
            endDate = "20240831"; // 기본 종료 날짜 (예시)
        }

        $.ajax({
            url    : `/api/moreDashBoard/gpt-titles-by-date`,
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
                                     data-timestamp="20240824_144249">
                                <span>📌</span>
                                <span>${date} ${time}</span>
                                <span>${title}</span>
                            </div>
                        `;

                        dateListContainer.append(dateItemHtml);
                    });

                    // 상단의 날짜 범위 갱신
                    $("#date-range-display").text(formatDate(minDate) + " ~ " + formatDate(maxDate));
                } else {
                    dateListContainer.append("<div>데이터가 없습니다.</div>");
                }
            },
            error  : function (xhr, status, error) {
                console.error("GPT 제목 데이터를 가져오는 중 오류 발생: ", error);
                $("#date-list-container").append("<div>데이터를 가져오는 중 오류가 발생했습니다.</div>");
            }
        });
    }

    // 왼쪽 화살표 클릭 핸들러
    $("#next-date-range").on("click", function () {
        if (currentPage > 1) {
            currentPage--;
            loadGptTitles(currentPage, pageSize);
        }
    });

    // 오른쪽 화살표 클릭 핸들러
    $("#prev-date-range").on("click", function () {
        currentPage++;
        loadGptTitles(currentPage, pageSize);
    });

    function formatDate(dateString) {
        if (dateString.length === 8) {
            return dateString.substring(0, 4) + "." + dateString.substring(4, 6) + "." + dateString.substring(6, 8);
        }
        return dateString;
    }
});


// ********* 제목에 따른 감정 분석 결과 **************
$(document).ready(function () {
    $(document).on('click', '.gpt-title-item', function () {
        const gptFileKey = $(this).data('gpt-file-key'); // GPT 파일 키를 읽어옴
        fetchEmotionAnalysis(gptFileKey);
    });

    function fetchEmotionAnalysis(gptFileKey) {
        $.ajax({
            url    : `/api/moreDashBoard/fetch-emotion-analysis`,
            type   : 'GET',
            data   : {gptFileKey: gptFileKey}, // 타임스탬프 대신 GPT 파일 키를 보냄
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
});
