import React, { useEffect, useState } from 'react';
import axios from 'axios';
import {jwtDecode, JwtPayload } from 'jwt-decode';
import { useNavigate } from 'react-router-dom';

interface User {
    username: string;
    email: string;
    fullName: string;
}

const Profile: React.FC = () => {
    const [user, setUser] = useState<User | null>(null);
    const [error, setError] = useState<string>('');
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('jwtToken');

        if (!token) {
            navigate('/login');
            return;
        }

        // Decode the JWT token to get the username
        const decodedToken = jwtDecode<JwtPayload & { sub: string }>(token);
        const username = decodedToken.sub;

        const fetchUserDetails = async () => {
            try {
                const response = await axios.get<User>(`http://localhost:8090/api/users/${username}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                    withCredentials: true,
                });
                setUser(response.data);
            } catch (err) {
                setError('Failed to fetch user details: ' + err);
            }
        };

        fetchUserDetails();
    }, [navigate]);

    const handleLogout = () => {
        localStorage.removeItem('jwtToken'); // Remove the token
        navigate('/'); // Redirect to landing page
    };

    const handleIndexNavigation = () => {
        navigate('/index'); // Navigate to the new "Index" component
    };

    if (error) {
        return <p className="error">{error}</p>;
    }

    if (!user) {
        return <p>Loading...</p>;
    }

    return (
        <div className="profile-container">
            <header className="profile-header">
                <nav className="profile-nav">
                    <button onClick={handleLogout} className="logout-button">Logout</button>
                    <button onClick={handleIndexNavigation} className="index-button">Index</button>
                </nav>
            </header>
            <div className="card">
                <h2>User Profile</h2>
                <p><strong>Username:</strong> {user.username}</p>
                <p><strong>Email:</strong> {user.email}</p>
            </div>
        </div>
    );
};

export default Profile;
