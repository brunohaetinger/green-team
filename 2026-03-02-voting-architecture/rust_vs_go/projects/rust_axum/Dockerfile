FROM rust:alpine3.22 AS build

# ENV HTTPS_PROXY=set proxy if needed

WORKDIR /app
RUN apk add --no-cache musl-dev vim curl

COPY . .
RUN cargo build --release

FROM alpine:3.22
WORKDIR /app

COPY --from=build /app/target/release/voting_system /app/

CMD ["/app/voting_system"]