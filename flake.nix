{
  description = "LaTeX development environment";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true; # <-- This allows steam-run and other unfree packages
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
             alsa-lib
             alsa-plugins
          ];
          packages = with pkgs; [
             maven
             javaPackages.compiler.openjdk25
             python315
          ];

          shellHook = ''
            echo "maven environment"
            echo ""
          '';
        };
      }
    );
}
