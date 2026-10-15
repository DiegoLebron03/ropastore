# Stage 1: Compilación con Maven y JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copiar Maven Wrapper y pom.xml para aprovechar la caché de capas
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B || true

# Copiar código fuente y compilar JAR
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Imagen de ejecución ligera con JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios y directorio de uploads
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /app/uploads/productos && chown -R appuser:appgroup /app

# Copiar JAR compilado desde la etapa anterior
COPY --from=builder /workspace/target/*.jar /app/app.jar

USER appuser

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

