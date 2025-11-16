include .env
export $(shell sed 's/=.*//' .env)

.PHONY: connect-db start-server restart-server stop-server logs

connect-db:
	docker compose exec -it rupheus-database psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)

start-server:
	docker compose up --build -d

restart-server:
	docker compose down
	docker compose build --no-cache
	docker compose up -d

stop-server:
	docker compose down -v

logs:
	docker logs -f rupheus-server
