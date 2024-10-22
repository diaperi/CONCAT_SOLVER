let mediaRecorder;
let audioChunks = [];

const startBtn = document.getElementById('startBtn');
const recordBtn = document.getElementById('recordBtn');
const sendBtn = document.getElementById('sendBtn');
const melSpectrogram = document.getElementById('melSpectrogram');

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

    // 녹음 시작 알림 메시지
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
        // STT 결과가 도착한 후 FFT 스펙트로그램 이미지 처리
        fetch('/scenario/sc', {
            method: 'POST',
            body: formData,
            headers: {
                'Cache-Control': 'no-cache' // 요청에 캐시 무시 지시
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    alert(data.error);
                    return;
                }

                // STT 결과 표시
                document.getElementById('sttResult').innerHTML = `STT 결과: ${data.sttResult}`;

                // FFT 스펙트로그램 이미지가 생성될 시간을 기다림 (예: 2초 대기)
                setTimeout(() => {
                    // 이미지가 생성되었는지 확인하고 처리
                    if (data.melSpectrogram) {
                        const imgContainer = document.getElementById('melSpectrogram').parentNode;
                        const oldImg = document.getElementById('melSpectrogram');

                        // 기존 이미지 태그가 있을 경우 삭제
                        if (oldImg) {
                            imgContainer.removeChild(oldImg);
                        }

                        // 새로운 이미지 태그 생성
                        const newImg = document.createElement('img');
                        newImg.id = 'melSpectrogram';
                        newImg.src = data.melSpectrogram;  // 타임스탬프 없이 이미지 바로 로드
                        newImg.alt = 'FFT 스펙트로그램 이미지';

                        // 새로운 이미지 태그를 DOM에 추가
                        imgContainer.appendChild(newImg);
                    } else {
                        document.getElementById('melSpectrogram').alt = 'FFT 스펙트로그램 이미지를 불러올 수 없습니다.';
                    }
                }, 5000);  // 5초 대기 후 이미지 전송 시도

                // 피드백을 네 번째 버블에 표시
                setTimeout(() => {
                    // 피드백 표시 코드
                    document.getElementById('feedback').textContent = data.feedback;
                }, 10000);  // 10초 대기

                // 디버깅: STT 결과와 피드백 로그 확인
                console.log("STT Result:", data.sttResult);
                console.log("Feedback:", data.feedback);
            })
            .catch(error => console.error('Error:', error));
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
