const scalingFactor = 10;
let pixelData = [];

document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById('drawing-canvas');
    const context = canvas.getContext('2d');
    const cursorCircle = document.getElementById('cursor-circle');
    const clearButton = document.getElementById('clear-button');

    let drawing = false;

    // Event listener for mouse move
    canvas.addEventListener('mousemove', (e) => {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        cursorCircle.style.left = `${e.clientX - cursorCircle.offsetWidth / 2}px`;
        cursorCircle.style.top = `${e.clientY - cursorCircle.offsetHeight / 2}px`;
        cursorCircle.style.display = 'block';
    });

    // Event listener for mouse leave
    canvas.addEventListener('mouseleave', () => {
        cursorCircle.style.display = 'none';
    });

    // Event listener for mouse down
    canvas.addEventListener('mousedown', () => {
        drawing = true;
    });

    // Event listener for mouse up
    canvas.addEventListener('mouseup', () => {
        drawing = false;
        context.beginPath(); // reset the context's path to avoid connecting lines
    });

    // Event listener for drawing
    canvas.addEventListener('mousemove', (e) => {
        if (!drawing) return;

        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        context.lineWidth = 25;
        context.lineCap = 'round';
        context.strokeStyle = 'black';

        context.lineTo(x, y);
        context.stroke();
        context.beginPath();
        context.moveTo(x, y);
    });

    // Event listener for clear button
    clearButton.addEventListener('click', () => {
        context.clearRect(0, 0, canvas.width, canvas.height);
    });

    // Function to print pixel data every second
    setInterval(() => {
        const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;
        pixelData = [];

        const index = 0;
        const red = data[index];
        const green = data[index + 1];
        const blue = data[index + 2];
        const alpha = data[index + 3];
        const average = (red + green + blue) / 3.0;

        // Downscale to 28x28
        for (let y = 0; y < 280; y += 10) {
            for (let x = 0; x < 280; x += 10) {
                let total = 0.0;
                for (let dy = 0; dy < 10; dy++) {
                    for (let dx = 0; dx < 10; dx++) {
                        const index = ((y + dy) * 280 + (x + dx)) * 4;
                        const red = data[index];
                        const green = data[index + 1];
                        const blue = data[index + 2];
                        const alpha = data[index + 3];
                        const average = (red + green + blue) / 3.0;
                        
                        total += alpha > 0?average:255.0;
                    }
                }
                const avgColor = total / 100.0; // If no pixels, assume white
                const darkness = 1 - avgColor / 255.0;
                pixelData.push(darkness);
            }
        }
        
        // const asciiChars = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'.";
        // const asciiArray = pixelData.map(val => {
        //     const index = Math.floor((1.0-val) * (asciiChars.length - 1));
        //     return asciiChars[index];
        // });
    
        // // Convert the 1D array into a 28x28 grid
        // const asciiGrid = [];
        // for (let i = 0; i < 28; i++) {
        //     const row = asciiArray.slice(i * 28, i * 28 + 28).map(char => char + char).join('');
        //     asciiGrid.push(row);
        // }
        // // Display the ASCII art
        // const preElement = document.getElementById('ascii-art');
        // preElement.textContent = asciiGrid.join('\n');

        sendPixelArray();
    }, 333);
    
    connect();
});