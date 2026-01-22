######################################################
## Build
FROM eclipse-temurin:25-jammy as build


RUN     mkdir /tmp/build

WORKDIR /tmp/build

COPY . /tmp/build

RUN     cd api.adoptium.net && \
        ./mvnw clean install -Padoptium,-adoptopenjdk && \
        cd ../ && \
        mkdir -p /api/deployment && \
        ./mvnw clean install && \
        cp adoptium-marketplace-server/adoptium-marketplace-updater-parent/adoptium-marketplace-updater/target/adoptium-marketplace-updater-*-jar-with-dependencies.jar /api/deployment/updater.jar && \
        cp adoptium-marketplace-server/adoptium-marketplace-frontend-parent/adoptium-marketplace-frontend/target/adoptium-marketplace-frontend-*-jar-with-dependencies.jar /api/deployment/frontend.jar && \
        cp docker/run.sh /api/deployment/ && \
        chmod +x /api/deployment/run.sh && \
        cd /tmp && \
        rm -rf /tmp/build ~/.m2 && \
        cd /api/ && find


######################################################
## Build Deployment
FROM eclipse-temurin:25-jammy

RUN     apt-get update && apt-get -y install openssl adduser && \
        useradd -ms /bin/bash api && \
        mkdir -p /logs

COPY --chown=api:api --from=build /api/deployment /home/api/deployment

USER api

WORKDIR /home/api/deployment/

ENV JAVA_OPTS=""
ARG type=frontend
ENV typeEnv=$type

CMD ./run.sh ${typeEnv}

