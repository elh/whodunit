.PHONY: test
test:
	@lein kaocha

# not using lein because there were issues issues with lein-clj-kondo plugin
.PHONY: lint
lint:
	@clj -M:lint

.PHONY: generate run
generate run:
	@lein exec -p src/script/generate.clj

.PHONY: zebra
zebra:
	@lein exec -p src/script/zebra.clj
