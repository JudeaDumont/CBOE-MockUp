import React from 'react';
import { useNavigate } from 'react-router-dom';

const Landing: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <div className="card" style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px' }}>
                <h2>Welcome</h2>
                <button onClick={() => navigate('/login')} style={{ margin: '10px' }}>Login</button>
                <button onClick={() => navigate('/create-user')} style={{ margin: '10px' }}>Create User</button>
            </div>
        </div>
    );
};

export default Landing;
