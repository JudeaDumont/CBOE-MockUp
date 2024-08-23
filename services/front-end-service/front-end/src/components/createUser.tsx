import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';

const CreateUser: React.FC = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        try {
            // Prepare the login request payload
            const loginRequest = {username, password, email};

            // Send the login request to the backend
            const response = await fetch('http://localhost:8080/api/user-details/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(loginRequest),
                credentials: 'include',
            });

            if (response.ok) {
                const data = await response.json();

                // Store the JWT token in localStorage
                localStorage.setItem('jwtToken', data.token);

                // Redirect to the profile page or another secure page
                navigate('/profile');
            } else {
                // Handle errors if the response is not ok (e.g., authentication failed)
                const errorText = await response.text();
                setError(errorText || 'Login failed');
            }
        } catch (error) {
            // Handle any other errors (e.g., network issues)
            setError('Error: ' + error);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Create User</h2>
            {error && <p>{error}</p>}
            <label htmlFor="username">Username</label>
            <input
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
            />
            <label htmlFor="email">Email</label>
            <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
            />
            <label htmlFor="password">Password</label>
            <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
            />
            <button type="submit">Create Account</button>
        </form>
    );
};

export default CreateUser;
