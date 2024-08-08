document.addEventListener('DOMContentLoaded', function() {
    const downBarButton = document.querySelector('.myPageMain_downBar');
    const targetSection = document.querySelector('.myPageMain_downBar');
    const upBarButton = document.querySelector('.myPageMain_UpBar');
    const targetSection2 = document.querySelector('.header_bar');

    downBarButton.addEventListener('click', function() {
        targetSection.scrollIntoView({ behavior: 'smooth' });
    });
    upBarButton.addEventListener('click', function() {
        targetSection2.scrollIntoView({ behavior: 'smooth' });
    });
});