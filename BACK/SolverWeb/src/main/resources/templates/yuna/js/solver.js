document.addEventListener('DOMContentLoaded', function() {
    // Scroll navigation
    const navAbout = document.querySelector('.navAbout');
    const solvernavAbout = document.querySelector('.solver_about');
    const navService = document.querySelector('.navService');
    const solverService = document.querySelector('.solver_mainService');
    const navReview = document.querySelector('.navReview');
    const solverReview = document.querySelector('.solver_wh');

    navAbout.addEventListener('click', function() {
        solvernavAbout.scrollIntoView({ behavior: 'smooth' });
    });

    navService.addEventListener('click', function() {
        solverService.scrollIntoView({ behavior: 'smooth' });
    });

    navReview.addEventListener('click', function() {
        solverReview.scrollIntoView({ behavior: 'smooth' });
    });

    // Button navigation
    document.getElementById('solver_loginBtn').addEventListener('click', function() {
        window.location.href = '../../yoonseo/login.html';
    });

    document.getElementById('solver_registerBtn').addEventListener('click', function() {
        window.location.href = '../../yoonseo/register.html';
    });

    document.getElementById('solver_registerBtn2').addEventListener('click', function() {
        window.location.href = '../../yoonseo/register.html';
    });

    // Swiper initialization
    var swiper = new Swiper(".mySwiper", {
        slidesPerView: 3,
        spaceBetween: 14,
        loop: false,
        freeMode: true,
        freeModeMomentum: true,
        navigation: {
            nextEl: ".swiper-button-next",
            prevEl: ".swiper-button-prev",
        },
    });

    document.querySelector('.swiper-button-next').addEventListener('click', function() {
        swiper.slideTo(swiper.slides.length - 1, 22000); 
    });

    document.querySelector('.swiper-button-prev').addEventListener('click', function() {
        swiper.slideTo(0, 22000);
    });

    // Number increment function
    function incrementNumber(elementId, maxNumber) {
        const element = document.getElementById(elementId);
        let currentNumber = 0;
        const increment = Math.ceil(maxNumber / 200); // Adjust the speed of the increment by changing 200

        const interval = setInterval(() => {
            currentNumber += increment;
            if (currentNumber >= maxNumber) {
                currentNumber = maxNumber;
                clearInterval(interval);
            }
            element.textContent = currentNumber.toLocaleString() + (elementId === 'investmentCount' ? '원' : '+');
        }, 10); // Adjust the interval time to speed up or slow down the effect
    }

    function startIncrementing() {
        incrementNumber('userCount', 99999);
        incrementNumber('downloadCount', 99999);
        incrementNumber('investmentCount', 99999);
    }

    // Number increment observer
    const numberObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                startIncrementing();
            }
        });
    }, {
        threshold: 0.5 // Adjust this value to determine when the effect starts (0.5 means 50% of the element is visible)
    });

    numberObserver.observe(document.querySelector('.solver_aboutBox'));

    // Animation observer
    const animationObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate');
            } else {
                entry.target.classList.remove('animate'); // Remove animation class to re-trigger on scroll
            }
        });
    }, {
        threshold: 0.1 // Trigger when 10% of the element is visible
    });

    document.querySelectorAll('.hidden').forEach(element => {
        animationObserver.observe(element);
    });

    // Header scroll effect
    var header = document.querySelector('.solver_headerContainer');
    var loginButton = document.querySelector('#solver_loginBtn');
    var registerButton = document.querySelector('#solver_registerBtn');
    window.addEventListener('scroll', function() {
        if (window.scrollY > 0) {
            header.classList.add('scrolled');
            header.style.backgroundColor = '#000'; 
            document.querySelectorAll('.solver_navMenu ul li').forEach(navItem => navItem.style.color = '#fff');
            if (loginButton && !loginButton.classList.contains('scrollHover')) {
                loginButton.style.color = '#000'; // Make login button text black
            }
            if (registerButton) {
                registerButton.style.backgroundColor = '#3084F4'; // Change background color to blue
                registerButton.style.color = '#fff'; // Change text color to white
            }
        } else {
            header.classList.remove('scrolled');
            header.style.backgroundColor = 'transparent';
            document.querySelectorAll('.solver_navMenu ul li').forEach(navItem => navItem.style.color = ''); // Reset to default color
            if (loginButton) {
                loginButton.style.color = '#3084F4'; // Default color for login button
                loginButton.style.borderColor = '#8b95a1'; 
            }
            if (registerButton) {
                registerButton.style.backgroundColor = '#fff'; // Default background color
                registerButton.style.color = '#000'; // Default text color
            }
        }
    });

    // Add hover effect when scrolled
    document.querySelectorAll('.solver_navMenu ul li').forEach(navItem => {
        navItem.addEventListener('mouseenter', function() {
            if (header.classList.contains('scrolled')) {
                navItem.style.color = '#3084F4'; // Blue color on hover
            }
        });
        navItem.addEventListener('mouseleave', function() {
            if (header.classList.contains('scrolled')) {
                navItem.style.color = '#fff'; // White color on mouse leave
            } else {
                navItem.style.color = ''; // Reset to default color
            }
        });
    });
});
