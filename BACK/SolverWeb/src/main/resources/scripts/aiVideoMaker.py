# gpt_response_20241024_160258
# 10초마다 스케줄링 하면서 새로운 파일있는지 확인. 들어오면 코드 실행. 화면 gui 는 없애기.
import boto3
import os
import sys  # For accepting arguments
import time
from datetime import datetime
from playwright.sync_api import sync_playwright
from dotenv import load_dotenv

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

# S3 버킷 이름 설정
bucket_name = 'diaperiwinklebucket2'

# 동적으로 경로를 설정할 id_value
if len(sys.argv) < 2:
    print("사용자 ID를 입력해야 합니다.")
    sys.exit(1)
id_value = sys.argv[1]

# 마지막으로 처리한 파일의 키를 저장하는 변수
last_processed_file = None
last_processed_time = None


def initialize_last_processed_state():
    """
    스크립트 시작 시 S3의 가장 최신 파일 정보를 초기화
    """
    global last_processed_file, last_processed_time

    prefix = f"{id_value}/gpt/"
    try:
        response = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=prefix)
        if 'Contents' not in response:
            print("초기화: S3 버킷에 파일이 없습니다.")
            return

        # 파일 목록에서 최신 파일 찾기
        files = [{'Key': obj['Key'], 'LastModified': obj['LastModified']} for obj in response['Contents']]
        most_recent_file = max(files, key=lambda x: x['LastModified'])
        last_processed_file = most_recent_file['Key']
        last_processed_time = most_recent_file['LastModified']

        print(f"초기화 완료: 마지막 처리된 파일 설정 - {last_processed_file}, {last_processed_time}")
    except Exception as e:
        print(f"초기화 중 오류 발생: {e}")


def get_latest_text_from_s3(s3_client, bucket_name, id_value):
    """
    S3에서 가장 최신 텍스트 파일 가져오기
    """
    prefix = f"{id_value}/gpt/"
    try:
        response = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=prefix)
        if 'Contents' not in response:
            return None, None, None

        # 파일 목록에서 최신 파일 찾기
        files = [{'Key': obj['Key'], 'LastModified': obj['LastModified']} for obj in response['Contents']]
        most_recent_file = max(files, key=lambda x: x['LastModified'])
        file_key = most_recent_file['Key']
        last_modified = most_recent_file['LastModified']

        # S3에서 파일 내용 가져오기
        response = s3_client.get_object(Bucket=bucket_name, Key=file_key)
        content = response['Body'].read().decode('utf-8')

        return file_key, last_modified, content
    except Exception as e:
        print(f"S3에서 텍스트를 가져오는 중 오류 발생: {e}")
        return None, None, None


def upload_file_to_s3(file_path, bucket_name, s3_key):
    """
    파일을 S3에 업로드
    """
    try:
        with open(file_path, "rb") as file_data:
            s3_client.put_object(
                Bucket=bucket_name,
                Key=s3_key,
                Body=file_data,
                ContentType="video/mp4"  # 메타데이터 설정
            )
        print(f"파일이 S3에 성공적으로 업로드되었습니다: {s3_key}")
    except Exception as e:
        print(f"S3 업로드 중 오류 발생: {e}")


def generate_file_paths(latest_text_key, id_value):
    """
    로컬 및 S3 경로 생성
    """
    base_name = os.path.basename(latest_text_key)
    new_file_name = os.path.splitext(base_name)[0] + ".mp4"

    # 프로젝트 디렉토리 기준으로 경로 설정
    project_base_dir = os.path.dirname(os.path.abspath(__file__))  # 현재 파일 기준 프로젝트 루트
    local_file_path = os.path.join(project_base_dir, "output", new_file_name)  # output 디렉토리 사용

    s3_key = f"{id_value}/aiVideo/{new_file_name}"
    return local_file_path, s3_key


def extract_relevant_text(full_text):
    """
    전체 텍스트에서 > 뒤와 특정 문구 앞 내용을 추출하는 함수
    """
    start_marker = ">"  # 시작 지점
    end_marker = "참여자1에 대한 솔루션 입니다."  # 종료 지점

    start_index = full_text.find(start_marker)
    end_index = full_text.find(end_marker)

    if start_index == -1 or end_index == -1:
        print("시작 또는 종료 마커를 찾을 수 없습니다.")
        return None

    relevant_text = full_text[start_index + 1:end_index].strip()
    return relevant_text


def complete_video_processing(page, local_file_path, s3_key):
    """
    작업 페이지에서 비디오 생성 및 다운로드 작업 수행
    """
    try:
        # 언어 설정 드롭다운 클릭
        language_dropdown = page.locator("div.operate-border.language")
        language_dropdown.click()
        print("언어 설정 드롭다운 클릭 완료")
        page.wait_for_timeout(1000)

        # 드롭다운 메뉴에서 정확한 clone-voice-select 요소 선택
        dropdown_buttons = page.locator("div.clone-voice-select")
        dropdown_buttons.nth(0).click()
        print("첫 번째 드롭다운 버튼 클릭 완료")
        page.wait_for_timeout(1000)

        # 한국어 옵션 선택
        korean_option = page.locator("span.clone-voice-select-item__label:has-text('한국어 - KR')")
        korean_option.click()
        print("한국어 옵션 선택 완료")
        page.wait_for_timeout(1000)

        # YuJin 이름 선택
        try:
            yujin_option = page.locator("h5.clone-voice-card__title:has-text('YuJin')").nth(0)
            yujin_option.click()
            print("첫 번째 YuJin 이름 선택 완료")
            page.wait_for_timeout(1000)
        except Exception as e:
            print(f"YuJin 이름 선택 중 오류 발생: {e}")

        # 확인 버튼 클릭
        confirm_button = page.locator("div.vs-btn-ok")
        confirm_button.click()
        print("확인 버튼 클릭 완료")
        page.wait_for_timeout(1000)

        # 생성 버튼 클릭
        create_button = page.locator("div#box > div.main > span.text:has-text('생성')")
        create_button.click()
        print("생성 버튼 클릭 완료")

        # 새로운 페이지로 이동 후 로드 대기
        page.wait_for_url("https://aiapp-kr.vidnoz.com/video/index.html?slide=video", timeout=600000)
        page.wait_for_load_state("networkidle")
        print("생성 페이지 로드 완료")

        # "preview-box" 팝업 내의 다운로드 버튼 선택
        preview_popup = page.locator("div.preview-box")
        download_button = preview_popup.locator("button.download")
        download_button.wait_for(state="visible", timeout=600000)
        download_button.hover()
        print("다운로드 버튼 마우스 오버 완료")

        # 720p 다운로드 옵션 클릭
        download_720p_option = preview_popup.locator("ul.videolist-summary-download-list li:has-text('720p')")
        download_720p_option.wait_for(state="visible", timeout=90000)
        download_720p_option.click()
        print("다운로드 720p 선택 완료")

        # 다운로드 파일 저장
        download = page.wait_for_event("download", timeout=90000)
        download.save_as(local_file_path)
        print(f"파일이 로컬에 저장되었습니다: {local_file_path}")

        # S3에 업로드
        upload_file_to_s3(local_file_path, bucket_name, s3_key)

    except Exception as e:
        print(f"비디오 처리 중 오류 발생: {e}")


def process_new_file(latest_text_key, content):
    """
    새로 들어온 파일을 처리하는 함수
    """
    print(f"새 파일 발견: {latest_text_key}")
    print(f"파일 내용: {content}")

    # 텍스트에서 중요한 부분 추출
    extracted_text = extract_relevant_text(content)
    if not extracted_text:
        print("텍스트에서 유효한 데이터를 추출하지 못했습니다.")
        return

    # 로컬 및 S3 저장 경로 생성
    local_file_path, s3_key = generate_file_paths(latest_text_key, id_value)

    # Playwright 설정
    project_base_dir = os.path.dirname(os.path.abspath(__file__))  # 현재 파일 기준 프로젝트 루트
    user_data_dir = os.path.join(project_base_dir, "user_data")  # user_data 디렉토리

    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=user_data_dir,
            headless=False,  # GUI 없이 실행
            channel="chrome",
            args=["--disable-blink-features=AutomationControlled"]
        )

        try:
            page = browser.pages[0]
            print("작업 페이지로 이동 중...")
            page.goto("https://aiapp-kr.vidnoz.com/edit/?from=video&id=11926187", wait_until="domcontentloaded")
            text_area = page.locator("div.speech-textarea")
            if text_area.is_visible():
                text_area.click()
                text_area.fill(extracted_text)
                print("텍스트 입력 완료.")
                complete_video_processing(page, local_file_path, s3_key)
            else:
                print("텍스트 입력란이 보이지 않습니다. 작업을 중단합니다.")
        except Exception as e:
            print(f"비디오 생성 중 오류 발생: {e}")
        finally:
            browser.close()


def check_for_new_files():
    """
    10초마다 S3에서 새 파일이 있는지 확인
    """
    global last_processed_file, last_processed_time

    while True:
        print(f"[{datetime.now()}] 새 파일을 확인 중...", flush=True)  # 즉시 출력
        latest_text_key, last_modified, content = get_latest_text_from_s3(s3_client, bucket_name, id_value)

        if latest_text_key and (last_processed_time is None or last_modified > last_processed_time):
            last_processed_file = latest_text_key  # 마지막 처리한 파일 갱신
            last_processed_time = last_modified  # 마지막 처리한 시간 갱신
            print(f"[{datetime.now()}] 새 파일이 감지되었습니다: {latest_text_key}", flush=True)
            process_new_file(latest_text_key, content)  # 새 파일 처리
        else:
            print(f"[{datetime.now()}] 새 파일이 없습니다.", flush=True)

        time.sleep(10)  # 10초 대기


if __name__ == "__main__":
    print("S3 새 파일 감지 시스템 초기화 중...")
    initialize_last_processed_state()  # 시작 시 S3의 가장 최신 파일 정보를 기록
    print("감지 시스템 실행 중...")
    try:
        check_for_new_files()  # 새 파일 감지 시작
    except KeyboardInterrupt:
        print("프로그램 종료 중...")
