# --- Stage 1: Build WAR with Maven ---
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -q -e clean package -DskipTests
# --- Stage 2: Run on Tomcat 10 ---
FROM tomcat:10.1-jdk17
WORKDIR /usr/local/tomcat
# Tắt cổng shutdown (ngăn spam log Render)
RUN sed -i 's/port="8005"/port="-1"/' conf/server.xml
# Xóa webapps mặc định
RUN rm -rf webapps/*
# Copy đúng WAR thành ROOT.war
COPY --from=build /app/target/demoSQL-1.0-SNAPSHOT.war ./webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
