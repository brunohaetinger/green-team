#!/bin/sh
set -e

# Start the original entrypoint in the background, overriding the command to use our custom config
/usr/local/bin/docker-entrypoint.sh postgres -c config_file=/var/lib/postgresql/data/postgresql.conf &

# Wait for PostgreSQL to be available
until pg_isready -U "$POSTGRES_USER" -d postgres; do
  echo "Waiting for postgres..."
  sleep 2
done

# Create the database if it doesn't exist
psql -U "$POSTGRES_USER" -d postgres -tc "SELECT 1 FROM pg_database WHERE datname = '$POSTGRES_DB'" | grep -q 1 || \
  psql -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE \"$POSTGRES_DB\";"

# Wait for the background postgres to exit (keeps the container running)
wait
