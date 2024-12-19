document.getElementById('expandButton').addEventListener('click', function () {
    var videoBox = document.getElementById('videoBox');
    var video = document.getElementById('myVideo');
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



