// 파일 삭제 요청 함수
function deleteFile(fileName) {
    if (confirm('정말로 이 파일을 삭제하시겠습니까?')) {
        fetch('/myPage/delete-file', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ fileName: fileName })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('파일이 삭제되었습니다.');
                    updateList(); // 삭제 후 목록 갱신
                } else {
                    alert('파일 삭제에 실패했습니다: ' + data.message);
                }
            })
            .catch(error => {
                console.error('삭제 요청 중 오류 발생:', error);
                alert('파일 삭제에 실패했습니다.');
            });
    }
}
// 목록 업데이트 함수
function updateList() {
    $.ajax({
        url: '/mypagemain/updateList',
        method: 'GET',
        success: function(data) {
            // 데이터를 받아와서 목록을 갱신
            $('#fileList').html(data);
        },
        error: function(xhr, status, error) {
            console.error('목록 업데이트 중 오류 발생:', error);
            alert('목록 업데이트에 실패했습니다.');
        }
    });
}


// expandButton 클릭 이벤트 핸들러
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

// deleteButton 클릭 이벤트
document.querySelectorAll('.deleteButton').forEach(function(button) {
    button.addEventListener('click', function() {
        var fileName = button.getAttribute('data-file-name');
        if (fileName) {
            deleteFile(fileName);
        } else {
            alert('파일 이름을 가져올 수 없습니다.');
        }
    });
});
