const WebSocket = require('ws');

// Start WebSocket server on port 8080
const wss = new WebSocket.Server({ port: 8080 });
const clients = new Map(); // Map of userId -> WebSocket connection

console.log('WebSocket server is running on ws://0.0.0.0:8080');

wss.on('connection', (ws) => {
    console.log('New client connected');

    // When the client sends a message, we process it
    ws.on('message', (data) => {
        const message = JSON.parse(data.toString());
        console.log('Received:', message);

        // Register the connection with the sender's userId
        if (message.senderId) {
            clients.set(message.senderId, ws);
        }

        // Forward the message to the receiver if the receiver is connected
        if (message.receiverId && clients.has(message.receiverId)) {
            const receiverWs = clients.get(message.receiverId);
            if (receiverWs.readyState === WebSocket.OPEN) {
                receiverWs.send(JSON.stringify(message)); // Send message to the receiver
            }
        }
    });

    // Handle disconnections
    ws.on('close', () => {
        // Remove disconnected clients
        for (const [userId, clientWs] of clients) {
            if (clientWs === ws) {
                clients.delete(userId);
            }
        }
        console.log('Client disconnected');
    });
});

// const WebSocket = require('ws');

// // Create a WebSocket server that listens on all network interfaces
// const wss = new WebSocket.Server({ port: 8080, host: '0.0.0.0' });

// wss.on('connection', (ws) => {
//     console.log('Client connected');

//     ws.on('message', (message) => {
//         console.log('Received:', message);
//         wss.clients.forEach(client => {
//             if (client.readyState === WebSocket.OPEN) {
//                 client.send(message);
//             }
//         });
//     });

//     ws.on('close', () => {
//         console.log('Client disconnected');
//     });
// });

// console.log('WebSocket server is running on ws://0.0.0.0:8080');
 