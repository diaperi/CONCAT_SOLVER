let mediaRecorder;
let audioChunks = [];

const startBtn = document.getElementById('startBtn');
const recordBtn = document.getElementById('recordBtn');
const sendBtn = document.getElementById('sendBtn');


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

            // 새로운 STT 결과와 피드백 요소 추가
            const scrollBox = document.querySelector('.scroll-box');

            // STT 결과 표시할 새로운 div 생성
            const newUserResponse = document.createElement('div');
            newUserResponse.classList.add('bubble2', 'userResponse');
            const newSttResult = document.createElement('div');
            newSttResult.classList.add('sttResult');
            newSttResult.innerHTML = `STT 결과: ${data.sttResult}`;
            newUserResponse.appendChild(newSttResult);

            // <피드백>과 <다음 대화>를 분리
            const feedbackText = data.feedback.split("\n\n")[0] || "<피드백 없음>";
            const nextConversationText = data.feedback.split("\n\n")[1] || "<다음 대화 없음>";

            // 피드백 표시할 새로운 div 생성
            const newFeedback = document.createElement('div');
            newFeedback.classList.add('bubble3', 'feedback');
            newFeedback.textContent = feedbackText;

            // 다음 대화 표시할 새로운 div 생성
            const newNextConversation = document.createElement('div');
            newNextConversation.classList.add('bubble', 'nextConversation');
            newNextConversation.textContent = nextConversationText;

            // scroll-box에 새로운 요소 추가
            scrollBox.appendChild(newUserResponse);
            scrollBox.appendChild(newFeedback);
            scrollBox.appendChild(newNextConversation);

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
