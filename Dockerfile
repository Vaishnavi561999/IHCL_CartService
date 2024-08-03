FROM gradle:7.1 as BUILD
RUN mkdir /app
COPY . /app/
WORKDIR /app
RUN chmod +x gradlew
RUN gradle installDist

FROM openjdk:11 as RUN

RUN mkdir -p /app/
RUN useradd -u 1000 tajhotels

COPY --from=BUILD /app/build/install/cart-service/ /app/
COPY ./agentlib/applicationinsights-agent-3.4.17.jar agent.jar
COPY ./agentlib/applicationinsights.json applicationinsights.json
ENV JAVA_OPTS="-javaagent:/agent.jar"
WORKDIR /app/bin
RUN chmod +x /app/bin/cart-service
EXPOSE 8081:8081
USER tajhotels
CMD ["./cart-service"]
