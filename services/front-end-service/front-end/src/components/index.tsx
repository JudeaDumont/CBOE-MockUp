import React, { useEffect, useState } from 'react';

const Index: React.FC = () => {
    const [data, setData] = useState<number[][]>(Array.from({ length: 10 }, () => Array(10).fill(0)));

    useEffect(() => {
        const socket = new WebSocket('ws://your-websocket-url'); // Replace with your WebSocket URL

        socket.onmessage = (event) => {
            const updatedData = JSON.parse(event.data);
            setData(updatedData);
        };

        return () => {
            socket.close();
        };
    }, []);

    return (
        <div className="index-container">
            <h2>Real-Time Data Table</h2>
            <table className="data-table">
                <tbody>
                {data.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                        {row.map((cell, cellIndex) => (
                            <td key={cellIndex}>{cell}</td>
                        ))}
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default Index;
