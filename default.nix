# channel 23.05
{ pkgs ? import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/720e61ed8de116eec48d6baea1d54469b536b985.tar.gz") {} }:

with pkgs;

mkShell {
  buildInputs = [
    clojure
    leiningen
  ];
}
