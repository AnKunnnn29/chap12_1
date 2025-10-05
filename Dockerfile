# --- Stage 1: Build WAR with Maven ---
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Nếu dùng tests bị lỗi môi trường, có thể bỏ -DskipTests
RUN mvn -q -e clean package -DskipTests

# --- Stage 2: Run on Tomcat 10 ---
FROM tomcat:10.1-jdk17
# Xóa webapps mặc định để tránh ROOT mặc định của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*
# Đổi tên WAR của bạn thành ROOT.war để app chạy ở "/"
# Đúng tên file WAR do Maven tạo trong target/
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]