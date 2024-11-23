import os
import time
import boto3
import torch
import torch.nn as nn
import numpy as np
import librosa
from moviepy.editor import VideoFileClip
from collections import Counter
import tensorflow as tf
from transformers import AutoTokenizer, TFBertModel
import absl.logging
from datetime import datetime, timedelta
from datetime import datetime, timedelta, timezone
from pytz import utc
from dotenv import load_dotenv
import sys  # For accepting arguments

# 로그 레벨 설정
absl.logging.set_verbosity(absl.logging.ERROR)
sys.stdout.reconfigure(encoding='utf-8')

# 감정 목록 정의
emotions = ["Angry", "Disgust", "Fear", "Happiness", "Neutral", "Sadness", "Surprise"]
negative_emotions = ["Angry", "Disgust", "Fear", "Sadness"]
positive_emotions = ["Happiness"]

text_positive_emotions = ["행복"]
text_negative_emotions = ["두려움", "슬픔", "화남", "놀람"]


# CNN 기반 감정 분석 모델 정의
class EmotionCNN(nn.Module):
    def __init__(self, num_classes):
        super(EmotionCNN, self).__init__()
        self.conv1 = nn.Conv2d(1, 64, kernel_size=3, stride=1, padding=1)
        self.conv2 = nn.Conv2d(64, 128, kernel_size=3, stride=1, padding=1)
        self.conv3 = nn.Conv2d(128, 256, kernel_size=3, stride=1, padding=1)
        self.conv4 = nn.Conv2d(256, 512, kernel_size=3, stride=1, padding=1)
        self.pool = nn.MaxPool2d(2, 2)
        self.batch_norm = nn.BatchNorm2d(512)
        self.adaptive_pool = nn.AdaptiveAvgPool2d((8, 8))
        self.fc1 = nn.Linear(512 * 8 * 8, 512)
        self.fc2 = nn.Linear(512, num_classes)
        self.dropout = nn.Dropout(0.5)
        self.relu = nn.ReLU()

    def forward(self, x):
        x = self.pool(self.relu(self.conv1(x)))
        x = self.pool(self.relu(self.conv2(x)))
        x = self.pool(self.relu(self.conv3(x)))
        x = self.pool(self.relu(self.conv4(x)))
        x = self.batch_norm(x)
        x = self.adaptive_pool(x)
        x = x.view(x.size(0), -1)
        x = self.dropout(self.relu(self.fc1(x)))
        x = self.fc2(x)
        return x


# 모델 로드
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
emotion_model = EmotionCNN(num_classes=7)
emotion_model.load_state_dict(torch.load("D:/pythonProject/testProject/mel/emotion_cnn_model.pt", map_location=device))
emotion_model = emotion_model.to(device)
emotion_model.eval()

# KoBERT 모델 로드
SEQ_LEN = 64
tokenizer = AutoTokenizer.from_pretrained('monologg/kobert', trust_remote_code=True)
model_path = 'D:/pythonProject/testProject/integration/model/sentiment44.h5'


def custom_objects():
    return {'TFBertModel': TFBertModel}


try:
    with tf.keras.utils.custom_object_scope(custom_objects()):
        sentiment_model = tf.keras.models.load_model(model_path)
    print("KoBERT 모델 로드 성공")
except Exception as e:
    print(f"모델 로딩 오류: {e}")

# .env 파일 로드
load_dotenv()

# AWS 키와 리전 설정
aws_access_key_id = os.getenv('AWS_ACCESS_KEY_ID')
aws_secret_access_key = os.getenv('AWS_SECRET_ACCESS_KEY')
aws_region = os.getenv('AWS_REGION', 'ap-northeast-2')

sys.stdout.reconfigure(encoding='utf-8')

# S3 클라이언트 생성
s3_client = boto3.client(
    's3',
    aws_access_key_id=aws_access_key_id,
    aws_secret_access_key=aws_secret_access_key,
    region_name=aws_region
)

bucket_name = 'diaperiwinklebucket2'
# 동적으로 경로를 설정할 id_value
if len(sys.argv) < 2:
    print("사용자 ID를 입력해야 합니다.")
    sys.exit(1)
id_value = sys.argv[1]
video_prefix = f'{id_value}/videos/'
text_prefix = f'{id_value}/done/'


# 마지막 처리된 파일 저장소
def reset_processed_files():
    global processed_files
    processed_files = {
        "emotion_log": set(),
        "transcript": set(),
        "videos": set()
    }
    print("[LOG] `processed_files` 초기화 완료")


# 초기화: S3 버킷의 기존 파일을 처리된 파일로 간주
def initialize_processed_files():
    global processed_files
    now = datetime.now()
    try:
        # Emotion Log 초기화
        response_logs = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=text_prefix)
        if 'Contents' in response_logs:
            for obj in response_logs['Contents']:
                last_modified = obj['LastModified'].replace(tzinfo=None)
                if (now - last_modified) > timedelta(minutes=1):  # 최근 1분 초과된 파일만 처리
                    if "emotion_log" in obj['Key']:
                        processed_files["emotion_log"].add(obj['Key'])
                    if "transcript" in obj['Key']:
                        processed_files["transcript"].add(obj['Key'])

        # Videos 초기화
        response_videos = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=video_prefix)
        if 'Contents' in response_videos:
            for obj in response_videos['Contents']:
                last_modified = obj['LastModified'].replace(tzinfo=None)
                if (now - last_modified) > timedelta(minutes=1):  # 최근 1분 초과된 파일만 처리
                    processed_files["videos"].add(obj['Key'])

        # 디버깅 출력
        # print(f"[DEBUG] 초기 처리된 파일 목록 (emotion_log): {processed_files['emotion_log']}")
        # print(f"[DEBUG] 초기 처리된 파일 목록 (transcript): {processed_files['transcript']}")
        # print(f"[DEBUG] 초기 처리된 파일 목록 (videos): {processed_files['videos']}")

    except Exception as e:
        print(f"[ERROR] 파일 초기화 실패: {e}")


# 새로운 파일 감지
def get_new_files_with_pattern(s3_client, bucket_name, prefix, pattern, processed_set):
    try:
        now = datetime.now(timezone.utc)  # 현재 시간을 UTC로 설정
        response = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=prefix)
        if 'Contents' not in response:
            print(f"[LOG] S3에서 파일 없음: {prefix}")
            return []

        all_files = response['Contents']
        new_files = []

        for obj in all_files:
            last_modified = obj['LastModified']  # 이미 offset-aware datetime
            time_diff = now - last_modified  # 시간 차이 계산

            # 디버깅 로그
            # print(f"[DEBUG] 파일: {obj['Key']}, 수정 시간: {last_modified}, 시간 차이: {time_diff}")

            # 1분 이내 파일 필터링 및 처리되지 않은 파일만 선택
            if pattern in obj['Key'] and obj['Key'] not in processed_set and time_diff <= timedelta(minutes=1):
                new_files.append(obj)
                print(f"[DEBUG] 새로운 파일로 추가: {obj['Key']}")

        # 디버깅 로그 추가
        # print(f"[DEBUG] 처리된 파일 목록: {processed_set}")
        # print(f"[DEBUG] 새로운 파일 목록: {[obj['Key'] for obj in new_files]}")

        return new_files
    except Exception as e:
        print(f"[ERROR] 새로운 파일 감지 실패: {e}")
        return []


# 파일 다운로드 함수 수정 (파일 이름 고유화 적용)
def download_file_from_s3(s3_client, bucket_name, file_key, local_path):
    try:
        s3_client.download_file(bucket_name, file_key, local_path)
        print(f"다운로드 완료: {file_key}")
        return True
    except Exception as e:
        print(f"다운로드 실패: {e}")
        return False


# 파일 처리 후 즉시 업데이트
def process_emotion_log():
    global processed_files
    new_logs = get_new_files_with_pattern(s3_client, bucket_name, text_prefix, "emotion_log",
                                          processed_files["emotion_log"])
    for log in new_logs:
        processed_files["emotion_log"].add(log['Key'])  # 처리 완료 후 업데이트
        content = s3_client.get_object(Bucket=bucket_name, Key=log['Key'])['Body'].read().decode('utf-8')
        print(f"새로운 emotion_log: {log['Key']}")
        return analyze_emotions(content)
    return False


# 텍스트 감정 분석 함수
def analyze_emotions(file_content):
    lines = file_content.splitlines()
    emotions_extracted = [line.split(':')[-1].strip() for line in lines if ':' in line]
    emotion_counts = Counter(emotions_extracted)

    positive_count = sum(emotion_counts[emotion] for emotion in text_positive_emotions if emotion in emotion_counts)
    negative_count = sum(emotion_counts[emotion] for emotion in text_negative_emotions if emotion in emotion_counts)

    print(f"긍정 감정 개수: {positive_count}, 부정 감정 개수: {negative_count}")
    return negative_count > positive_count


# KoBERT 기반 텍스트 분석 수행
def process_transcript():
    global processed_files
    new_transcripts = get_new_files_with_pattern(s3_client, bucket_name, text_prefix, "transcript",
                                                 processed_files["transcript"])
    for transcript in new_transcripts:
        processed_files["transcript"].add(transcript['Key'])
        content = s3_client.get_object(Bucket=bucket_name, Key=transcript['Key'])['Body'].read().decode('utf-8')
        print(f"새로운 transcript: {transcript['Key']}")
        return evaluate_file(content)
    return False


# KoBERT 기반 텍스트 분석 수행 함수
def evaluate_file(file_content):
    sentences = file_content.splitlines()
    emotion_results = []

    for sentence in sentences:
        sentence = sentence.strip()
        if sentence:
            emotion = evaluation_predict(sentence)
            emotion_results.append(emotion)

    emotion_counter = Counter(emotion_results)
    final_emotion = emotion_counter.most_common(1)[0][0]

    return final_emotion == "부정"


def evaluation_predict(sentence):
    data_x = sentence_convert(sentence)
    predict = sentiment_model.predict(data_x, verbose=0)
    predict_value = predict[0][0]

    if predict_value < 0.5:
        return "부정"
    else:
        return "긍정"


# KoBERT 감정 분석 예측 함수
def sentence_convert(data):
    tokens, masks, segments = [], [], []
    token = tokenizer.encode(data, max_length=SEQ_LEN, truncation=True, padding='max_length')

    num_zeros = token.count(0)
    mask = [1] * (SEQ_LEN - num_zeros) + [0] * num_zeros
    segment = [0] * SEQ_LEN

    tokens.append(token)
    segments.append(segment)
    masks.append(mask)

    tokens = np.array(tokens)
    masks = np.array(masks)
    segments = np.array(segments)
    return [tokens, masks, segments]


# 동영상 파일 처리 함수 수정
def process_videos():
    global processed_files
    new_videos = get_new_files_with_pattern(s3_client, bucket_name, video_prefix, ".mp4", processed_files["videos"])
    # print(f"[DEBUG] 초기 처리된 파일 목록 (emotion_log): {processed_files['emotion_log']}")
    # print(f"[DEBUG] 초기 처리된 파일 목록 (transcript): {processed_files['transcript']}")
    # print(f"[DEBUG] 초기 처리된 파일 목록 (videos): {processed_files['videos']}")

    for video in new_videos:
        processed_files["videos"].add(video['Key'])
        local_path = f"./latest_video_{int(time.time())}.mp4"
        if download_file_from_s3(s3_client, bucket_name, video['Key'], local_path):
            audio_path = "./extracted_audio.wav"
            if extract_audio_from_mp4(local_path, audio_path):
                emotions = predict_emotions_for_long_audio(emotion_model, audio_path)
                return sum(e in negative_emotions for e in emotions) > len(emotions) / 2
    return False


# MP4 파일에서 오디오 추출
def extract_audio_from_mp4(mp4_file_path, output_audio_path):
    try:
        video = VideoFileClip(mp4_file_path)
        video.audio.write_audiofile(output_audio_path)
        print(f"오디오 추출 성공: {output_audio_path}")
        video.close()  # 파일 닫기
        return True
    except Exception as e:
        print(f"오디오 추출 실패: {e}")
        return False


# 긴 오디오 파일을 나누고 감정 예측
def predict_emotions_for_long_audio(model, audio_file_path, segment_duration=2.0):
    try:
        audio, sr = librosa.load(audio_file_path, sr=8000)
        total_duration = librosa.get_duration(y=audio, sr=sr)
        emotion_results = []

        for start in np.arange(0, total_duration, segment_duration):
            end = min(start + segment_duration, total_duration)
            audio_segment = audio[int(start * sr):int(end * sr)]
            predicted_emotion = predict_emotion(model, audio_segment)
            emotion_results.append(predicted_emotion)
        # 최종 예측된 모든 감정 출력
        print(f"[LOG] 예측된 모든 감정: {emotion_results}")
        return emotion_results
    except Exception as e:
        print(f"오디오 분석 실패: {e}")
        return []


# 오디오 파일을 멜스펙트로그램으로 변환하고 감정을 예측하는 함수
def predict_emotion(model, audio_segment):
    mel_spec = librosa.feature.melspectrogram(y=audio_segment, sr=8000, n_mels=128)
    mel_spec_db = librosa.power_to_db(mel_spec, ref=np.max)
    mel_spec_tensor = torch.tensor(mel_spec_db, dtype=torch.float32).unsqueeze(0).unsqueeze(0).to(device)

    with torch.no_grad():
        outputs = model(mel_spec_tensor)
        _, predicted = torch.max(outputs, 1)

    predicted_emotion = emotions[predicted.item()]
    # 감정 로그 추가
    print(f"[DEBUG] 모델 출력: {outputs}")
    print(f"[LOG] 예측된 감정: {predicted_emotion}")
    return predicted_emotion
    return predicted_emotion


def get_recent_s3_files(bucket_name, prefix, minutes=1):
    """S3 버킷에서 최근 n분 이내에 생성된 파일만 반환"""
    # 현재 로컬 시간(한국 시간)
    now = datetime.now(timezone.utc).astimezone()  # 현재 로컬 시간으로 변환
    recent_files = []

    try:
        response = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=prefix)
        if 'Contents' in response:
            for obj in response['Contents']:
                # S3 객체의 UTC 시간 반환
                modified_time = obj['LastModified']  # S3에서 UTC 시간 반환
                modified_time_local = modified_time.astimezone()  # 로컬 시간으로 변환

                # 시간 차이 계산
                time_difference = now - modified_time_local
                # print(f"[DEBUG] 파일: {obj['Key']}, 수정 시간: {modified_time_local}, 시간 차이: {time_difference}")

                # 조건에 따라 최근 파일로 간주
                if time_difference <= timedelta(minutes=minutes):
                    recent_files.append(obj['Key'])
    except Exception as e:
        print(f"[ERROR] S3 파일 감지 오류: {e}")

    return recent_files


# 갈등 판단
def check_conflict(video_result, text_result, bert_result):
    results = [video_result, text_result, bert_result]
    conflict_probability = sum(bool(result) for result in results) / len(results) * 100
    print(f"갈등 확률: {conflict_probability:.2f}%")
    if conflict_probability >= 50:
        print("갈등 상황입니다.")
    else:
        print("갈등 상황이 아닙니다.")


# 스케줄링
if __name__ == "__main__":
    # processed_files 초기화
    reset_processed_files()
    while True:
        print("[LOG] 새 파일 확인 중...")

        # S3에서 새 파일 확인
        new_video_files = get_recent_s3_files(bucket_name, video_prefix, minutes=1)
        new_emotion_logs = get_recent_s3_files(bucket_name, text_prefix, minutes=1)
        new_transcripts = get_recent_s3_files(bucket_name, text_prefix, minutes=1)

        video_result = None
        text_result = None
        bert_result = None

        # 새로운 동영상 파일이 있을 경우
        if new_video_files:
            print("[LOG] 새로운 동영상 파일 발견")
            video_result = process_videos()

        # 새로운 감정 로그 파일이 있을 경우
        if new_emotion_logs:
            print("[LOG] 새로운 emotion_log 파일 발견")
            text_result = process_emotion_log()

        # 새로운 대화 파일이 있을 경우
        if new_transcripts:
            print("[LOG] 새로운 transcript 파일 발견")
            bert_result = process_transcript()

        # 모든 파일 분석 결과가 준비되면 갈등 판단 실행 후 종료
        if video_result is not None or text_result is not None or bert_result is not None:
            check_conflict(video_result, text_result, bert_result)
            print("[LOG] 분석 완료. 프로그램 종료.")
            break  # 갈등 판단 후 루프 종료
        else:
            print("[LOG] 새로운 파일이 없어 갈등 판단을 건너뜁니다.")

        # 일정 시간 대기
        time.sleep(10)
