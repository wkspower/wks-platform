default: test

test:
	@opa test -v tests .

docker:
	@docker build -t wks/opa .

start:
	@docker run --rm -ti wks/opa run --server /policies

.PHONY: test docker start