package api

import (
    "net/http"
)

const openAPISpec = `{
  "openapi": "3.0.3",
  "info": {
    "title": "Poll Service API",
    "version": "1.0.0",
    "description": "Simple high-throughput poll/voting service (only endpoints used by poll_ramp_10k.js are documented)."
  },
  "servers": [
    { "url": "/" }
  ],
  "tags": [
    { "name": "LoadTest", "description": "Endpoints used by the k6 load test" }
  ],
  "paths": {
    "/vote": {
      "post": {
        "tags": ["LoadTest"],
        "summary": "Submit a vote",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/VoteRequest" }
            }
          }
        },
        "responses": {
          "202": { "description": "Accepted" },
          "404": { "description": "Poll/Option not found" },
          "409": { "description": "Poll is closed" },
          "503": { "description": "Server busy" }
        }
      }
    },
    "/polls": {
      "post": {
        "tags": ["LoadTest"],
        "summary": "Create a poll",
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"type":"object","required":["id","question"],"properties":{"id":{"type":"string"},"question":{"type":"string"},"is_open":{"type":"boolean"}}}}}},
        "responses": {"201": {"description": "Created"}, "409": {"description": "Already exists"}}
      }
    },
    "/options": {
      "post": {
        "tags": ["LoadTest"],
        "summary": "Add option to a poll",
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"type":"object","required":["id","poll_id","label"],"properties":{"id":{"type":"string"},"poll_id":{"type":"string"},"label":{"type":"string"}}}}}},
        "responses": {"201": {"description": "Created"}, "404": {"description": "Poll not found"}, "409": {"description": "Already exists"}}
      }
    }
  },
  "components": {
    "schemas": {
      "VoteRequest": {
        "type": "object",
        "required": ["poll_id", "option_id", "voter_id"],
        "properties": {
          "poll_id": { "type": "string" },
          "option_id": { "type": "string" },
          "voter_id": { "type": "string" }
        }
      }
    }
  }
}`

func registerSwagger() {
    http.HandleFunc("/swagger.json", func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json")
        _, _ = w.Write([]byte(openAPISpec))
    })

    http.HandleFunc("/swagger", func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "text/html; charset=utf-8")
        _, _ = w.Write([]byte(`<!doctype html>
<html>
  <head>
    <meta charset="utf-8" />
    <title>Poll Service API Docs</title>
    <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
    <style>body { margin: 0; } #swagger-ui { height: 100vh; }</style>
  </head>
  <body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
    <script>
      window.ui = SwaggerUIBundle({
        url: '/swagger.json',
        dom_id: '#swagger-ui',
        presets: [SwaggerUIBundle.presets.apis],
        layout: 'BaseLayout'
      });
    </script>
  </body>
</html>`))
    })
}
