document.addEventListener('DOMContentLoaded', function () {
    const downBarButton = document.querySelector('.myPageMain_downBar');
    const upBarButton = document.querySelector('.myPageMain_UpBar');
    const targetSection2 = document.querySelector('.header_navbar');

    downBarButton.addEventListener('click', function () {
        window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});
    });

    upBarButton.addEventListener('click', function () {
        targetSection2.scrollIntoView({behavior: 'smooth'});
    });
});
