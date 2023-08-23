.PHONY: test
test:
	@lein kaocha

# not using lein because there were issues issues with lein-clj-kondo plugin
.PHONY: lint
lint:
	@clj -M:lint

.PHONY: puzzle run
puzzle run:
	@lein exec -p src/script/puzzle.clj 4

.PHONY: zebra
zebra:
	@lein exec -p src/script/zebra.clj

.PHONY: new-zebra
new-zebra:
	@lein exec -p src/script/new_zebra.clj
