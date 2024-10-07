document.getElementById("recordBtn").addEventListener("click", function() {
    alert("녹음을 시작합니다.");
});

document.getElementById("imgBtn").addEventListener("click", function() {
    alert("이미지가 첨부되었습니다.");
});

document.getElementById("sendBtn").addEventListener("click", function() {
    alert("녹음이 전송되었습니다.");
});

document.getElementById('startBtn').addEventListener('click', function() {
    // 클릭하면 갈등 상황과 대화창이 보이도록 설정
    document.getElementById('scenario').style.display = 'block';
});
