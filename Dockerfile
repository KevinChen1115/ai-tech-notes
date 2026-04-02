# ============================================
# 第一階段：Build Stage（廚房）
# 用完整的 JDK + Maven 來編譯專案
# ============================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# 先複製 Maven Wrapper 和 pom.xml
# 為什麼不一次全複製？因為 Docker 有「層快取」機制：
# 只要這些檔案沒變，下次 build 就不用重新下載依賴，省很多時間
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 下載所有依賴（這一層會被快取）
RUN ./mvnw dependency:go-offline -B

# 再複製原始碼（這層最常變動，放最後）
COPY src/ src/

# 編譯打包，跳過測試（測試應該在 CI 階段跑，不是在 build image 時跑）
RUN ./mvnw package -DskipTests -B

# ============================================
# 第二階段：Run Stage（便當盒）
# 只用輕量的 JRE，不需要完整 JDK
# ============================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 從第一階段複製編譯好的 jar 檔（只拿便當，不搬廚房）
COPY --from=build /app/target/*.jar app.jar

# Spring Boot 預設跑在 8080 port
EXPOSE 8080

# 啟動應用
# -Xms256m：JVM 初始記憶體 256MB
# -Xmx512m：JVM 最大記憶體 512MB（在 t3.micro 1GB RAM 上留空間給 OS）
# -XX:+UseG1GC：使用 G1 垃圾回收器，適合小記憶體環境
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "app.jar"]
