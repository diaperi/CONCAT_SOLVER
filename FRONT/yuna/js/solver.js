
document.addEventListener('DOMContentLoaded', function() {
    const navAbout = document.querySelector('.navAbout');
    const solvernavAbout = document.querySelector('.solver_about');
    const navService = document.querySelector('.navService');
    const solverService = document.querySelector('.solver_mainService');
    const navReview = document.querySelector('.navReview');
    const solverReview = document.querySelector('.solver_review');

    navAbout.addEventListener('click', function() {
        solvernavAbout.scrollIntoView({ behavior: 'smooth' });
    });

    navService.addEventListener('click', function() {
        solverService.scrollIntoView({ behavior: 'smooth' });
    });

    navReview.addEventListener('click', function() {
        solverReview.scrollIntoView({ behavior: 'smooth' });
    });

    document.getElementById('solver_loginBtn').addEventListener('click', function() {
        window.location.href = 'login.html';
    });

    document.getElementById('solver_registerBtn').addEventListener('click', function() {
        window.location.href = 'register.html';
    });

    document.getElementById('solver_registerBtn2').addEventListener('click', function() {
        window.location.href = 'register.html';
    });
});

var navAbout = document.querySelectorAll('.navAbout');
var loginButton = document.querySelector('#solver_loginBtn');
var registerButton = document.querySelector('#solver_registerBtn');

window.addEventListener('scroll', function() {
    var header = document.querySelector('.solver_headerContainer');

    if (window.scrollY > 0) {
        header.style.backgroundColor = '#000'; 
        navAbout.style.color = '#fff'; 
        navAbout.forEach(function(navAbout) {
            navAbout.style.color = '#fff';
        });
        if (!loginButton.classList.contains('scrollHover')) {
            // loginButton.style.color = '#000';
        }
        // registerButton.style.backgroundColor = '#3084F4'; 
        // registerButton.style.color = '#fff';
    } else {
        header.style.backgroundColor = 'transparent';
        loginButton.style.color = '#3084F4';
        loginButton.style.borderColor = '#8b95a1'; 
        // registerButton.style.backgroundColor = '#fff';
        // registerButton.style.color = '#3084F4';
    }
});

document.addEventListener('DOMContentLoaded', function() {
    var swiper = new Swiper(".mySwiper", {
        slidesPerView: 3,
        spaceBetween: 2,
        loop: false,
        freeMode: true,
        freeModeMomentum: true,
    });
});
