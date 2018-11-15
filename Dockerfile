FROM openjdk:8-jdk-alpine as runtime
WORKDIR /workspace/app
COPY runtime/runtime/target/runtime.jar .
RUN mkdir -p dependencies && (cd dependencies && jar xvf /workspace/app/runtime.jar)

FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/dependencies
COPY --from=runtime ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=runtime ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=runtime ${DEPENDENCY}/BOOT-INF/classes /app
COPY ui/dist/app /app/public
ENTRYPOINT ["java","-cp","app:app/lib/*","io.atlasmap.runtime.Application"]
