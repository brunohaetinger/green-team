package api

import (
	"net/http"

	"github.com/gin-gonic/gin"
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
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"type":"object","required":["id","question"],"properties":{"id":{"type":"integer"},"question":{"type":"string"},"is_open":{"type":"boolean"}}}}}},
        "responses": {"201": {"description": "Created"}, "409": {"description": "Already exists"}}
      }
    },
    "/options": {
      "post": {
        "tags": ["LoadTest"],
        "summary": "Add option to a poll",
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"type":"object","required":["id","poll_id","label"],"properties":{"id":{"type":"integer"},"poll_id":{"type":"integer"},"label":{"type":"string"}}}}}},
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
          "poll_id": { "type": "integer" },
          "option_id": { "type": "integer" },
          "voter_id": { "type": "string" }
        }
      }
    }
  }
} `

func registerSwagger(r gin.IRoutes) {
	r.GET("/swagger.json", func(c *gin.Context) {
		c.Header("Content-Type", "application/json")
		c.String(http.StatusOK, openAPISpec)
	})

	r.GET("/swagger", func(c *gin.Context) {
		c.Header("Content-Type", "text/html; charset=utf-8")
		c.String(http.StatusOK, `<!doctype html>
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
</html>`)
	})
}
