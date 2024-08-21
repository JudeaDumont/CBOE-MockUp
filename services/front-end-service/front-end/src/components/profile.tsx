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
                const response = await axios.get<User>(`http://localhost:8080/api/users/${username}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                setUser(response.data);
            } catch (err) {
                setError('Failed to fetch user details: ' + err);
            }
        };

        fetchUserDetails();
    }, [navigate]);

    if (error) {
        return <p style={{ color: 'red' }}>{error}</p>;
    }

    if (!user) {
        return <p>Loading...</p>;
    }

    return (
        <div>
            <h2>User Profile</h2>
            <p><strong>Username:</strong> {user.username}</p>
            <p><strong>Email:</strong> {user.email}</p>
            <p><strong>Full Name:</strong> {user.fullName}</p>
        </div>
    );
};

export default Profile;