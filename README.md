[English](README.md) | [简体中文](README_zh.md)

# Wenyan addon

Wenyan Addon  is an experimental addon for the Wenyan ecosystem.
It is built on top of [Wenyan Programming](https://github.com/gyxx-xc/WenyanNature) to test and deliver novel, distinctive features.

This project follows a rapid iteration model. APIs, gameplay details, data formats, and compatibility behavior may change between versions.

Stable features may be promoted into [Wenyan Programming](https://github.com/gyxx-xc/WenyanNature) as part of the core mod.

> [!NOTE]
> **The role of Wenyan addon**
>
> This addon is a testing ground for new features and may contain unstable code. Features may later be incorporated into the core mod, or they may be removed or changed at any time.

## Features

- **Rapid iteration**: Experimental features and mechanics that are not yet suitable for the core mod are tested and developed here.
- **Beyond core limitations**: Enables interactions and gameplay that would be inconvenient to implement directly in [Wenyan Programming](https://github.com/gyxx-xc/WenyanNature).
- **Distinctive experiments**: Hosts more experimental, thematic, or independently modular content, including the built-in Pong module.

## Stability Notice

Wenyan Addon does not guarantee code stability.

This repository may contain experimental code, temporary APIs, feature migrations, and functionality that "works, technically."

> [!WARNING]
> Expect incompatibilities between versions. Saves, configuration, scripts, or code may require manual adjustments after an update.

## Development

Target environment:

- Minecraft `26.1.2`
- NeoForge `26.1.2.71`
- Java `25`

Common commands:

```bash
./gradlew compileJava
./gradlew runData
./gradlew build
```
## Contributing
We welcome your contributions! If you'd like to contribute, please follow these steps:
1. Fork this repository.
2. Create a new branch for your feature or bugfix: git checkout -b feature/your-feature-name.
3. Commit your changes and push them to your fork.
4. Open a Pull Request to the master branch of this repository.


## License

This project is licensed under the [MIT License](LICENSE).
