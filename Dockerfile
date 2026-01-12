FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# Compilar
RUN javac -cp ".:lib/sqlite-jdbc.jar" $(find . -name "*.java")

# Ejecutar
CMD ["java", "-cp", ".:lib/sqlite-jdbc.jar", "Main"]
