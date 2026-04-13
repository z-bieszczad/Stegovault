# StegoVault

StegoVault is a Java application for encrypting and hiding text data inside images using steganography techniques. The project follows a layered architecture and includes a graphical user interface built with JavaFX.

## Architecture

The project is structured using a layered MVC-like architecture:

- `controller` – handles user input and application flow
- `model` – data structures and core domain objects
- `service` – business logic (encryption, steganography processing)
- `util` – helper and utility classes
- `view` – UI logic and JavaFX integration

## UI
The user interface is built using JavaFX:
- `fxml` – UI layout definitions
- `css` – styling for the interface

## Features

- Text encryption before embedding
- Hiding encrypted text inside images
- Extracting hidden data from images
- JavaFX-based graphical interface
- Modular and extensible design

## Technologies

- Java
- JavaFX (FXML + CSS)
- Image processing
- Basic cryptography
- Steganography techniques

## Testing

Unit tests are located in:
- `src/test/java`

## Purpose

This project was created for educational purposes to learn:
- data hiding techniques (steganography)
- encryption and decryption
- image processing in Java
- building GUI applications using JavaFX
- clean layered architecture (MVC-style)


