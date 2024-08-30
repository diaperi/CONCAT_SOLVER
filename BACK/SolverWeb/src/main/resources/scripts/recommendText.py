import sys
import openai
import logging

# stdout의 인코딩을 UTF-8로 설정
sys.stdout.reconfigure(encoding='utf-8')

# 로그 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# OpenAI API 키 설정
openai.api_key = 'sk-proj-VkwIsCz3wYHVv48cHSrpT3BlbkFJBurcyg1NTvLVnsCdby50'

# 표준 입력으로부터 원본 텍스트 읽기
try:
    dialogue = sys.stdin.read()  # UTF-8로 자동 디코딩
except Exception as e:
    logging.error(f"Error reading input: {e}")
    sys.exit(1)


# OpenAI 요청 처리 함수
def process_dialogue(dialogue):
    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "당신은 대화를 정중하고 공감하며 친절하게 재구성하는 데 능숙한 조력자입니다."},
                {"role": "user", "content": dialogue},
                {"role": "user", "content": """
위 대화에서 참여자들이 서로 배려하고 공감하며 존중하는 방식으로 말하도록 대화를 재구성해 주세요. 
받은 대화내용이 반말이면 반말로, 존댓말이면 존댓말로 유지해주세요. ( ) 에 있는 감정 표현을 제거하고, 전체적으로 긍정적이고 이해심 있는 착한 어조로 바꿔주세요.
                """}
            ],
            max_tokens=1500,
            temperature=0.5,
        )
        return response.choices[0].message['content'].strip()
    except Exception as e:
        logging.error(f"Error processing dialogue with OpenAI: {e}")
        sys.exit(1)


if __name__ == "__main__":
    try:
        result = process_dialogue(dialogue)
        print(result)  # 여기서는 인코딩된 데이터만 출력
    except Exception as e:
        logging.error(f"Error in main execution: {e}")
        sys.exit(1)
