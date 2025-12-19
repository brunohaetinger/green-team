package main

import (
	"errors"
	"log"
	"net"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/joho/godotenv"
	reuseport "github.com/libp2p/go-reuseport"
	"github.com/thiagonasc/poll/internal/api"
)

func main() {
	if err := godotenv.Load(); err != nil {
	}

	srv := api.NewServer()
	srv.Routes()

	addr := ":8080"
	if v := os.Getenv("PORT"); strings.TrimSpace(v) != "" {
		addr = ":" + strings.TrimSpace(v)
	}
	s := &http.Server{
		Addr:              addr,
		ReadTimeout:       15 * time.Second,
		ReadHeaderTimeout: 15 * time.Second,
		WriteTimeout:      30 * time.Second,
		IdleTimeout:       60 * time.Second,
	}
	log.Printf("poll service starting on %s", addr)

	useReuse := strings.TrimSpace(os.Getenv("ENABLE_REUSEPORT")) == "1"
	var err error
	if useReuse {
		var ln net.Listener
		ln, err = reuseport.Listen("tcp", addr)
		if err != nil {
			log.Printf("reuseport listen failed, falling back to standard listener: %v", err)
		} else {
			err = s.Serve(ln)
		}
	}
	if !useReuse {
		err = s.ListenAndServe()
	}
	if err != nil && !errors.Is(err, http.ErrServerClosed) {
		log.Fatalf("server error: %v", err)
	}

	srv.Close()
}
