# FNN Web Demo

## Description

This project implements a **Feed-Forward Neural Network (FNN)** from scratch using **Java** and **Spring Boot**. The neural network is trained on the **MNIST dataset** to classify handwritten digits (0-9). The project demonstrates the functionality of the FNN by using a full-stack web application built with a **Java Spring Boot** backend and a **JavaScript** frontend.

### Features:
- **28x28 pixel handwritten digit classification** using FNN
- **Backpropagation** using **gradient descent**
- Customizable network layers, learning rates, and number of training epochs
- **96% accuracy** on 10,000 testing images (accuracy varies due to randomness at the beginning of training)
- **Matrix class** for matrix operations
- Misclassified images are stored in `errors.txt` as ASCII art
- Data from training epochs are saved in `data.txt`
- Lists of weights and biases saved in `savedBiases` and `savedWeights`
- Flexible input and output layers to support other classification tasks, with the ability to scale input data between 0 and 1

### Limitations:
- The project is based on a feed-forward neural network, which can be computationally expensive and prone to overfitting with large datasets.
- Requires a significant amount of training data for accurate classification, and alternative models might be more efficient.

## How to Run

To run the project, follow these steps:

1. Clone the repository:
    ```bash
    git clone https://github.com/Khalil-Burns/fnn-web-demo.git
    cd fnn-web-demo
    ```

2. Build and run the project:
    - To run the application using Gradle:
        ```bash
        ./gradlew bootRun
        ```

    - If that doesn't work, try building and running manually:
        ```bash
        ./gradlew build
        java -jar build/libs/fnn-server-0.0.1-SNAPSHOT.jar
        ```

3. Open the web app in your browser:
    - The web app will be hosted at [localhost:8080](http://localhost:8080).

## Technologies Used
- **Java**: For the neural network implementation
- **Spring Boot**: Backend framework for creating the server and API
- **JavaScript**: Frontend for interacting with the neural network
- **HTML/CSS**: For the user interface
- **WebSockets**: For communication between frontend and backend

## MNIST Dataset

The **MNIST** dataset is used for training and testing the network. It consists of 28x28 pixel images of handwritten digits. The dataset is publicly available [here](https://yann.lecun.com/exdb/mnist/).

## Project Structure

- `src/main/java/`: Java code for the FNN and Spring Boot backend.
- `src/main/resources/static/`: Frontend files (HTML, CSS, JavaScript).
- `build/libs/`: Compiled `.jar` file.

## Notes

This neural network is designed to classify handwritten digits, but it is not limited to the MNIST dataset. The input layer can be adjusted to work with other classification tasks, as long as the input data is normalized between 0 and 1.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
