document.addEventListener("DOMContentLoaded", () => {
    const slider = document.querySelector(".slider");

    let isDragging = false;
    let startX = 0;
    let currentTranslate = 0;
    let prevTranslate = 0;

    const setSliderPosition = () => {
        slider.style.transform = `translateX(${currentTranslate}px)`;
    };

    const handleMouseDown = (e) => {
        isDragging = true;
        startX = e.clientX;
        slider.style.cursor = "grabbing";
    };

    const handleMouseMove = (e) => {
        if (!isDragging) return;
        const currentX = e.clientX;
        const deltaX = currentX - startX;
        currentTranslate = prevTranslate + deltaX;
        setSliderPosition();
    };

    const handleMouseUp = () => {
        isDragging = false;
        prevTranslate = currentTranslate;
        slider.style.cursor = "grab";
    };

    slider.addEventListener("mousedown", handleMouseDown);
    slider.addEventListener("mousemove", handleMouseMove);
    slider.addEventListener("mouseup", handleMouseUp);
    slider.addEventListener("mouseleave", handleMouseUp); // 마우스가 영역을 벗어나면 드래그 중단
});
