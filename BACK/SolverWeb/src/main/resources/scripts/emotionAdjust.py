import sys
import boto3
import openai
from dotenv import load_dotenv
import os

# .env 파일에서 환경 변수를 로드
load_dotenv()

# stdout의 인코딩을 UTF-8로 설정
sys.stdout.reconfigure(encoding='utf-8')

# AWS S3 클라이언트 생성
s3_client = boto3.client('s3')


# S3에서 파일 가져오기 함수
def get_file_from_s3(bucket_name, file_key):
    try:
        response = s3_client.get_object(Bucket=bucket_name, Key=file_key)
        dialogue = response['Body'].read().decode('utf-8')
        return dialogue
    except Exception as e:
        # 에러가 발생한 경우만 stderr로 출력
        sys.stderr.write(f"Error fetching file from S3: {e}\n")
        sys.exit(1)


# GPT API 키 설정 (환경 변수에서 가져오기)
openai_api_key = os.getenv('OPENAI_API_KEY')
openai.api_key = openai_api_key


# OpenAI 요청 처리 함수
def process_dialogue(dialogue):
    try:
        response = openai.ChatCompletion.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "You are a helpful assistant specializing in conflict resolution."},
                {"role": "user", "content": dialogue},
                {"role": "user", "content": """
위 갈등 대화 내용을 바탕으로
대화내용을 한줄 요약을 해주고 감정 분석을 간략하게 한줄로 해줘. 그리고 너가 대화자들간에 서로의 감정을 이해하고 배려하면서 어떻게 대화하면 좋을지 서로의 감정에 대한 충고도 한줄로 해줘.
충고는 마지막 말에 "~~ 하면 어떨까요?"를 붙여서 라는 권유하는 형식으로 충고하고 끝내줘. 이건 너가 충고할때 쓰는 형식이야. 충고도 한줄로 써줘.
첫번째, 두번째 줄은 "~습니다" 대신 "~요" 체로 최대한 부드러운 어투로 써줘.
                """}
            ],
            max_tokens=400,
            temperature=0.7
        )
        return response.choices[0].message['content'].strip()
    except Exception as e:
        sys.stderr.write(f"Error processing dialogue with OpenAI: {e}\n")
        sys.exit(1)


# 메인 함수
if __name__ == "__main__":
    try:
        bucket_name = sys.argv[1]
        file_key = sys.argv[2]

        # S3에서 대화 파일 가져오기
        dialogue = get_file_from_s3(bucket_name, file_key)

        # OpenAI를 통한 감정 분석 및 결과 반환
        result = process_dialogue(dialogue)

        # 결과만 출력
        print(result)
    except Exception as e:
        sys.stderr.write(f"Error in main execution: {e}\n")
        sys.exit(1)
