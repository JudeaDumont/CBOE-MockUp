import React, {useEffect, useState} from 'react';

interface IndexData {
    indexName: string;
    timestamp: number;
    value: number;
}

const formatValue = (value: number): string => {
    return value.toFixed(2); // Rounds the value to the nearest hundredth
};

const formatTimestamp = (timestamp: number): string => {
    const date = new Date(timestamp);
    return date.toLocaleString(); // Formats the timestamp using the local timezone
};

const Index: React.FC = () => {
    const [data, setData] = useState<number[][]>(Array.from({length: 10}, () => Array(10).fill(0)));
    const [indexTicker, setIndexTicker] = useState<IndexData>();

    useEffect(() => {
        const socket = new WebSocket('ws://localhost:8060/ws/financial-data'); // Replace with your WebSocket URL

        socket.onmessage = (event) => {
            const updatedData = JSON.parse(event.data);
            setIndexTicker(updatedData);
        };

        return () => {
            socket.close();
        };
    }, []);

    return (
        <div>
            <div className="index-container">
                <h2>Index Ticker</h2>
                {indexTicker ? (
                    <div>
                        {indexTicker.indexName} : {formatValue(indexTicker.value)} @ {formatTimestamp(indexTicker.timestamp)}
                    </div>
                ) : (
                    <div>
                        Index data is unavailable
                    </div>
                )}

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
        </div>
    );
};

export default Index;
