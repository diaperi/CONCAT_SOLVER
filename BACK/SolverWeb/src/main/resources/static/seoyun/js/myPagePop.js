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

    const overlay = document.getElementById("overlay");
    const popup = document.getElementById("popup");
    const closePopupButton = document.getElementById("close-popup");
    const popupDate = document.getElementById("popup-date");
    const popupVideo = document.getElementById("popup-video");
    const popupVideoSource = document.getElementById("popup-video-source");

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
                    cell.dataset.date = `${year}-${String(month + 1).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
                    if (j === 0) {
                        cell.classList.add("sunday");
                    }
                    if (date === currentDate && month === today.getMonth() && year === today.getFullYear()) {
                        cell.classList.add("today");
                    }

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

    const loggedInUserId = document.getElementById('loggedInUserId').value; // 숨겨진 input 요소에서 유저 ID 가져오기

    function openPopup(day, month, year) {
        // 날짜를 yyyyMMdd 형식으로 변환
        const dateStr = `${year}${String(month + 1).padStart(2, '0')}${String(day).padStart(2, '0')}`;
        popupDate.innerText = `${month + 1}월 ${day}일`;

        const encodedDate = encodeURIComponent(dateStr);
        const encodedUserId = encodeURIComponent(loggedInUserId);
        const requestUrl = `/myPage/getVideoByDate?date=${encodedDate}&userId=${encodedUserId}`;

        fetch(requestUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(video => {
                if (video && video.url) {
                    const popupVideoSource = document.getElementById('popup-video-source');
                    const popupVideo = document.getElementById('popup-video');

                    popupVideoSource.src = video.url;
                    popupVideo.load();

                    // 타임스탬프를 추출해 디테일 페이지 링크 생성
                    const match = video.url.match(/negative_emotion_(\d{8}_\d{6})_converted\.mp4/);
                    if (match && match[1]) {
                        const timestamp = match[1];
                        const detailUrl = `/myPage/myPageDetail?timestamp=${timestamp}`;

                        const link = document.createElement('a');
                        link.href = detailUrl;
                        link.innerText = popupDate.innerText;
                        link.style.textDecoration = 'none';
                        link.style.color = 'white';
                        link.style.cursor = 'pointer';
                        link.onmouseover = function () {
                            link.style.color = '#3084F4';
                        };
                        link.onmouseout = function () {
                            link.style.color = 'white';
                        };

                        popupDate.innerHTML = '';
                        popupDate.appendChild(link);

                        overlay.style.display = 'block';
                        popup.style.display = 'block';
                    } else {
                        alert("영상 URL에서 타임스탬프를 추출할 수 없습니다.");
                    }
                } else {
                    alert("해당 날짜에 대한 동영상을 찾을 수 없습니다.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("해당 날짜에 대한 동영상을 찾을 수 없습니다.");
            });
    }

    function closePopup() {
        overlay.style.display = 'none';
        popup.style.display = 'none';
        popupVideo.pause();
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

    closePopupButton.addEventListener('click', closePopup);
    overlay.addEventListener('click', closePopup);

    showCalendar(currentMonth, currentYear);
});
