import os
import boto3
import re
import matplotlib.pyplot as plt
from collections import Counter
import sys
from matplotlib import font_manager, rc
import platform
import numpy as np

# AWS S3 설정
s3_client = boto3.client('s3')

# 기본 폰트 설정
plt.rcParams['font.family'] = 'DejaVu Sans'  # 기본 폰트 (이모티콘 지원)
plt.rcParams['axes.unicode_minus'] = False


# S3에서 텍스트 파일 가져오기
def get_transcript_from_s3(bucket_name, key):
    try:
        obj = s3_client.get_object(Bucket=bucket_name, Key=key)
        transcript_content = obj['Body'].read().decode('utf-8')
        return transcript_content
    except Exception as e:
        print(f"S3에서 텍스트를 가져오는 중 오류 발생: {e}")
        sys.exit(1)


# 감정 분석 및 그래프 생성
def generate_emotion_chart(transcript_content, participant, output_path):
    # 감정 추출
    pattern = re.compile(f"{participant}:.*?\\((.*?)\\)")
    emotions = pattern.findall(transcript_content)

    # 감정 빈도 계산
    emotion_counts = Counter(emotions)

    # 이모지 매핑
    emoji_map = {
        '화남': '😠',  # 화남
        '두려움': '😱',  # 두려움
        '슬픔': '😭',  # 슬픔
        '놀람': '😮',  # 놀람
        '행복': '😀',  # 행복
        '보통': '😑'  # 보통
    }

    # 색상 설정 (각 감정별로 색상 정의)
    colors = {
        '화남': '#FF6347',  # Tomato
        '두려움': '#FF4500',  # OrangeRed
        '슬픔': '#4682B4',  # SteelBlue
        '놀람': '#FFD700',  # Gold
        '행복': '#32CD32',  # LimeGreen
        '보통': '#A9A9A9'  # DarkGray
    }

    # 원형 그래프 생성
    fig, ax = plt.subplots(figsize=(8, 8), facecolor='none')
    wedges, texts, autotexts = ax.pie(
        emotion_counts.values(),
        colors=[colors.get(emotion, '#D3D3D3') for emotion in emotion_counts.keys()],
        autopct='',  # 퍼센트 라벨 제거
        startangle=140,
        wedgeprops=dict(edgecolor='w')  # 각 원 조각에 흰색 테두리 추가
    )

    # 이모지 크기 설정
    fontsize = 40  # 이모지 크기 조정

    # 원 안에 이모지 추가
    for wedge, emotion in zip(wedges, emotion_counts.keys()):
        # 원 조각의 중심 좌표 계산
        theta = (wedge.theta2 - wedge.theta1) / 2 + wedge.theta1
        x = np.cos(np.deg2rad(theta)) * wedge.r * 0.6  # 중심 위치 계산
        y = np.sin(np.deg2rad(theta)) * wedge.r * 0.6

        # 이모지 추가 (중앙 위치 조정)
        emoji = emoji_map.get(emotion, emotion)
        plt.text(x, y, emoji, ha='center', va='center', fontsize=fontsize, color='black')

    # 배경을 투명하게 설정
    plt.gca().set_facecolor('none')
    ax.set_aspect('equal')

    # 그래프의 축과 레이블 제거
    plt.axis('off')

    plt.savefig(output_path, transparent=True, bbox_inches='tight', pad_inches=0)
    plt.close()


# Main function
if __name__ == "__main__":
    # 입력 인수 처리
    bucket_name = sys.argv[1]
    key = sys.argv[2]
    participant = sys.argv[3]  # 예: "참여자1"
    output_path = sys.argv[4]  # 그래프를 저장할 경로

    # S3에서 텍스트 파일 가져오기
    transcript_content = get_transcript_from_s3(bucket_name, key)

    # 감정 분석 및 그래프 생성
    generate_emotion_chart(transcript_content, participant, output_path)
