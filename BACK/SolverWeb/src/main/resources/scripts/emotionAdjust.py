import sys
import boto3
import openai

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
        print(f"Error fetching file from S3: {e}", file=sys.stderr)
        sys.exit(1)


# OpenAI API 키 설정
openai.api_key = 'sk-proj-VkwIsCz3wYHVv48cHSrpT3BlbkFJBurcyg1NTvLVnsCdby50'


# OpenAI 요청 처리 함수
def process_dialogue(dialogue):
    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are a helpful assistant specializing in conflict resolution."},
                {"role": "user", "content": dialogue},
                {"role": "user", "content": """
위 갈등 대화 내용을 바탕으로 
대화내용을 한줄 요약을 해주고 감정 분석을 간략하게 한줄로 해줘. 그리고 너가 대화자들간에 서로의 감정을 이해하고 배려하면서 어떻게 대화하면 좋을지 서로의 감정에 대한 충고도 한줄로 해줘. 
충고는 마지막 말에 "~~ 하면 어떨까요?"를 붙여서 라는 권유하는 형식으로 충고하고 끝내줘. 이건 너가 충고할때 쓰는 형식이야. 충고도 한줄로 써줘.
첫번째, 두번째 줄은 "~습니다" 대신 "~요" 체로 최대한 부드러운 어투로 써줘.
3줄을 합쳐서 한문단으로 보이게 해. 꼭 3줄만 써야 해.
                """}
            ],
            max_tokens=400,
            temperature=0.7
        )
        return response.choices[0].message['content'].strip()
    except Exception as e:
        print(f"Error processing dialogue with OpenAI: {e}", file=sys.stderr)
        sys.exit(1)


# 메인 함수
if __name__ == "__main__":
    try:
        bucket_name = sys.argv[1]
        file_key = sys.argv[2]

        dialogue = get_file_from_s3(bucket_name, file_key)
        result = process_dialogue(dialogue)

        print(result)
    except Exception as e:
        print(f"Error in main execution: {e}", file=sys.stderr)
        sys.exit(1)
