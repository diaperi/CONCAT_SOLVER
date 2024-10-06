# Java 17 기반 이미지 사용 (필요에 따라 Java 버전을 조정)
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
VOLUME /tmp

# 빌드된 JAR 파일의 위치 지정 (생성된 JAR 파일 경로와 일치하게 수정)
ARG JAR_FILE=BACK/SolverWeb/build/libs/SolverWeb-0.0.1-SNAPSHOT.jar 

# JAR 파일을 컨테이너 내부로 복사
COPY ${JAR_FILE} app.jar

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]
