FROM rust:alpine3.22 AS build

# ENV HTTPS_PROXY=set proxy if needed

WORKDIR /app
RUN apk add --no-cache musl-dev vim curl

COPY . .
RUN cargo build

FROM alpine:3.22
WORKDIR /app

COPY --from=build /app/target/debug /app/

CMD ["/app/voting_system"]