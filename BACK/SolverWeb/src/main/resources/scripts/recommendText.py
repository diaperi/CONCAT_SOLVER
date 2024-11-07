import sys
import logging
import requests
import json
import os
from dotenv import load_dotenv

# .env 파일에서 환경 변수를 로드
load_dotenv()

# stdout과 stdin의 인코딩을 UTF-8로 설정
sys.stdin = open(sys.stdin.fileno(), mode='r', encoding='utf-8', buffering=1)
sys.stdout = open(sys.stdout.fileno(), mode='w', encoding='utf-8', buffering=1)

# 로그 설정 (레벨을 WARNING으로 변경하고 출력 스트림을 stderr로 설정)
logging.basicConfig(level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s', stream=sys.stderr)

# OpenAI API Key 설정 (환경 변수에서 가져오기)
openai_api_key = os.getenv('OPENAI_API_KEY')

if openai_api_key is None:
    logging.error("OPENAI_API_KEY is not set in the environment variables.")
    sys.exit(1)

# 표준 입력으로부터 대화 내용 읽기
try:
    dialogue = sys.stdin.read().strip()
    # Java로부터 전달된 대화 내용을 로그로 출력 (INFO 레벨이므로 출력되지 않음)
    logging.info(f"Original dialogue received:\n{dialogue}")

except Exception as e:
    logging.error(f"Error reading input: {e}")
    sys.exit(1)


# OpenAI 요청 처리 함수
def process_dialogue(dialogue):
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {openai_api_key}'
    }

    # OpenAI API에 전달할 payload
    payload = {
        'model': 'gpt-4o',
        'messages': [
            {
                'role': 'system',
                'content': '당신은 대화를 정중하고 공감하며 친절하게 재구성하는 데 능숙한 조력자입니다.'
            },
            {
                'role': 'user',
                'content': dialogue
            },
            {
                'role': 'user',
                'content': """
위 대화에서 갈등이 일어나지 않도록 참여자들이 서로 배려하고 공감하며 존중하는 방식으로 말하도록 대화를 재구성해 주세요.
받은 대화내용이 반말이면 반말로, 존댓말이면 존댓말로 유지해주세요. ( )에 있는 감정 표현을 제거하고, 전체적으로 긍정적이고 이해심 있는 어조로 바꿔주세요.
                """
            }
        ],
        'max_tokens': 1500,
        'temperature': 0.5
    }

    try:
        # OpenAI API에 요청 보내기
        response = requests.post('https://api.openai.com/v1/chat/completions', headers=headers, json=payload)
        response.raise_for_status()

        # 응답 처리
        response_data = response.json()
        # OpenAI 응답에서 결과 추출
        return response_data['choices'][0]['message']['content'].strip()

    except requests.exceptions.RequestException as e:
        logging.error(f"Error processing dialogue with OpenAI: {e}")
        sys.exit(1)


# 메인 실행 부분
if __name__ == '__main__':
    try:
        # 받은 대화를 처리
        result = process_dialogue(dialogue)

        # 최종 결과 출력
        print(result)

    except Exception as e:
        logging.error(f"Error in main execution: {e}")
        sys.exit(1)
