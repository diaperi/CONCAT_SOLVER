# CONCAT_SOLVER
멀티모달 감정인식모델과 ChatGPT를 통한 가족 갈등 해결 AI 스마트 홈캠
<br><br>

## 성과
👑 캡스톤 졸업작품 1위 대상
<br>
(2차심사 발표 자료)<br>
https://docs.google.com/presentation/d/1QF2_HgGSUXVy5FNGshKR1xfPqBW14Zqy/edit?usp=drivesdk&ouid=115287319250167358701&rtpof=true&sd=true
<br>

🎓 (Women in AI) AI4HER APAC Conference 발표
<br>
(컨퍼런스 발표 자료)<br>
https://docs.google.com/presentation/d/1RKoUel4Z5G0qxQvNuAo1SSfZCSM9Pj3g/edit?usp=drivesdk&ouid=115287319250167358701&rtpof=true&sd=true
<br><br>

## 🖥️ 프로젝트 소개
### 프로젝트 주제
본 프로젝트는, <strong>음성과 표정 및 텍스트 감정 기반 멀티 감정 인식 모델, ChatGPT 그리고 AI 비디오의 3가지 인공지능 기술</strong>을 종합적으로 활용하여 가정 내에서 발생하는 갈등 상황을 실시간으로 감지하고, 이를 분석하여 사용자에게 갈등을 해소할 수 있는 해결 방법을 제공한다. <br>
최종적으로 프로젝트는 다양한 인공지능 기술과 이미지, 영상, 오디오 처리 기술 그리고 주행 기술을 통해 사용자에게 갈등 해결 방법을 제공하여 가정 내 관계를 개선시키는 것을 목표로 한다.
<br><br>


### 목적 및 필요성
가족 구성원들 간의 상호 이해 부족이나 해결책의 부재,  
감정적으로 격한 상황에서는 갈등의 원인과 해결 방안을 명확하게 인식하기 어려움  
--> 갈등의 원인 제대로 이해X, 반복  
--> 비슷한 갈등이 다시 발생할 가능성↑  
==> 감정, 갈등 분석을 통해 갈등의 본질적인 원인을 찾아내고,  
이를 기반으로 구체적인 맞춤형 해결책을 제시함으로써 같은 문제의 재발을 방지하여 가족 관계의 개선
<br><br>


## ⏰ 개발 기간
24.04.01~24.11.26

<br><br>
## 🧑‍🦲 팀 구성
#### 팀원 1 서윤 (팀장) : 웹개발, 통합 및 형상 관리, 갈등&감정 분석 및 해결책 도출 기기 개발, DB, 표정 이미지/음성 톤 기반 감정인식모델 개발
#### 팀원 2 소연 : 웹 및 로고 디자인
#### 팀원 3 윤아 : 웹개발, 텍스트 단어 기반 감정인식모델 개발, DB
#### 팀원 4 혜은 : 웹개발
#### 팀원 5 윤서 : 풀스택, 객체 감지 장애물 회피 주행 기기 개발
<br><br>


## 🔦 개발 환경
#### Back-end
- Spring Boot
- Gradle, JPA
- Flask
- PostgreSQL
- supabase
- amazon AWS S3
- python
#### Front-end
- html, js, css
#### API
- Naver Colva Speech
- OpenCV
- Youtube API
- coolsms
- OpenAI GPT API
- Vidnoz(단순 사이트 활용)
#### 개발 & 협업 도구
- DBeaver
- git
- GitHub
- IntelliJ IDEA
- Colab
- Visual Studio Code
<br><br>
## 사용된 하드웨어
#### 감정/갈등 인식 분석 및 해결책 도출을 위한 장치
- 라즈베리파이5, 로지텍HD웹캠 C920<br>
#### 객체 감지 장애물 회피 주행
- 라즈베리파이4, 라이다센서RPLIDAR A1M5-R6, 라즈베리파이RC카 쉴드, DC모터, 웹카메라
<br><br>
## 🔑 8가지 주요 기능
- 실시간 감정 인식(음성, 표정, 단어)
- 멀티 모달 감정 인식 모델 통한 갈등 상황 파악
- GPT를 통한 맞춤형 갈등 해결 방법 해결책 제공
- 갈등 해결 방법 생성 알림 문자 서비스 제공
- 갈등 해결 방법 AI 영상, 참여자별 행동 가이드 제공
- 반성 및 학습을 위한 교육 웹페이지 (챗봇음성대화훈련/지난 갈등의 감정 통계/지난 갈등의 대화의 재구성)
<br><br>



## 🛠️ 주요 기술적 구현
### [갈등&감정 분석 기기]
#### 갈등 상황 실시간 감지 및 녹화
- 음성 톤, 얼굴 표정, 텍스트 기반 감정 분석 모델을 바탕으로 종합적으로 분석하여 갈등 상황을 파악.
- 실시간으로 변화하는 전체 감정 로그와 입 열림에 따른 감정 로그 기록.
- mp4 파일을 S3 데이터베이스에 각각의 사용자 ID 별로 저장.
- 연동된 웹에 실시간 영상 송출.<br>
![image](https://github.com/user-attachments/assets/b093eb29-6121-48de-85b3-09a8b2fce140)
![image](https://github.com/user-attachments/assets/9b769a9c-e648-4b5d-9b7b-9ec29973035d)
<br>
(-> 갈등 감지: 3가지 감정인식모델 활용)

#### stt 및 화자인식
- 음성을 텍스트로 변환.
- stt된 대화 내용을 화자인식으로 대화 참여자들을 각각 구분. 
- 각각의 발언을 정확히 인식하고, 누가 무엇을 말했는지 파악.
- 입 열림 감정 로그 데이터와 화자인식 된 데이터를 각 참여자에 맞게 매칭.<br>
![image](https://github.com/user-attachments/assets/78c16823-d677-496e-9f1d-fae24db634f1)

#### AI기반 해결방안 제시
- 사전에 GPT API에 적절한 해결책 요구 템플릿 생성.
- 감정&대화 데이터를 갈등 해결을 위한 GPT API에 입력.
- 출력 해결책 데이터 S3 데이터베이스에 저장.<br>
![image](https://github.com/user-attachments/assets/48446fd9-dc22-4b60-ae97-93488a0b6b6d)

#### AI 아바타가 설명해주는 AI 비디오 생성
- Vidnoz AI 플랫폼에서는 개발용 API 제공하지 않기 때문에 자체적으로 AI 영상 생성 자동화 로직 개발.
- S3에 있는 사용자 id 해결 방안 텍스트 파일 폴더 일정 시간 간격으로 탐색
- 새로운 파일 감지 시 AI 영상 생성 후 저장.
![image](https://github.com/user-attachments/assets/6c214dc6-7461-4138-98ba-3d369f525040)

#### 클라우드 및 데이터 저장소
- AWS S3: 녹화된 영상 및 분석된 데이터, 이미지, 텍스트 파일을 저장하고 관리하기 위한 클라우드 스토리지. 각 id별로 폴더 생성.
- 클라우드 데이터베이스: 사용자 정보 저장하기 위한 Supabase 데이터베이스 사용.
![image](https://github.com/user-attachments/assets/87fcd007-8a5f-4287-95be-8e28841aaec3)

### [객체 감지&장애물 회피 주행 기기]
#### 객체 감지(사람) 주행
- YOLO 알고리즘을 통해 객체(사람) 감지.
- 객체 감지 시 객체가 있는 방향으로 주행.
- 사람과 일정 거리 가까워지면 주행 기기 정지.
- 객체 미감지 시 회전하며 객체 찾음.<br>
![image](https://github.com/user-attachments/assets/c133d702-42b7-44c9-a82f-cc2ff5069545)

#### 장애물 회피 주행
- 라이다 센서를 이용해 장애물(벽)을 회피하며 주행

### [웹 플랫폼]
#### 저장된 갈등 영상 & 해결책 확인
- 사용자 ID에 따라 각각 저장된 갈등 영상과 해결책 확인
- 1. AI 아바타가 알려주는 갈등 해결 방법 영상
  2. 마음의 안정을 위한 '힐링' 키워드 영상/음악
  3. 참여자별 각각의 행동 가이드(TTS 제공)
  4. 갈등의 순간을 되돌아볼 수 있는 과거 갈등 영상<br>
![image](https://github.com/user-attachments/assets/3c857f97-f66c-47ce-8d07-174a7998088c)

#### 참여한 대화의 재구성
- 캘린더의 날짜를 선택하여 해당 날짜의 대화를 더 나은 방향으로 재구성  
→ 배려하는 대화 연습<br>
![image](https://github.com/user-attachments/assets/60a7623a-ed38-4c3b-925e-999846190639)

#### 참여한 대화의 감정통계
- 날짜 목록을 선택하여 해당 날짜의 대화의 감정 통계와 피드백 확인<br>
![image](https://github.com/user-attachments/assets/c96de8e2-4f13-4809-acdd-2627b7e9c24e)

#### 감정 음성 대화 패드백 챗봇
- 음성으로 챗봇과 대화하며 감정조절대화 훈련
→ 배려하며 말하기 방법 피드백 제공<br>
![image](https://github.com/user-attachments/assets/5c483c96-ed9a-4f97-bf9a-b4a6a8213d10)

#### 휴지통
- 영상 삭제되면 휴지통으로 이동
- 삭제된 영상 확인, 복구, 비우기 가능<br>
![image](https://github.com/user-attachments/assets/d277a098-2b83-4eae-983b-1438c73536ea)

#### 회원 기능
- 이메일 인증 회원가입(sns 회원가입 포함), 메시지 전송용 전화번호 인증, 로그인(sns 로그인 포함), 회원정보 수정, 아이디/비밀번호 찾기, 로그아웃, 회원 탈퇴 가능<br>
![image](https://github.com/user-attachments/assets/48de92e7-fdc3-4668-ba7f-e8a66fb40839)
![image](https://github.com/user-attachments/assets/f068a911-8a82-4430-a5d5-c2d1cf87e0cf)
![image](https://github.com/user-attachments/assets/7c6c9d13-70ef-4c84-aa6d-7d65ecf64310)
![image](https://github.com/user-attachments/assets/86d9fef7-5154-4967-b2e2-e828bb25bff0)

<br><br>


## 🤖 인공지능 모델 (Google COLAB 이용)
- 표정 이미지 기반 감정 분류 모델<br>
https://colab.research.google.com/drive/1vuM0N5g9EWdB9WhB8DzeilEuBk5qhQsP<br>
![image](https://github.com/user-attachments/assets/a4d2bfe6-a17b-4e11-8a66-b724f1b4ce21)

- 멜 스펙트로그램 기반 감정 분류 모델<br>
https://colab.research.google.com/drive/1CLXTmYyTlg5mW_78R7RGM8yCgz29scq2<br>
![image](https://github.com/user-attachments/assets/37d863f1-c8f9-4977-806e-78d778474957)

- 텍스트 기반 감정 분류 모델<br>
https://docs.google.com/document/d/1zRzcQoUG-ELiOuUhPlZOY0DNYo5wZVjzwly7XiSM7Oo/edit?usp=sharing<br>
![image](https://github.com/user-attachments/assets/2bf008ff-f08d-4863-b2e9-3da671343999)
<br><br>

## solver 웹 
http://solver.o-r.kr/

<br><br>

## ✍️ 회고
- 1년을 갈아넣은 캡스톤 졸업작품 회고<br>
https://seoyun-jung.tistory.com/118
- (Women in AI) AI4HER APAC Conference 에서 발표한 회고<br>
https://seoyun-jung.tistory.com/119
