FROM openjdk:23-slim-bookworm AS base

COPY docker/sources.list /etc/apt/sources.list
RUN apt update


FROM base AS builder

# Install maven
RUN apt install -y maven
COPY docker/settings.xml /usr/share/maven/conf/

# build java src
COPY . /app

WORKDIR /app
RUN mvn clean package -DskipTests=true

FROM base

# nginx
RUN apt install -y nginx
COPY docker/nginx.conf /etc/nginx/nginx.conf

# oauth2-server
COPY --from=builder /app/application.yml /app/
COPY --from=builder /app/README.md /app/
COPY --from=builder /app/web /app/web
COPY --from=builder /app/mock /app/mock

COPY --from=builder /app/target/lib /app/lib
COPY --from=builder /app/target/oauth2-server.jar /app/

COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# dumb-init
# https://github.com/Yelp/dumb-init
RUN apt install -y dumb-init

WORKDIR /app

RUN mkdir -p pem

EXPOSE 8000
EXPOSE 9080

ENTRYPOINT ["dumb-init", "--"]

CMD ["sh", "/app/entrypoint.sh"]
