FROM gcr.io/distroless/java17-debian12:nonroot
LABEL org.opencontainers.image.source=https://github.com/dafnik/angular-todos-backend

# hack to get a mount point where the nonroot user is allowed to write
COPY --chown=nonroot:nonroot --chmod=660 docker/angular-todos /var/lib/angular-todos
COPY monolith/build/libs/monolith.jar server.jar
CMD ["-Djava.security.egd=file:/dev/urandom", "-Dspring.profiles.active=container", "server.jar"]
