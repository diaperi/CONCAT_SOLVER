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
                    cell.dataset.date = `${year}-${String(month + 1).padStart(2, '0')}-${String(date).padStart(2, '0')}`; // 날짜 데이터 설정
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


    // function openPopup(day, month, year) {
    //     // 날짜를 올바르게 표시
    //     const dateStr = `${year}.${String(month + 1).padStart(2, '0')}.${String(day).padStart(2, '0')}`;
    //     popupDate.innerText = `${month + 1}월 ${day}일`;
    //
    //     // ************** 동영상 팝업 표시 **************
    //     fetch(`/myPage/getVideoByDate?date=${dateStr}`)
    //         .then(response => {
    //             console.log(`Response status: ${response.status}`);
    //             if (!response.ok) {
    //                 throw new Error(`HTTP error! Status: ${response.status}`);
    //             }
    //             return response.json();
    //         })
    //         .then(video => {
    //             console.log('Video data:', video);
    //             if (video && video.url) {
    //                 const popupVideoSource = document.getElementById('popup-video-source');
    //                 const popupVideo = document.getElementById('popup-video');
    //                 console.log('Setting video URL:', video.url);  // 콘솔 로그 추가
    //                 popupVideoSource.src = video.url;
    //                 popupVideo.load(); // Load the new video source
    //                 overlay.style.display = 'block';
    //                 popup.style.display = 'block';
    //             } else {
    //                 alert("해당 날짜에 대한 동영상을 찾을 수 없습니다.");
    //             }
    //         })
    //         .catch(error => {
    //             console.error('Error:', error);
    //             alert("해당 날짜에 대한 동영상을 찾을 수 없습니다.");
    //         });
    //
    //     // 팝업 표시
    //     overlay.style.display = 'block';
    //     popup.style.display = 'block';
    // }
    function openPopup(day, month, year) {
        // 날짜를 올바르게 표시
        const dateStr = `${year}.${String(month + 1).padStart(2, '0')}.${String(day).padStart(2, '0')}`;
        popupDate.innerText = `${month + 1}월 ${day}일`;

        // ************** 동영상 팝업 표시 **************
        fetch(`/myPage/getVideoByDate?date=${dateStr}`)
            .then(response => {
                console.log(`Response status: ${response.status}`);
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(video => {
                console.log('Video data:', video);
                if (video && video.url) {
                    const popupVideoSource = document.getElementById('popup-video-source');
                    const popupVideo = document.getElementById('popup-video');
                    const popupDate = document.getElementById('popup-date');

                    console.log('Setting video URL:', video.url);  // 콘솔 로그 추가
                    popupVideoSource.src = video.url;
                    popupVideo.load(); // Load the new video source

                    // Extract timestamp from video URL
                    const timestamp = video.url.match(/negative_emotion_(\d{8}_\d{6})_converted\.mp4/)[1];
                    const detailUrl = `/myPage/myPageDetail?timestamp=${timestamp}`;

                    // Wrap popupDate in an anchor tag with desired styles
                    const link = document.createElement('a');
                    link.href = detailUrl;
                    link.innerText = popupDate.innerText;
                    link.style.textDecoration = 'none';
                    link.style.color = 'white';
                    link.style.cursor = 'pointer';
                    link.style.transition = 'color 0.5s ease';
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
        popupVideo.pause(); // Pause the video when closing the popup
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
