// 주석 처리 - 주파수 스펙트로그램 관련 코드
let mediaRecorder;
let audioChunks = [];

const startBtn = document.getElementById('startBtn');
const recordBtn = document.getElementById('recordBtn');
const sendBtn = document.getElementById('sendBtn');
// const melSpectrogram = document.getElementById('melSpectrogram');

// 시나리오 시작 버튼 클릭 시 갈등 시나리오와 GPT 대화 가져오기
startBtn.addEventListener('click', () => {
    fetch('/scenario/getScenario')
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            document.getElementById('conflictScenario').innerText = data.conflictScenario;
            document.getElementById('gptConversation').innerText = data.conversation;
            document.getElementById('scenario').style.display = 'block';
        })
        .catch(error => console.error('Error:', error));
});

// 녹음 버튼 클릭 시 녹음 시작
recordBtn.addEventListener('click', async () => {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });

    mediaRecorder.start();
    audioChunks = [];

    alert("녹음이 시작되었습니다.");

    mediaRecorder.addEventListener('dataavailable', event => {
        audioChunks.push(event.data);
    });

    mediaRecorder.addEventListener('stop', async () => {
        const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });

        // Blob을 AudioBuffer로 변환 후 WAV로 변환
        const audioBuffer = await blobToAudioBuffer(audioBlob);
        const wavBlob = audioBufferToWavBlob(audioBuffer);

        const formData = new FormData();
        formData.append('audioFile', wavBlob, 'audio.wav'); // WAV 파일로 전송

        // STT 서버로 파일 전송
        try {
            const response = await fetch('/scenario/sc', {
                method: 'POST',
                body: formData,
                headers: {
                    'Cache-Control': 'no-cache' // 요청에 캐시 무시 지시
                }
            });
            const data = await response.json();

            if (data.error) {
                alert(data.error);
                return;
            }

            // STT 결과 표시
            document.getElementById('sttResult').innerHTML = `STT 결과: ${data.sttResult}`;

            // 이미지 로드 대기 및 확인
            // await loadImageWithCheck(data.melSpectrogram);
            // 피드백 표시
            document.getElementById('feedback').textContent = data.feedback;

        } catch (error) {
            console.error('Error:', error);
        }
    });
});

// 전송 버튼 클릭 시 녹음 중지
sendBtn.addEventListener('click', () => {
    if (mediaRecorder) {
        mediaRecorder.stop();
        alert("녹음이 종료되었습니다.");
    }
});

// 이미지 로드 확인 함수
// async function loadImageWithCheck(melSpectrogramPath) {
//     const imgContainer = document.getElementById('melSpectrogram').parentNode;
//
//     // 이전 이미지 제거
//     const oldImg = document.getElementById('melSpectrogram');
//     if (oldImg) {
//         imgContainer.removeChild(oldImg);
//     }
//
//     const newImg = document.createElement('img');
//     newImg.id = 'melSpectrogram';
//
//     // 캐시를 피하기 위한 타임스탬프 추가
//     const timestamp = new Date().getTime();
//     newImg.src = `${melSpectrogramPath}&t=${timestamp}`;
//     newImg.alt = 'FFT 스펙트로그램 이미지';
//
//     // 이미지 로드 대기
//     return new Promise((resolve, reject) => {
//         newImg.onload = function() {
//             console.log('이미지 로드 완료');
//             imgContainer.appendChild(newImg);  // 새 이미지를 추가
//             resolve();
//         };
//         newImg.onerror = function() {
//             console.error('이미지 로드 중 오류 발생');
//             reject(new Error('이미지 로드 오류'));
//         };
//     });
// }


// Blob을 AudioBuffer로 변환하는 함수
async function blobToAudioBuffer(blob) {
    const arrayBuffer = await blob.arrayBuffer();
    const audioContext = new AudioContext();
    return audioContext.decodeAudioData(arrayBuffer);
}

// AudioBuffer를 WAV 파일로 변환하는 함수
function audioBufferToWavBlob(audioBuffer) {
    const wavBuffer = audioBufferToWav(audioBuffer); // AudioBuffer를 WAV로 변환
    return new Blob([wavBuffer], { type: 'audio/wav' });
}

// AudioBuffer를 WAV로 변환하는 함수 (PCM 변환)
function audioBufferToWav(buffer) {
    let numOfChan = buffer.numberOfChannels,
        length = buffer.length * numOfChan * 2 + 44,
        bufferData = new ArrayBuffer(length),
        view = new DataView(bufferData);

    // RIFF 헤더
    writeString(view, 0, 'RIFF');
    view.setUint32(4, 36 + buffer.length * 2, true);
    writeString(view, 8, 'WAVE');

    // fmt 서브청크
    writeString(view, 12, 'fmt ');
    view.setUint32(16, 16, true);
    view.setUint16(20, 1, true); // PCM
    view.setUint16(22, numOfChan, true);
    view.setUint32(24, buffer.sampleRate, true);
    view.setUint32(28, buffer.sampleRate * 2, true); // byte rate
    view.setUint16(32, numOfChan * 2, true); // block align
    view.setUint16(34, 16, true); // bits per sample

    // data 서브청크
    writeString(view, 36, 'data');
    view.setUint32(40, buffer.length * 2, true);

    let offset = 44;
    for (let i = 0; i < buffer.length; i++) {
        for (let channel = 0; channel < numOfChan; channel++) {
            let sample = buffer.getChannelData(channel)[i];
            sample = Math.max(-1, Math.min(1, sample));
            view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true);
            offset += 2;
        }
    }

    return bufferData;
}

// 문자열을 DataView에 작성하는 함수
function writeString(view, offset, string) {
    for (let i = 0; i < string.length; i++) {
        view.setUint8(offset + i, string.charCodeAt(i));
    }
}
